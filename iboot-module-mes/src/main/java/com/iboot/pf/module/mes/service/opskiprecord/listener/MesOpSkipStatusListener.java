package com.iboot.pf.module.mes.service.opskiprecord.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.iboot.pf.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.iboot.pf.module.mes.dal.dataobject.opskiprecord.OpSkipRecordDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.mysql.opskiprecord.OpSkipRecordMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStateMachine;
import com.iboot.pf.module.mes.enums.MesSkipApproveStatusEnum;
import com.iboot.pf.module.mes.enums.MesSkipRecordStatusEnum;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import com.iboot.pf.module.mes.service.wiptxn.bo.MesWipTxnBO;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.service.opskiprecord.MesOpSkipDomainServiceImpl;
import com.iboot.pf.module.mes.service.optask.MesOpTaskDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 工序跳序审批状态监听器。
 *
 * <p>审批期间工序任务状态一行不动；APPROVE 后走与 ALLOW_WITH_REASON 相同的生效逻辑。
 */
@Slf4j
@Component
public class MesOpSkipStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OpSkipRecordMapper opSkipRecordMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private MesWipTxnRecorder wipTxnRecorder;
    @Resource
    private MesOpTaskDomainService opTaskDomainService;

    @Override
    protected String getProcessDefinitionKey() {
        return MesOpSkipDomainServiceImpl.PROCESS_KEY_OP_SKIP;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        try {
            updateSkipApprovalStatus(event.getBusinessKey(), event.getId(),
                    event.getStatus(), event.getReason());
        } catch (Exception e) {
            log.error("[onEvent] 跳序审批回调异常。businessKey={}, instanceId={}, status={}",
                    event.getBusinessKey(), event.getId(), event.getStatus(), e);
        }
    }

    void updateSkipApprovalStatus(String businessKey, String instanceId,
                                  Integer status, String reason) {
        if (!BpmProcessInstanceStatusEnum.isProcessEndStatus(status)) {
            return;
        }
        Long recordId = Long.valueOf(businessKey);
        OpSkipRecordDO record = opSkipRecordMapper.selectById(recordId);
        if (record == null || !instanceId.equals(record.getProcessInstanceId())) {
            return;
        }
        boolean approved = BpmProcessInstanceStatusEnum.APPROVE.getStatus().equals(status);
        String recordStatus = approved
                ? MesSkipRecordStatusEnum.EFFECTIVE.name()
                : MesSkipRecordStatusEnum.REVOKED.name();
        String approveStatus = approved
                ? MesSkipApproveStatusEnum.APPROVED.name()
                : MesSkipApproveStatusEnum.REJECTED.name();
        int updated = opSkipRecordMapper.update(
                new OpSkipRecordDO()
                        .setId(recordId)
                        .setStatus(recordStatus)
                        .setApproveStatus(approveStatus)
                        .setApprovalStatus(status)
                        .setApprovalTime(LocalDateTime.now())
                        .setApprovalReason(reason),
                new LambdaUpdateWrapper<OpSkipRecordDO>()
                        .eq(OpSkipRecordDO::getId, recordId)
                        .eq(OpSkipRecordDO::getStatus, MesSkipRecordStatusEnum.PENDING.name())
                        .eq(OpSkipRecordDO::getApprovalStatus,
                                BpmProcessInstanceStatusEnum.RUNNING.getStatus()));
        if (updated != 1) {
            return;
        }
        if (!approved) {
            log.info("[updateSkipApprovalStatus] 跳序审批驳回，工序保持原状。recordId={}", recordId);
            return;
        }
        if (approveSkip(record)) {
            opTaskDomainService.completeApprovedSkip(record.getOpTaskId(),
                    "BPM:" + record.getId() + ":OP_SKIP_APPROVE:0");
        }
    }

    /** 审批通过后走与 ALLOW_WITH_REASON 相同的生效逻辑：工序推 SKIPPED、写 SKIP 流水。 */
    private boolean approveSkip(OpSkipRecordDO record) {
        OpTaskDO task = opTaskMapper.selectById(record.getOpTaskId());
        if (task == null) {
            return false;
        }
        WorkOrderDO workOrder = workOrderMapper.selectById(task.getWorkOrderId());
        String fromStatus = task.getStatus();
        String toStatus = MesOpTaskStateMachine.next(fromStatus, MesOpTaskStateMachine.ACTION_SKIP);
        int currentVersion = task.getVersion() == null ? MesConstants.INIT_VERSION : task.getVersion();
        if (opTaskMapper.update(OpTaskDO.builder()
                .status(toStatus).version(currentVersion + 1).build(),
                new LambdaUpdateWrapper<OpTaskDO>()
                        .eq(OpTaskDO::getId, task.getId())
                        .eq(OpTaskDO::getVersion, currentVersion)) == 0) {
            log.warn("[approveSkip] 工序任务已被并发修改，跳过生效。recordId={}", record.getId());
            return false;
        }
        task.setStatus(toStatus).setVersion(currentVersion + 1);
        wipTxnRecorder.record(new MesWipTxnBO()
                .setProdOrderId(workOrder == null ? MesConstants.NONE_ID : workOrder.getProdOrderId())
                .setWorkOrderId(task.getWorkOrderId())
                .setOpTaskId(task.getId())
                .setTxnType(MesWipTxnTypeEnum.SKIP)
                .setQty(MesConstants.ZERO_QTY)
                .setFromStatus(fromStatus)
                .setToStatus(toStatus)
                .setBizType(MesBizTypeEnum.OP_TASK)
                .setBizNo(task.getTaskNo())
                .setIdempotencyKey("BPM:" + record.getId() + ":OP_SKIP_APPROVE:0")
                .setRemark("审批通过跳序"));
        log.info("[approveSkip] 跳序审批通过并生效。recordId={}, taskNo={}, {} -> {}",
                record.getId(), task.getTaskNo(), fromStatus, toStatus);
        return true;
    }
}
