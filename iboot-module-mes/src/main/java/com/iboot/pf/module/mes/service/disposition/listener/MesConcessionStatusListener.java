package com.iboot.pf.module.mes.service.disposition.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.iboot.pf.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.dal.mysql.disposition.DispositionMapper;
import com.iboot.pf.module.mes.enums.MesDispositionStatusEnum;
import com.iboot.pf.module.mes.service.disposition.MesDispositionDomainService;
import com.iboot.pf.module.mes.service.disposition.MesDispositionDomainServiceImpl;
import com.iboot.pf.module.mes.service.disposition.bo.MesDispositionActionBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 让步接收审批状态监听器。
 *
 * <p>审批期间处置单不执行任何联动；APPROVE 后由回调统一触发 executeDisposition。
 * onEvent 内 catch 全部异常不向外抛（跑在 Flowable 事务内，抛了会回滚流程状态变更）。
 */
@Slf4j
@Component
public class MesConcessionStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private DispositionMapper dispositionMapper;
    @Resource
    private MesDispositionDomainService dispositionDomainService;

    @Override
    protected String getProcessDefinitionKey() {
        return MesDispositionDomainServiceImpl.PROCESS_KEY_CONCESSION;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        try {
            updateConcessionApprovalStatus(event.getBusinessKey(), event.getId(),
                    event.getStatus(), event.getReason());
        } catch (Exception e) {
            log.error("[onEvent] 让步接收审批回调异常。businessKey={}, instanceId={}, status={}",
                    event.getBusinessKey(), event.getId(), event.getStatus(), e);
        }
    }

    void updateConcessionApprovalStatus(String businessKey, String instanceId,
                                        Integer status, String reason) {
        // 第一层：非终态不处理（审批中不算）
        if (!BpmProcessInstanceStatusEnum.isProcessEndStatus(status)) {
            return;
        }
        Long dispositionId = Long.valueOf(businessKey);
        DispositionDO disposition = dispositionMapper.selectById(dispositionId);
        if (disposition == null) {
            return;
        }
        // 第二层：实例编号一致且当前处于审批中
        if (!instanceId.equals(disposition.getProcessInstanceId())
                || !BpmProcessInstanceStatusEnum.RUNNING.getStatus().equals(disposition.getApprovalStatus())) {
            return;
        }
        // 第三层：带 approval_status = RUNNING 条件的 UPDATE，受影响行数不为 1 则已被处理
        boolean approved = BpmProcessInstanceStatusEnum.APPROVE.getStatus().equals(status);
        String targetStatus = approved
                ? MesDispositionStatusEnum.APPROVED.name()
                : MesDispositionStatusEnum.DRAFT.name();
        int updated = dispositionMapper.update(
                new DispositionDO()
                        .setId(dispositionId)
                        .setStatus(targetStatus)
                        .setApprovalStatus(status)
                        .setApprovalTime(LocalDateTime.now())
                        .setApprovalReason(reason),
                new LambdaUpdateWrapper<DispositionDO>()
                        .eq(DispositionDO::getId, dispositionId)
                        .eq(DispositionDO::getApprovalStatus,
                                BpmProcessInstanceStatusEnum.RUNNING.getStatus()));
        if (updated != 1) {
            return;
        }
        log.info("[updateConcessionApprovalStatus] 让步接收审批回调。dispositionNo={}, status={}, approved={}",
                disposition.getDispositionNo(), status, approved);
        if (approved) {
            MesDispositionActionBO action = new MesDispositionActionBO();
            action.setDispositionId(dispositionId);
            action.setSourceModule("BPM");
            dispositionDomainService.executeDisposition(action);
        }
    }
}
