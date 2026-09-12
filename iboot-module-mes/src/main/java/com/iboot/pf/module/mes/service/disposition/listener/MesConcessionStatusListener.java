package com.iboot.pf.module.mes.service.disposition.listener;

import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.iboot.pf.module.mes.service.disposition.MesDispositionDomainService;
import com.iboot.pf.module.mes.service.disposition.MesDispositionDomainServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 让步接收审批状态监听器。
 *
 * <p>只做转发：三层幂等与审批通过后的处置执行都在
 * {@link MesDispositionDomainService#updateConcessionApprovalStatus} 里（与 HRM 请假的
 * {@code HrmAttendanceLeaveStatusListener} 同构，监听器不持有业务 Mapper）。
 *
 * <p>{@code onEvent} 内 catch 全部异常且不向外抛——它跑在 Flowable 的事务里，抛出去会回滚整个
 * 流程状态变更，表现为"审批人点了没反应"。
 */
@Slf4j
@Component
public class MesConcessionStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private MesDispositionDomainService dispositionDomainService;

    @Override
    protected String getProcessDefinitionKey() {
        return MesDispositionDomainServiceImpl.PROCESS_KEY_CONCESSION;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        try {
            dispositionDomainService.updateConcessionApprovalStatus(Long.valueOf(event.getBusinessKey()),
                    event.getId(), event.getStatus(), event.getReason());
        } catch (Exception e) {
            log.error("[onEvent][让步接收审批回调异常。businessKey={}, instanceId={}, status={}]",
                    event.getBusinessKey(), event.getId(), event.getStatus(), e);
        }
    }

}
