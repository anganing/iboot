package com.iboot.pf.module.mes.service.proddemand;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandImportExcelVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandImportRespVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandPageReqVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandRespVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.proddemand.ProdDemandDO;
import com.iboot.pf.module.mes.dal.mysql.proddemand.ProdDemandMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDemandSourceTypeEnum;
import com.iboot.pf.module.mes.enums.MesDemandStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesImportResolver;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.IDEMPOTENCY_KEY_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_DEMAND_HAS_PROD_ORDER;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_DEMAND_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_DEMAND_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_DEMAND_STATUS_INVALID;

/**
 * MES-生产需求 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ProdDemandServiceImpl implements ProdDemandService {

    @Resource
    private ProdDemandMapper prodDemandMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesRelationFiller relationFiller;
    @Resource
    private MesImportResolver importResolver;

    @Override
    public Long createProdDemand(ProdDemandSaveReqVO createReqVO) {
        ProdDemandDO prodDemand = BeanUtils.toBean(createReqVO, ProdDemandDO.class);
        prodDemand.setId(null);
        prodDemand.setDemandNo(codeService.generate(MesCodeRuleConstants.PROD_DEMAND, prodDemand.getDemandNo()));
        validateForeignKeys(prodDemand);
        fillSnapshot(prodDemand);
        validateDemandNoUnique(prodDemand.getDemandNo(), null);
        validateIdempotencyKeyUnique(prodDemand.getIdempotencyKey(), null);
        prodDemandMapper.insert(prodDemand);
        return prodDemand.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public ProdDemandImportRespVO importProdDemandList(List<ProdDemandImportExcelVO> importList) {
        ProdDemandImportRespVO respVO = ProdDemandImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        if (CollUtil.isEmpty(importList)) {
            return respVO;
        }
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                createProdDemand(convertImportItem(importItem));
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    /** 导入模板里填的是编码，这里解析成编号；导入的需求固定标记为 EXCEL 来源、新建状态。 */
    private ProdDemandSaveReqVO convertImportItem(ProdDemandImportExcelVO importItem) {
        ProdDemandSaveReqVO reqVO = new ProdDemandSaveReqVO();
        reqVO.setSourceType(MesDemandSourceTypeEnum.EXCEL.name());
        reqVO.setSourceNo(importItem.getSourceNo());
        reqVO.setPlantId(importResolver.requirePlantId(importItem.getPlantCode()));
        reqVO.setProjectId(importResolver.resolveProjectId(importItem.getProjectCode()));
        reqVO.setProjectTaskId(MesConstants.NONE_ID);
        reqVO.setMatId(importResolver.requireMatId(importItem.getMatCode()));
        reqVO.setQty(importItem.getQty());
        reqVO.setUomId(importResolver.requireUomId(importItem.getUomCode()));
        reqVO.setDueDate(importItem.getDueDate());
        reqVO.setPriority(importItem.getPriority() == null ? 0 : importItem.getPriority());
        reqVO.setStatus(MesDemandStatusEnum.DRAFT.name());
        reqVO.setRemark(importItem.getRemark());
        return reqVO;
    }

    @Override
    public void updateProdDemand(ProdDemandSaveReqVO updateReqVO) {
        ProdDemandDO existed = validateProdDemandExists(updateReqVO.getId());
        // 已完成或已取消的需求不允许再改，否则排产与工单会跟着一起乱
        if (MesDemandStatusEnum.isFinal(existed.getStatus())) {
            throw exception(PROD_DEMAND_STATUS_INVALID);
        }
        ProdDemandDO updateObj = BeanUtils.toBean(updateReqVO, ProdDemandDO.class);
        // 需求编号由编码规则分配，更新时不允许改
        updateObj.setDemandNo(existed.getDemandNo());
        validateForeignKeys(updateObj);
        fillSnapshot(updateObj);
        validateIdempotencyKeyUnique(updateObj.getIdempotencyKey(), updateReqVO.getId());
        prodDemandMapper.updateById(updateObj);
    }

    @Override
    public void deleteProdDemand(Long id) {
        validateProdDemandExists(id);
        validateNoProdOrder(id);
        prodDemandMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProdDemandListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 批量删也要逐条校验占用，否则会留下引用不到需求的工单
        for (Long id : ids) {
            validateProdDemandExists(id);
            validateNoProdOrder(id);
        }
        prodDemandMapper.deleteByIds(ids);
    }

    private ProdDemandDO validateProdDemandExists(Long id) {
        ProdDemandDO prodDemand = id == null ? null : prodDemandMapper.selectById(id);
        if (prodDemand == null) {
            throw exception(PROD_DEMAND_NOT_EXISTS);
        }
        return prodDemand;
    }

    /** 已生成生产工单的需求不允许删除。 */
    private void validateNoProdOrder(Long demandId) {
        if (prodOrderMapper.selectCountByDemandId(demandId) > 0) {
            throw exception(PROD_DEMAND_HAS_PROD_ORDER);
        }
    }

    /**
     * 需求编号唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateDemandNoUnique(String demandNo, Long excludeId) {
        ProdDemandDO existed = prodDemandMapper.selectByDemandNo(demandNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(PROD_DEMAND_NO_DUPLICATE);
    }

    /** 幂等键唯一：外部系统重复下发同一需求时直接拒绝。 */
    private void validateIdempotencyKeyUnique(String idempotencyKey, Long excludeId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        ProdDemandDO existed = prodDemandMapper.selectByIdempotencyKey(idempotencyKey);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(IDEMPOTENCY_KEY_DUPLICATE);
    }

    /** 外键校验：工厂、项目、项目任务都要真实存在，避免脏外键落库。 */
    private void validateForeignKeys(ProdDemandDO prodDemand) {
        snapshotService.validatePlant(prodDemand.getPlantId());
        snapshotService.validateProject(prodDemand.getProjectId(), prodDemand.getProjectTaskId());
    }

    /** 物料与单位快照由后端补全，前端只提交编号。 */
    private void fillSnapshot(ProdDemandDO prodDemand) {
        MaterialRespDTO material = snapshotService.requireMaterial(prodDemand.getMatId());
        prodDemand.setMatCode(material.getCode());
        prodDemand.setMatName(material.getName());
        prodDemand.setMatSpec(material.getSpec());
        prodDemand.setUomCode(snapshotService.requireUomCode(prodDemand.getUomId()));
    }

    @Override
    public ProdDemandRespVO getProdDemand(Long id) {
        ProdDemandRespVO respVO = prodDemandMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public PageResult<ProdDemandRespVO> getProdDemandPage(ProdDemandPageReqVO pageReqVO) {
        PageResult<ProdDemandRespVO> pageResult = prodDemandMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    @Override
    public List<ProdDemandDO> getProdDemandList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return prodDemandMapper.selectByIds(ids);
    }

    /**
     * 跨模块可读字段补全。
     *
     * <p>放在 Service 而非 Controller：Excel 导出复用这里的分页结果，写在 Controller 会丢字段。
     */
    private void fillRelationNames(List<ProdDemandRespVO> list) {
        relationFiller.fillPlantName(list, ProdDemandRespVO::getPlantId, ProdDemandRespVO::setPlantName);
        relationFiller.fillProjectName(list, ProdDemandRespVO::getProjectId, ProdDemandRespVO::setProjectName);
        relationFiller.fillProjectTaskName(list, ProdDemandRespVO::getProjectTaskId,
                ProdDemandRespVO::setProjectTaskName);
    }

}
