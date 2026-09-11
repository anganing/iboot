package com.iboot.pf.module.wms.service.count.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.iboot.pf.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.iboot.pf.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.iboot.pf.module.wms.dal.dataobject.count.WmsCountOrderDO;
import com.iboot.pf.module.wms.dal.mysql.count.WmsCountOrderMapper;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.service.count.bo.WmsConfirmCountDiffBO;
import com.iboot.pf.module.wms.service.count.WmsCountOrderService;
import com.iboot.pf.module.wms.service.count.WmsCountOrderServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 盘点差异审批状态监听器。
 *
 * <p>审批通过后逐行确认差异（盘盈入库/盘亏出库）；驳回后单据回 COUNTING 要求重盘。
 */
@Slf4j
@Component
public class WmsCountDiffStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private WmsCountOrderMapper wmsCountOrderMapper;
    @Resource
    private WmsCountOrderService countOrderService;

    @Override
    protected String getProcessDefinitionKey() {
        return WmsCountOrderServiceImpl.PROCESS_KEY_COUNT_DIFF;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        try {
            updateCountDiffApprovalStatus(event.getBusinessKey(), event.getId(),
                    event.getStatus(), event.getReason());
        } catch (Exception e) {
            log.error("[onEvent] 盘点差异审批回调异常。businessKey={}, instanceId={}, status={}",
                    event.getBusinessKey(), event.getId(), event.getStatus(), e);
        }
    }

    void updateCountDiffApprovalStatus(String businessKey, String instanceId,
                                       Integer status, String reason) {
        if (!BpmProcessInstanceStatusEnum.isProcessEndStatus(status)) {
            return;
        }
        Long countOrderId = Long.valueOf(businessKey);
        WmsCountOrderDO order = wmsCountOrderMapper.selectById(countOrderId);
        if (order == null || !instanceId.equals(order.getProcessInstanceId())
                || !BpmProcessInstanceStatusEnum.RUNNING.getStatus().equals(order.getApprovalStatus())) {
            return;
        }
        boolean approved = BpmProcessInstanceStatusEnum.APPROVE.getStatus().equals(status);
        int updated = wmsCountOrderMapper.update(
                new WmsCountOrderDO()
                        .setId(countOrderId)
                        .setApprovalStatus(status)
                        .setApprovalTime(LocalDateTime.now())
                        .setApprovalReason(reason),
                new LambdaUpdateWrapper<WmsCountOrderDO>()
                        .eq(WmsCountOrderDO::getId, countOrderId)
                        .eq(WmsCountOrderDO::getApprovalStatus,
                                BpmProcessInstanceStatusEnum.RUNNING.getStatus()));
        if (updated != 1) {
            return;
        }
        log.info("[updateCountDiffApprovalStatus] 盘点差异审批回调。countNo={}, status={}, approved={}",
                order.getCountNo(), status, approved);
        if (approved) {
            // Service 再按行号派生为 WMS:{countNo}:COUNT_DIFF_POST:{lineNo}，用于逐行调整单过账。
            countOrderService.confirmCountDiff(new WmsConfirmCountDiffBO()
                    .setOrderId(countOrderId)
                    .setSourceModule("WMS")
                    .setSourceBizNo(order.getCountNo())
                    .setIdempotencyKey("WMS:" + order.getCountNo() + ":COUNT_DIFF_POST"));
        } else {
            // 驳回后单据回 COUNTING 要求重盘
            wmsCountOrderMapper.updateById(new WmsCountOrderDO()
                    .setId(countOrderId)
                    .setStatus(WmsOrderStatusEnum.COUNTING.name()));
        }
    }
}
