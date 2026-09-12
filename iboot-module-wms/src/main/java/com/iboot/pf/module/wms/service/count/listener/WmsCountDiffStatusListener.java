package com.iboot.pf.module.wms.service.count.listener;

import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.iboot.pf.module.wms.service.count.WmsCountOrderService;
import com.iboot.pf.module.wms.service.count.WmsCountOrderServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 盘点差异审批状态监听器。
 *
 * <p>只做转发：三层幂等、审批通过后的逐行过账、驳回后退回重盘都在
 * {@link WmsCountOrderService#updateCountDiffApprovalStatus} 里（与 HRM 请假的
 * {@code HrmAttendanceLeaveStatusListener} 同构，监听器不持有业务 Mapper）。
 *
 * <p>{@code onEvent} 内 catch 全部异常且不向外抛——它跑在 Flowable 的事务里，抛出去会回滚整个
 * 流程状态变更。
 */
@Slf4j
@Component
public class WmsCountDiffStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private WmsCountOrderService countOrderService;

    @Override
    protected String getProcessDefinitionKey() {
        return WmsCountOrderServiceImpl.PROCESS_KEY_COUNT_DIFF;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        try {
            countOrderService.updateCountDiffApprovalStatus(Long.valueOf(event.getBusinessKey()),
                    event.getId(), event.getStatus(), event.getReason());
        } catch (Exception e) {
            log.error("[onEvent][盘点差异审批回调异常。businessKey={}, instanceId={}, status={}]",
                    event.getBusinessKey(), event.getId(), event.getStatus(), e);
        }
    }

}
