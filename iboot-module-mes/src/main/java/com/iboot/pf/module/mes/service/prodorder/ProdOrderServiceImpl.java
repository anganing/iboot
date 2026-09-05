package com.iboot.pf.module.mes.service.prodorder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteRespDTO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderImportExcelVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderImportRespVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderPageReqVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderRespVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesImportResolver;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_HAS_WORK_ORDER;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_VERSION_SNAPSHOT_LOCKED;

/**
 * MES-生产工单 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ProdOrderServiceImpl implements ProdOrderService {

    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesRelationFiller relationFiller;
    @Resource
    private MesImportResolver importResolver;

    @Override
    public Long createProdOrder(ProdOrderSaveReqVO createReqVO) {
        ProdOrderDO prodOrder = BeanUtils.toBean(createReqVO, ProdOrderDO.class);
        prodOrder.setId(null);
        prodOrder.setOrderNo(codeService.generate(MesCodeRuleConstants.PROD_ORDER, prodOrder.getOrderNo()));
        prodOrder.setDemandId(prodOrder.getDemandId() == null ? MesConstants.NONE_ID : prodOrder.getDemandId());
        validateForeignKeys(prodOrder);
        fillSnapshot(prodOrder);
        // 状态不由表单维护，新建工单一律落 DRAFT，后续只能由下达等领域动作推进
        prodOrder.setStatus(MesProdOrderStatusEnum.DRAFT.name());
        // 进度数量由报工推进，齐套状态由齐套检查写入，创建时归零
        prodOrder.setCompletedQty(MesConstants.ZERO_QTY);
        prodOrder.setScrapQty(MesConstants.ZERO_QTY);
        prodOrder.setKitStatus(MesKitStatusEnum.UNCHECKED.name());
        prodOrder.setVersion(MesConstants.INIT_VERSION);
        validateOrderNoUnique(prodOrder.getOrderNo(), null);
        prodOrderMapper.insert(prodOrder);
        return prodOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ProdOrderImportRespVO importProdOrderList(List<ProdOrderImportExcelVO> importList) {
        ProdOrderImportRespVO respVO = ProdOrderImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        if (CollUtil.isEmpty(importList)) {
            return respVO;
        }
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createProdOrder(convertImportItem(importItem));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    /** 导入模板填编码，编号在这里解析；BOM 与工艺路线取物料当天的生效版本自动锁定。 */
    private ProdOrderSaveReqVO convertImportItem(ProdOrderImportExcelVO importItem) {
        ProdOrderSaveReqVO reqVO = new ProdOrderSaveReqVO();
        reqVO.setDemandId(MesConstants.NONE_ID);
        reqVO.setPlantId(importResolver.requirePlantId(importItem.getPlantCode()));
        reqVO.setWorkshopId(importResolver.requireWorkshopId(importItem.getWorkshopCode()));
        reqVO.setProjectId(importResolver.resolveProjectId(importItem.getProjectCode()));
        reqVO.setProjectTaskId(MesConstants.NONE_ID);
        Long matId = importResolver.requireMatId(importItem.getMatCode());
        reqVO.setMatId(matId);
        reqVO.setQty(importItem.getQty());
        reqVO.setUomId(importResolver.requireUomId(importItem.getUomCode()));
        LocalDate today = LocalDate.now();
        BomRespDTO bom = snapshotService.requireEffectiveBom(matId, today);
        RouteRespDTO route = snapshotService.requireEffectiveRoute(matId, today);
        reqVO.setBomId(bom.getId());
        reqVO.setRouteId(route.getId());
        reqVO.setDueDate(importItem.getDueDate());
        reqVO.setPriority(importItem.getPriority() == null ? 0 : importItem.getPriority());
        reqVO.setRemark(importItem.getRemark());
        return reqVO;
    }

    @Override
    public void updateProdOrder(ProdOrderSaveReqVO updateReqVO) {
        ProdOrderDO existed = validateProdOrderExists(updateReqVO.getId());
        // 终态工单不允许再改，已开工的工单也不允许改数量与锁定的 BOM/路线
        if (MesProdOrderStatusEnum.isFinal(existed.getStatus())) {
            throw exception(PROD_ORDER_STATUS_INVALID);
        }
        ProdOrderDO updateObj = BeanUtils.toBean(updateReqVO, ProdOrderDO.class);
        // 工单号、状态、进度数量、齐套状态、时间戳、乐观锁都不由表单维护
        updateObj.setOrderNo(existed.getOrderNo());
        updateObj.setStatus(existed.getStatus());
        updateObj.setCompletedQty(existed.getCompletedQty());
        updateObj.setScrapQty(existed.getScrapQty());
        updateObj.setKitStatus(existed.getKitStatus());
        updateObj.setReleaseTime(existed.getReleaseTime());
        updateObj.setActualStartTime(existed.getActualStartTime());
        updateObj.setActualEndTime(existed.getActualEndTime());
        updateObj.setVersion(existed.getVersion());
        updateObj.setDemandId(updateObj.getDemandId() == null ? MesConstants.NONE_ID : updateObj.getDemandId());
        boolean draft = MesProdOrderStatusEnum.isDraft(existed.getStatus());
        if (!draft) {
            validateVersionSnapshotLocked(existed, updateObj);
        }
        validateForeignKeys(updateObj);
        fillSnapshot(updateObj);
        if (!draft) {
            // fillSnapshot 会按 bomId / routeId 从主数据回读版本号。下达之后必须沿用下达那一刻的快照，
            // 否则主数据把已发布版本的版本号改了，一次改备注就把在制工单的执行依据换掉了。
            updateObj.setBomVersion(existed.getBomVersion());
            updateObj.setRouteVersion(existed.getRouteVersion());
        }
        prodOrderMapper.updateById(updateObj);
    }

    /**
     * 下达之后四个版本快照不可改。
     *
     * <p>下达那一刻起这张工单的执行依据就固定了，之后主数据改 BOM、改工艺都不影响在制工单，这是基石 #8。
     * 表单只提交 {@code bomId} 与 {@code routeId}，两个版本号由 {@link #fillSnapshot} 回读补全，
     * 因此拦住这两个编号就锁住了 {@code bom_id} {@code bom_version} {@code route_id} {@code route_version}
     * 四个字段。
     *
     * <p>为什么是报错而不是静默沿用原值：计划员改了 BOM 却收不到任何提示，会以为在制工单已经换了版本，
     * 照新 BOM 去备料。
     */
    private void validateVersionSnapshotLocked(ProdOrderDO existed, ProdOrderDO updateObj) {
        if (!ObjUtil.equal(existed.getBomId(), updateObj.getBomId())
                || !ObjUtil.equal(existed.getRouteId(), updateObj.getRouteId())) {
            throw exception(PROD_ORDER_VERSION_SNAPSHOT_LOCKED);
        }
    }

    @Override
    public void deleteProdOrder(Long id) {
        ProdOrderDO existed = validateProdOrderExists(id);
        // 只有新建状态允许删除，已下达的工单下面挂着作业工单与工序任务
        if (!MesProdOrderStatusEnum.isDraft(existed.getStatus())) {
            throw exception(PROD_ORDER_STATUS_INVALID);
        }
        validateNoWorkOrder(id);
        prodOrderMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProdOrderListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 批量删也要逐条校验状态与占用，漏一个就是脏数据
        for (Long id : ids) {
            ProdOrderDO existed = validateProdOrderExists(id);
            if (!MesProdOrderStatusEnum.isDraft(existed.getStatus())) {
                throw exception(PROD_ORDER_STATUS_INVALID);
            }
            validateNoWorkOrder(id);
        }
        prodOrderMapper.deleteByIds(ids);
    }

    private ProdOrderDO validateProdOrderExists(Long id) {
        ProdOrderDO prodOrder = id == null ? null : prodOrderMapper.selectById(id);
        if (prodOrder == null) {
            throw exception(PROD_ORDER_NOT_EXISTS);
        }
        return prodOrder;
    }

    /** 已生成作业工单的生产工单不允许删除。 */
    private void validateNoWorkOrder(Long prodOrderId) {
        if (workOrderMapper.selectCountByProdOrderId(prodOrderId) > 0) {
            throw exception(PROD_ORDER_HAS_WORK_ORDER);
        }
    }

    /**
     * 工单号唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateOrderNoUnique(String orderNo, Long excludeId) {
        ProdOrderDO existed = prodOrderMapper.selectByOrderNo(orderNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(PROD_ORDER_NO_DUPLICATE);
    }

    /** 外键校验：工厂、车间、项目都要真实存在。 */
    private void validateForeignKeys(ProdOrderDO prodOrder) {
        snapshotService.validatePlant(prodOrder.getPlantId());
        snapshotService.validateWorkshop(prodOrder.getWorkshopId());
        snapshotService.validateProject(prodOrder.getProjectId(), prodOrder.getProjectTaskId());
    }

    /** 物料、单位、BOM 版本、工艺路线版本快照由后端补全，前端只提交编号。 */
    private void fillSnapshot(ProdOrderDO prodOrder) {
        MaterialRespDTO material = snapshotService.requireMaterial(prodOrder.getMatId());
        prodOrder.setMatCode(material.getCode());
        prodOrder.setMatName(material.getName());
        prodOrder.setMatSpec(material.getSpec());
        prodOrder.setUomCode(snapshotService.requireUomCode(prodOrder.getUomId()));
        prodOrder.setBomVersion(snapshotService.requireBomVersion(prodOrder.getBomId()));
        prodOrder.setRouteVersion(snapshotService.requireRouteVersion(prodOrder.getRouteId()));
    }

    @Override
    public ProdOrderRespVO getProdOrder(Long id) {
        ProdOrderRespVO respVO = prodOrderMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public List<ProdOrderDO> getProdOrderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return prodOrderMapper.selectByIds(ids);
    }

    @Override
    public List<ProdOrderDO> getProdOrderListByPlantId(Long plantId) {
        return prodOrderMapper.selectListByPlantId(plantId);
    }

    @Override
    public PageResult<ProdOrderRespVO> getProdOrderPage(ProdOrderPageReqVO pageReqVO) {
        PageResult<ProdOrderRespVO> pageResult = prodOrderMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /** 跨模块可读字段补全，放在 Service 以便 Excel 导出复用。 */
    private void fillRelationNames(List<ProdOrderRespVO> list) {
        relationFiller.fillPlantName(list, ProdOrderRespVO::getPlantId, ProdOrderRespVO::setPlantName);
        relationFiller.fillWorkshopName(list, ProdOrderRespVO::getWorkshopId, ProdOrderRespVO::setWorkshopName);
        relationFiller.fillProjectName(list, ProdOrderRespVO::getProjectId, ProdOrderRespVO::setProjectName);
        relationFiller.fillProjectTaskName(list, ProdOrderRespVO::getProjectTaskId,
                ProdOrderRespVO::setProjectTaskName);
    }

}
