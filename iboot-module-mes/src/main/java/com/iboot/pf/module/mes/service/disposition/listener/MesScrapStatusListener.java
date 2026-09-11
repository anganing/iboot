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
 * 报废处置审批状态监听器。
 *
 * <p>审批期间不产生任何库存事实；APPROVE 后由回调触发 executeDisposition（ScrapHandler 做报废出库过账）。
 */
@Slf4j
@Component
public class MesScrapStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private DispositionMapper dispositionMapper;
    @Resource
    private MesDispositionDomainService dispositionDomainService;

    @Override
    protected String getProcessDefinitionKey() {
        return MesDispositionDomainServiceImpl.PROCESS_KEY_SCRAP;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        try {
            updateScrapApprovalStatus(event.getBusinessKey(), event.getId(),
                    event.getStatus(), event.getReason());
        } catch (Exception e) {
            log.error("[onEvent] 报废审批回调异常。businessKey={}, instanceId={}, status={}",
                    event.getBusinessKey(), event.getId(), event.getStatus(), e);
        }
    }

    void updateScrapApprovalStatus(String businessKey, String instanceId,
                                   Integer status, String reason) {
        if (!BpmProcessInstanceStatusEnum.isProcessEndStatus(status)) {
            return;
        }
        Long dispositionId = Long.valueOf(businessKey);
        DispositionDO disposition = dispositionMapper.selectById(dispositionId);
        if (disposition == null || !instanceId.equals(disposition.getProcessInstanceId())
                || !BpmProcessInstanceStatusEnum.RUNNING.getStatus().equals(disposition.getApprovalStatus())) {
            return;
        }
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
        log.info("[updateScrapApprovalStatus] 报废审批回调。dispositionNo={}, status={}, approved={}",
                disposition.getDispositionNo(), status, approved);
        if (approved) {
            MesDispositionActionBO action = new MesDispositionActionBO();
            action.setDispositionId(dispositionId);
            action.setSourceModule("BPM");
            dispositionDomainService.executeDisposition(action);
        }
    }
}
