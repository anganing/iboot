package com.iboot.pf.module.mes.service.optask;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.api.op.dto.OpRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpRespDTO;
import com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.dispatchtask.DispatchTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpExecModeEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import com.iboot.pf.module.mes.service.workorder.WorkOrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_HAS_DISPATCH;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NO_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_ROUTE_OP_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;

/**
 * MES-工序任务 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class OpTaskServiceImpl implements OpTaskService {

    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private DispatchTaskMapper dispatchTaskMapper;
    @Resource
    private WorkOrderService workOrderService;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesRelationFiller relationFiller;

    @Override
    public Long createOpTask(OpTaskSaveReqVO createReqVO) {
        OpTaskDO opTask = BeanUtils.toBean(createReqVO, OpTaskDO.class);
        opTask.setId(null);
        opTask.setTaskNo(codeService.generate(MesCodeRuleConstants.OP_TASK, opTask.getTaskNo()));
        fillFromWorkOrderAndRouteOp(opTask);
        snapshotService.validateRes(opTask.getResId());
        // 进度数量由报工推进，工序链由生成逻辑维护，创建时归零
        opTask.setCompletedQty(MesConstants.ZERO_QTY);
        opTask.setScrapQty(MesConstants.ZERO_QTY);
        opTask.setPrevTaskId(MesConstants.NONE_ID);
        opTask.setNextTaskId(MesConstants.NONE_ID);
        opTask.setVersion(MesConstants.INIT_VERSION);
        validateTaskNoUnique(opTask.getTaskNo(), null);
        validateRouteOpUnique(opTask.getWorkOrderId(), opTask.getRouteOpId(), null);
        opTaskMapper.insert(opTask);
        return opTask.getId();
    }

    @Override
    public void updateOpTask(OpTaskSaveReqVO updateReqVO) {
        OpTaskDO existed = validateOpTaskExists(updateReqVO.getId());
        // 终态任务不允许再改，否则报工与流水会跟着错位
        if (MesOpTaskStatusEnum.isFinal(existed.getStatus())) {
            throw exception(OP_TASK_STATUS_INVALID);
        }
        OpTaskDO updateObj = BeanUtils.toBean(updateReqVO, OpTaskDO.class);
        // 任务号、进度数量、工序链、实际时间、乐观锁都不由表单维护
        updateObj.setTaskNo(existed.getTaskNo());
        updateObj.setCompletedQty(existed.getCompletedQty());
        updateObj.setScrapQty(existed.getScrapQty());
        updateObj.setPrevTaskId(existed.getPrevTaskId());
        updateObj.setNextTaskId(existed.getNextTaskId());
        updateObj.setActualStartTime(existed.getActualStartTime());
        updateObj.setActualEndTime(existed.getActualEndTime());
        updateObj.setVersion(existed.getVersion());
        fillFromWorkOrderAndRouteOp(updateObj);
        snapshotService.validateRes(updateObj.getResId());
        validateRouteOpUnique(updateObj.getWorkOrderId(), updateObj.getRouteOpId(), updateReqVO.getId());
        opTaskMapper.updateById(updateObj);
    }

    @Override
    public void deleteOpTask(Long id) {
        OpTaskDO existed = validateOpTaskExists(id);
        if (!MesOpTaskStatusEnum.isNotStarted(existed.getStatus())) {
            throw exception(OP_TASK_STATUS_INVALID);
        }
        validateNoDispatchTask(id);
        opTaskMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOpTaskListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            OpTaskDO existed = validateOpTaskExists(id);
            if (!MesOpTaskStatusEnum.isNotStarted(existed.getStatus())) {
                throw exception(OP_TASK_STATUS_INVALID);
            }
            validateNoDispatchTask(id);
        }
        opTaskMapper.deleteByIds(ids);
    }

    private OpTaskDO validateOpTaskExists(Long id) {
        OpTaskDO opTask = id == null ? null : opTaskMapper.selectById(id);
        if (opTask == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        return opTask;
    }

    /** 已派工的工序任务不允许删除。 */
    private void validateNoDispatchTask(Long opTaskId) {
        if (dispatchTaskMapper.selectCountByOpTaskId(opTaskId) > 0) {
            throw exception(OP_TASK_HAS_DISPATCH);
        }
    }

    /** 任务号唯一：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。 */
    private void validateTaskNoUnique(String taskNo, Long excludeId) {
        OpTaskDO existed = opTaskMapper.selectByTaskNo(taskNo);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(OP_TASK_NO_DUPLICATE);
    }

    /** 同一作业工单下一道路线工序只能生成一个任务，对应唯一索引 (work_order_id, route_op_id, deleted)。 */
    private void validateRouteOpUnique(Long workOrderId, Long routeOpId, Long excludeId) {
        OpTaskDO existed = opTaskMapper.selectByWorkOrderIdAndRouteOpId(workOrderId, routeOpId);
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(OP_TASK_ROUTE_OP_DUPLICATE);
    }

    /**
     * 从作业工单与路线工序带出快照。
     *
     * <p>工序编码、名称、顺序号、并行组、关键工序标记都是主数据的快照，让前端提交只会不一致；
     * 执行模式按并行组推导：填了并行组就是并行执行，否则顺序执行。
     */
    private void fillFromWorkOrderAndRouteOp(OpTaskDO opTask) {
        WorkOrderDO workOrder = workOrderMapper.selectById(opTask.getWorkOrderId());
        if (workOrder == null) {
            throw exception(WORK_ORDER_NOT_EXISTS);
        }
        opTask.setWorkOrderNo(workOrder.getWorkOrderNo());
        RouteOpRespDTO routeOp = snapshotService.requireRouteOp(opTask.getRouteOpId());
        OpRespDTO op = snapshotService.requireOp(routeOp.getOpId());
        opTask.setOpId(op.getId());
        opTask.setOpCode(op.getCode());
        opTask.setOpName(op.getName());
        opTask.setSeq(routeOp.getSeq());
        opTask.setParallelGroup(routeOp.getParallelGroup());
        opTask.setKeyFlag(Boolean.TRUE.equals(routeOp.getKeyFlag()));
        opTask.setExecMode(routeOp.getParallelGroup() == null || routeOp.getParallelGroup().isBlank()
                ? MesOpExecModeEnum.SEQUENTIAL.name() : MesOpExecModeEnum.PARALLEL.name());
        opTask.setResId(opTask.getResId() == null ? MesConstants.NONE_ID : opTask.getResId());
    }

    @Override
    public OpTaskRespVO getOpTask(Long id) {
        OpTaskRespVO respVO = opTaskMapper.selectJoinById(id);
        fillRelationNames(respVO == null ? Collections.emptyList() : Collections.singletonList(respVO));
        return respVO;
    }

    @Override
    public List<OpTaskDO> getOpTaskListByProdOrderIds(Collection<Long> prodOrderIds) {
        if (CollUtil.isEmpty(prodOrderIds)) {
            return Collections.emptyList();
        }
        List<WorkOrderDO> workOrders = workOrderService.getWorkOrderListByProdOrderIds(prodOrderIds);
        if (CollUtil.isEmpty(workOrders)) {
            return Collections.emptyList();
        }
        return opTaskMapper.selectListByWorkOrderIds(convertList(workOrders, WorkOrderDO::getId));
    }

    @Override
    public List<OpTaskDO> getOpTaskListByRouteOpIds(Collection<Long> routeOpIds) {
        if (CollUtil.isEmpty(routeOpIds)) {
            return Collections.emptyList();
        }
        return opTaskMapper.selectListByRouteOpIds(routeOpIds);
    }

    @Override
    public List<OpTaskDO> getOpTaskList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return opTaskMapper.selectByIds(ids);
    }

    @Override
    public PageResult<OpTaskRespVO> getOpTaskPage(OpTaskPageReqVO pageReqVO) {
        PageResult<OpTaskRespVO> pageResult = opTaskMapper.selectPage(pageReqVO);
        fillRelationNames(pageResult.getList());
        return pageResult;
    }

    /** 跨模块可读字段补全，放在 Service 以便 Excel 导出复用。 */
    private void fillRelationNames(List<OpTaskRespVO> list) {
        relationFiller.fillResName(list, OpTaskRespVO::getResId, OpTaskRespVO::setResName);
    }

    /**
     * 回写排程结果。
     *
     * <p>只更新资源与计划起止时间三个字段，用 {@code updateBatch} 一次落库；数量、状态、实际时间、
     * 乐观锁版本都不动，它们由 MES 自己的派工与报工动作推进。
     *
     * <p>跳过而不是报错的两类工序：已开工之后的（车间在做，挪走等于让现场按两份计划干活）、
     * 已完工/已跳过/已取消的（终态，改计划没有意义）。跳过属于正常业务结果，不是异常，
     * 所以返回实际生效条数让调用方知道差异，而不是让整批回写失败。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int applySchedule(ApplyScheduleReqDTO reqDTO) {
        Map<Long, ApplyScheduleReqDTO.Item> itemMap = reqDTO.getItems().stream()
                .collect(Collectors.toMap(ApplyScheduleReqDTO.Item::getOpTaskId, Function.identity(),
                        (left, right) -> right));
        List<OpTaskDO> existedTasks = opTaskMapper.selectByIds(itemMap.keySet());
        List<OpTaskDO> updates = new ArrayList<>();
        for (OpTaskDO existed : existedTasks) {
            if (!MesOpTaskStatusEnum.isNotStarted(existed.getStatus())) {
                continue;
            }
            ApplyScheduleReqDTO.Item item = itemMap.get(existed.getId());
            updates.add(OpTaskDO.builder()
                    .id(existed.getId())
                    .resId(item.getResId())
                    .plannedStartTime(item.getPlannedStartTime())
                    .plannedEndTime(item.getPlannedEndTime())
                    .build());
        }
        if (CollUtil.isEmpty(updates)) {
            return 0;
        }
        opTaskMapper.updateBatch(updates);
        return updates.size();
    }

}
