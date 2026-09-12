package com.iboot.pf.module.mes.service.disposition.listener;

import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.iboot.pf.module.mes.service.disposition.MesDispositionDomainService;
import com.iboot.pf.module.mes.service.disposition.MesDispositionDomainServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 报废处置审批状态监听器。
 *
 * <p>只做转发：三层幂等与审批通过后的报废出库过账都在
 * {@link MesDispositionDomainService#updateScrapApprovalStatus} 里。审批期间不产生任何库存事实。
 *
 * <p>{@code onEvent} 内 catch 全部异常且不向外抛（跑在 Flowable 事务内，抛了会回滚流程终态）。
 */
@Slf4j
@Component
public class MesScrapStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private MesDispositionDomainService dispositionDomainService;

    @Override
    protected String getProcessDefinitionKey() {
        return MesDispositionDomainServiceImpl.PROCESS_KEY_SCRAP;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        try {
            dispositionDomainService.updateScrapApprovalStatus(Long.valueOf(event.getBusinessKey()),
                    event.getId(), event.getStatus(), event.getReason());
        } catch (Exception e) {
            log.error("[onEvent][报废审批回调异常。businessKey={}, instanceId={}, status={}]",
                    event.getBusinessKey(), event.getId(), event.getStatus(), e);
        }
    }

}
