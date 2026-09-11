package com.iboot.pf.module.mes.service.materialreq.listener;

import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.iboot.pf.module.mes.service.materialreq.MesMaterialReqDomainService;
import com.iboot.pf.module.mes.service.materialreq.MesMaterialReqDomainServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 超发领料审批状态监听器。 */
@Slf4j
@Component
public class MesSupplementStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private MesMaterialReqDomainService materialReqDomainService;

    @Override
    protected String getProcessDefinitionKey() {
        return MesMaterialReqDomainServiceImpl.PROCESS_KEY_SUPPLEMENT;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        try {
            materialReqDomainService.updateSupplementApprovalStatus(Long.valueOf(event.getBusinessKey()),
                    event.getId(), event.getStatus(), event.getReason());
        } catch (Exception e) {
            // 回调运行在 Flowable 事务内，异常只能记录，不能反抛并回滚流程终态。
            log.error("[onEvent] 超发领料审批回调异常。businessKey={}, instanceId={}, status={}",
                    event.getBusinessKey(), event.getId(), event.getStatus(), e);
        }
    }
}
