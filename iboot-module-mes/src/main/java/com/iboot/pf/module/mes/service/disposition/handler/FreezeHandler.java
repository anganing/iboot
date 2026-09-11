package com.iboot.pf.module.mes.service.disposition.handler;

import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesDispositionTypeEnum;
import com.iboot.pf.module.wms.api.lock.InventoryLockApi;
import com.iboot.pf.module.wms.api.lock.dto.CreateLockReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/** 冻结处置：生成真实库存冻结记录。 */
@Component
public class FreezeHandler implements MesDispositionHandler {

    @Resource
    private DispositionInventoryResolver resolver;
    @Resource
    private InventoryLockApi lockApi;

    @Override
    public String type() {
        return MesDispositionTypeEnum.FREEZE.name();
    }

    @Override
    public DispositionResult execute(DispositionDO disposition) {
        var balance = resolver.resolve(disposition).balance();
        CreateLockReqDTO req = new CreateLockReqDTO();
        req.setIdempotencyKey("MES:" + disposition.getDispositionNo() + ":FREEZE:0");
        req.setSourceModule("MES");
        req.setSourceBizNo(disposition.getDispositionNo());
        req.setBizType("QC_HOLD");
        req.setInvOrgId(balance.getInvOrgId());
        req.setWhId(balance.getWhId());
        req.setLocId(balance.getLocId());
        req.setMatId(balance.getMatId());
        req.setBatchId(balance.getBatchId());
        req.setContainerId(balance.getContainerId());
        req.setProjectId(balance.getProjectId());
        req.setQualityStatus(balance.getQualityStatus());
        req.setQty(disposition.getQty());
        req.setReason(disposition.getRemark() == null ? "质检不合格冻结" : disposition.getRemark());
        req.setRemark(disposition.getRemark());
        Long lockId = lockApi.lockInventory(req);
        String lockNo = lockApi.getLock(lockId).getLockNo();
        return new DispositionResult(MesBizTypeEnum.INV_LOCK.name(), lockNo);
    }
}
