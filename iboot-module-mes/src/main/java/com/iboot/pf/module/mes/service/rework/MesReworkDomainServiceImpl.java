package com.iboot.pf.module.mes.service.rework;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.rework.ReworkDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.rework.ReworkMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStateMachine;
import com.iboot.pf.module.mes.enums.MesQcObjectTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.enums.MesReworkStateMachine;
import com.iboot.pf.module.mes.enums.MesReworkStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandService;
import com.iboot.pf.module.mes.service.prodorder.MesOrderExpandHelper;
import com.iboot.pf.module.mes.service.qctask.MesQcTaskDomainService;
import com.iboot.pf.module.mes.service.qctask.bo.MesCreateQcTaskBO;
import com.iboot.pf.module.mes.service.rework.bo.MesReworkActionBO;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import com.iboot.pf.module.mes.service.wiptxn.bo.MesWipTxnBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.REWORK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;

/** 返工领域动作实现。 */
@Service
public class MesReworkDomainServiceImpl implements MesReworkDomainService {

    private static final String SOURCE_MODULE_MES = "MES";
    private static final String KEY_SEPARATOR = ":";
    private static final String REWORK_REMARK_PREFIX = "返工任务：";

    @Resource
    private ReworkMapper reworkMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private MesOrderExpandHelper expandHelper;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesCommandService commandService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesQcTaskDomainService qcTaskDomainService;
    @Resource
    private MesWipTxnRecorder wipTxnRecorder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRework(ReworkDO rework) {
        OpTaskDO sourceTask = requireOpTask(rework.getOpTaskId());
        WorkOrderDO sourceWorkOrder = requireWorkOrder(sourceTask.getWorkOrderId());
        ProdOrderDO sourceOrder = requireProdOrder(sourceWorkOrder.getProdOrderId());
        rework.setId(null);
        rework.setReworkNo(codeService.generate(MesCodeRuleConstants.REWORK, rework.getReworkNo()));
        rework.setWorkOrderId(sourceWorkOrder.getId());
        rework.setProdOrderId(sourceOrder.getId());
        // 未指定返工路线时沿用原工单已锁定的路线，而不是重新按当前生效路线取值。
        if (rework.getRouteId() == null || MesConstants.NONE_ID.equals(rework.getRouteId())) {
            rework.setRouteId(sourceOrder.getRouteId());
            rework.setRouteVersion(sourceOrder.getRouteVersion());
        } else {
            rework.setRouteVersion(snapshotService.requireRouteVersion(rework.getRouteId()));
        }
        rework.setStatus(MesReworkStatusEnum.DRAFT.name());
        rework.setSourceQcTaskId(rework.getSourceQcTaskId() == null
                ? MesConstants.NONE_ID : rework.getSourceQcTaskId());
        reworkMapper.insert(rework);
        return rework.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseRework(MesReworkActionBO bo) {
        ReworkDO rework = requireRework(bo.getReworkId());
        MesCommandReqDTO command = command(bo, rework, MesCommandTypeEnum.RELEASE_REWORK);
        commandService.executeVoid(MesCommandTypeEnum.RELEASE_REWORK, command, () -> {
            String toStatus = MesReworkStateMachine.next(rework.getStatus(), MesReworkStateMachine.ACTION_RELEASE);
            ProdOrderDO order = requireProdOrder(rework.getProdOrderId());
            expandHelper.expandRework(order, rework.getRouteId(), rework.getQty(), reworkRemark(rework));
            reworkMapper.updateById(new ReworkDO().setId(rework.getId()).setStatus(toStatus));
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startRework(MesReworkActionBO bo) {
        ReworkDO rework = requireRework(bo.getReworkId());
        String toStatus = MesReworkStateMachine.next(rework.getStatus(), MesReworkStateMachine.ACTION_START);
        reworkMapper.updateById(new ReworkDO().setId(rework.getId()).setStatus(toStatus));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishRework(MesReworkActionBO bo) {
        ReworkDO rework = requireRework(bo.getReworkId());
        MesCommandReqDTO command = command(bo, rework, MesCommandTypeEnum.FINISH_REWORK);
        commandService.executeVoid(MesCommandTypeEnum.FINISH_REWORK, command, () -> {
            String toStatus = MesReworkStateMachine.next(rework.getStatus(), MesReworkStateMachine.ACTION_FINISH);
            createReinspectionTask(rework);
            reworkMapper.updateById(new ReworkDO().setId(rework.getId()).setStatus(toStatus));
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRework(MesReworkActionBO bo) {
        ReworkDO rework = requireRework(bo.getReworkId());
        String toStatus = MesReworkStateMachine.next(rework.getStatus(), MesReworkStateMachine.ACTION_CANCEL);
        cancelExpandedTasks(rework);
        reworkMapper.updateById(new ReworkDO().setId(rework.getId()).setStatus(toStatus));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeRework(Long reworkId, String idempotencyKey) {
        ReworkDO rework = requireRework(reworkId);
        String toStatus = MesReworkStateMachine.next(rework.getStatus(), MesReworkStateMachine.ACTION_QC_PASS);
        OpTaskDO sourceTask = requireOpTask(rework.getOpTaskId());
        int version = sourceTask.getVersion() == null ? MesConstants.INIT_VERSION : sourceTask.getVersion();
        BigDecimal completedQty = nvl(sourceTask.getCompletedQty()).add(rework.getQty());
        LambdaUpdateWrapper<OpTaskDO> wrapper = new LambdaUpdateWrapper<OpTaskDO>()
                .eq(OpTaskDO::getId, sourceTask.getId())
                .eq(OpTaskDO::getVersion, version);
        if (opTaskMapper.update(OpTaskDO.builder().completedQty(completedQty).version(version + 1).build(), wrapper) == 0) {
            throw exception(OP_TASK_CONCURRENT_CONFLICT);
        }
        reworkMapper.updateById(new ReworkDO().setId(rework.getId()).setStatus(toStatus));
        wipTxnRecorder.record(new MesWipTxnBO()
                .setProdOrderId(rework.getProdOrderId())
                .setWorkOrderId(rework.getWorkOrderId())
                .setOpTaskId(sourceTask.getId())
                .setTxnType(MesWipTxnTypeEnum.REWORK)
                .setQty(rework.getQty())
                .setFromStatus(rework.getStatus())
                .setToStatus(toStatus)
                .setBizType(MesBizTypeEnum.REWORK)
                .setBizNo(rework.getReworkNo())
                .setIdempotencyKey(idempotencyKey)
                .setRemark(rework.getRemark()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRework(Long reworkId, String idempotencyKey) {
        ReworkDO rework = requireRework(reworkId);
        String toStatus = MesReworkStateMachine.next(rework.getStatus(), MesReworkStateMachine.ACTION_QC_REJECT);
        reworkMapper.updateById(new ReworkDO().setId(rework.getId()).setStatus(toStatus));
    }

    private void cancelExpandedTasks(ReworkDO rework) {
        List<WorkOrderDO> workOrders = workOrderMapper.selectListByProdOrderIds(List.of(rework.getProdOrderId())).stream()
                .filter(item -> reworkRemark(rework).equals(item.getRemark())).toList();
        if (workOrders.isEmpty()) {
            return;
        }
        for (OpTaskDO task : opTaskMapper.selectListByWorkOrderIds(workOrders.stream().map(WorkOrderDO::getId).toList())) {
            String toStatus = MesOpTaskStateMachine.next(task.getStatus(), MesOpTaskStateMachine.ACTION_CANCEL);
            opTaskMapper.updateById(new OpTaskDO().setId(task.getId()).setStatus(toStatus));
        }
    }

    private ReworkDO requireRework(Long id) {
        ReworkDO rework = id == null ? null : reworkMapper.selectById(id);
        if (rework == null) {
            throw exception(REWORK_NOT_EXISTS);
        }
        return rework;
    }

    private ProdOrderDO requireProdOrder(Long id) {
        ProdOrderDO order = id == null ? null : prodOrderMapper.selectById(id);
        if (order == null) {
            throw exception(PROD_ORDER_NOT_EXISTS);
        }
        return order;
    }

    private OpTaskDO requireOpTask(Long id) {
        OpTaskDO task = id == null ? null : opTaskMapper.selectById(id);
        if (task == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        requireWorkOrder(task.getWorkOrderId());
        return task;
    }

    private WorkOrderDO requireWorkOrder(Long id) {
        WorkOrderDO workOrder = id == null ? null : workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw exception(WORK_ORDER_NOT_EXISTS);
        }
        return workOrder;
    }

    /**
     * 返工复检仍走 FQC 规则，但业务单据归属返工任务；T2-10 的 FQC Handler 据此辨识并回调
     * {@link #completeRework(Long, String)} 或 {@link #rejectRework(Long, String)}，不能按普通末道工序放行。
     */
    private void createReinspectionTask(ReworkDO rework) {
        ProdOrderDO order = requireProdOrder(rework.getProdOrderId());
        OpTaskDO sourceTask = requireOpTask(rework.getOpTaskId());
        MesCreateQcTaskBO qcTask = new MesCreateQcTaskBO();
        qcTask.setTriggerPoint(MesQcTriggerPointEnum.FQC.name());
        qcTask.setObjectType(MesQcObjectTypeEnum.OP_TASK.name());
        qcTask.setMatId(order.getMatId());
        qcTask.setOpId(sourceTask.getOpId());
        qcTask.setBizType(MesBizTypeEnum.REWORK.name());
        qcTask.setBizNo(rework.getReworkNo());
        qcTask.setBizLineId(rework.getId());
        qcTask.setQty(rework.getQty());
        qcTask.setUomId(order.getUomId());
        qcTask.setRemark("返工复检");
        qcTaskDomainService.createQcTask(qcTask);
    }

    private MesCommandReqDTO command(MesReworkActionBO source, ReworkDO rework, MesCommandTypeEnum type) {
        ReworkCommand command = new ReworkCommand();
        command.setSourceModule(StrUtil.blankToDefault(source.getSourceModule(), SOURCE_MODULE_MES));
        command.setSourceBizNo(StrUtil.blankToDefault(source.getSourceBizNo(), rework.getReworkNo()));
        command.setSourceBizLineNo(source.getSourceBizLineNo());
        command.setIdempotencyKey(StrUtil.blankToDefault(source.getIdempotencyKey(),
                String.join(KEY_SEPARATOR, SOURCE_MODULE_MES, rework.getReworkNo(), type.name(), "0")));
        return command;
    }

    private String reworkRemark(ReworkDO rework) {
        return REWORK_REMARK_PREFIX + rework.getReworkNo();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? MesConstants.ZERO_QTY : value;
    }

    private static class ReworkCommand extends MesCommandReqDTO {
    }
}
