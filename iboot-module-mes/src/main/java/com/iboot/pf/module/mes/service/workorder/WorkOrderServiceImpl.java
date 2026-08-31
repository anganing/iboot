package com.iboot.pf.module.mes.service.workorder;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderPageReqVO;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderRespVO;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_HAS_OP_TASK;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_STATUS_INVALID;

/**
 * MES-作业工单 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class WorkOrderServiceImpl implements WorkOrderService {

    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesRelationFiller relationFiller;

    @Override
    public Long createWorkOrder(WorkOrderSaveReqVO createReqVO) {
        WorkOrderDO workOrder = BeanUtils.toBean(createReqVO, WorkOrderDO.class);
        workOrder.setId(null);
        workOrder.setWorkOrderNo(codeService.generate(MesCodeRuleConstants.WORK_ORDER, workOrder.getWorkOrderNo()));
        // 工厂、车间、项目、生产工单号都从生产工单继承，避免两边不一致
        inheritFromProdOrder(workOrder);
        snapshotService.validateWc(workOrder.getWcId());
        // 进度数量与齐套状态由业务动作推进，创建时归零
        workOrder.setCompletedQty(MesConstants.ZERO_QTY);
        workOrder.setKitStatus(MesKitStatusEnum.UNCHECKED.name());
        workOrder.setVersion(MesConstants.INIT_VERSION);
        validateWorkOrderNoUnique(workOrder.getWorkOrderNo(), null);
        workOrderMapper.insert(workOrder);
        return workOrder.getId();
    }

    @Override
    public void updateWorkOrder(WorkOrderSaveReqVO updateReqVO) {
        WorkOrderDO existed = validateWorkOrderExists(updateReqVO.getId());
        if (MesWorkOrderStatusEnum.isFinal(existed.getStatus())) {
            throw exception(WORK_ORDER_STATUS_INVALID);
        }
        WorkOrderDO updateObj = BeanUtils.toBean(updateReqVO, WorkOrderDO.class);
        // 工单号、进度、齐套、实际时间、乐观锁都不由表单维护
        updateObj.setWorkOrderNo(existed.getWorkOrderNo());
        updateObj.setCompletedQty(existed.getCompletedQty());
        updateObj.setKitStatus(existed.getKitStatus());
        updateObj.setActualStartTime(existed.getActualStartTime());
        updateObj.setActualEndTime(existed.getActualEndTime());
        updateObj.setVersion(existed.getVersion());
        inheritFromProdOrder(updateObj);
        snapshotService.validateWc(updateObj.getWcId());
        workOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteWorkOrder(Long id) {
        WorkOrderDO existed = validateWorkOrderExists(id);
        if (!MesWorkOrderStatusEnum.isPending(existed.getStatus())) {
            throw exception(WORK_ORDER_STATUS_INVALID);
        }
        validateNoOpTask(id);
        workOrderMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkOrderListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            WorkOrderDO existed = validateWorkOrderExists(id);
            if (!MesWorkOrderStatusEnum.isPending(existed.getStatus())) {
                throw exception(WORK_ORDER_STATUS_INVALID);
            }
            validateNoOpTask(id);
        }
        workOrderMapper.deleteByIds(ids);
    }

    private WorkOrderDO validateWorkOrderExists(Long id) {
        WorkOrderDO workOrder = id == null ? null : workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw exception(WORK_ORDER_NOT_EXISTS);
        }
        return workOrder;
    }

    /** 已生成工序任务的作业工单不允许删除。 */
    private void validateNoOpTask(Long workOrderId) {
        if (opTaskMapper.selectCountByWorkOrderId(workOrderId) > 0) {
            throw exception(WORK_ORDER_HAS_OP_TASK);
        }
    }

    /**
     * 作业工单号唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateWorkOrderNoUnique(String workOrderNo, Long excludeId) {
        WorkOrderDO existed = workOrderMapper.selectByWorkOrderNo(workOrderNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(WORK_ORDER_NO_DUPLICATE);
    }

    /**
     * 从生产工单继承组织维度与单号快照。
     *
     * <p>作业工单是生产工单在车间的执行单位，工厂、车间、项目必须与主单一致，
     * 让前端各填一遍只会制造不一致；只有新建或已下达的生产工单允许开作业工单。
     */
    private void inheritFromProdOrder(WorkOrderDO workOrder) {
        ProdOrderDO prodOrder = prodOrderMapper.selectById(workOrder.getProdOrderId());
        if (prodOrder == null) {
            throw exception(PROD_ORDER_NOT_EXISTS);
        }
        if (MesProdOrderStatusEnum.isFinal(prodOrder.getStatus())) {
            throw exception(PROD_ORDER_STATUS_INVALID);
        }
        workOrder.setProdOrderNo(prodOrder.getOrderNo());
        workOrder.setPlantId(prodOrder.getPlantId());
        workOrder.setWorkshopId(prodOrder.getWorkshopId());
        workOrder.setProjectId(prodOrder.getProjectId());
        workOrder.setProjectTaskId(prodOrder.getProjectTaskId());
    }

    @Override
    public WorkOrderRespVO getWorkOrder(Long id) {
        WorkOrderRespVO respVO = workOrderMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public List<WorkOrderDO> getWorkOrderListByProdOrderIds(Collection<Long> prodOrderIds) {
        if (CollUtil.isEmpty(prodOrderIds)) {
            return Collections.emptyList();
        }
        return workOrderMapper.selectListByProdOrderIds(prodOrderIds);
    }

    @Override
    public List<WorkOrderDO> getWorkOrderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return workOrderMapper.selectByIds(ids);
    }

    @Override
    public PageResult<WorkOrderRespVO> getWorkOrderPage(WorkOrderPageReqVO pageReqVO) {
        PageResult<WorkOrderRespVO> pageResult = workOrderMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /** 跨模块可读字段补全，放在 Service 以便 Excel 导出复用。 */
    private void fillRelationNames(List<WorkOrderRespVO> list) {
        relationFiller.fillPlantName(list, WorkOrderRespVO::getPlantId, WorkOrderRespVO::setPlantName);
        relationFiller.fillWorkshopName(list, WorkOrderRespVO::getWorkshopId, WorkOrderRespVO::setWorkshopName);
        relationFiller.fillWcName(list, WorkOrderRespVO::getWcId, WorkOrderRespVO::setWcName);
        relationFiller.fillProjectName(list, WorkOrderRespVO::getProjectId, WorkOrderRespVO::setProjectName);
        relationFiller.fillProjectTaskName(list, WorkOrderRespVO::getProjectTaskId,
                WorkOrderRespVO::setProjectTaskName);
    }

}
