package com.iboot.pf.module.mes.service.opskiprecord.listener;

import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.iboot.pf.module.mes.service.opskiprecord.MesOpSkipDomainService;
import com.iboot.pf.module.mes.service.opskiprecord.MesOpSkipDomainServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 工序跳序审批状态监听器。
 *
 * <p>只做转发：三层幂等、审批通过后的生效动作（工序推 {@code SKIPPED}、写流水、后序推进）都在
 * {@link MesOpSkipDomainService#updateSkipApprovalStatus} 里（与 HRM 请假的
 * {@code HrmAttendanceLeaveStatusListener} 同构，监听器不持有业务 Mapper）。
 *
 * <p>{@code onEvent} 内 catch 全部异常且不向外抛——它跑在 Flowable 的事务里，抛出去会回滚整个
 * 流程状态变更。
 */
@Slf4j
@Component
public class MesOpSkipStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private MesOpSkipDomainService opSkipDomainService;

    @Override
    protected String getProcessDefinitionKey() {
        return MesOpSkipDomainServiceImpl.PROCESS_KEY_OP_SKIP;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        try {
            opSkipDomainService.updateSkipApprovalStatus(Long.valueOf(event.getBusinessKey()),
                    event.getId(), event.getStatus(), event.getReason());
        } catch (Exception e) {
            log.error("[onEvent][跳序审批回调异常。businessKey={}, instanceId={}, status={}]",
                    event.getBusinessKey(), event.getId(), event.getStatus(), e);
        }
    }

}
