package com.iboot.pf.module.mes.service.disposition.handler;

import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesDispositionTypeEnum;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.QualityChangeReqDTO;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/** 让步接收：把待检/不合格库存迁移为合格。 */
@Component
public class ConcessionHandler implements MesDispositionHandler {

    @Resource
    private DispositionInventoryResolver resolver;
    @Resource
    private InventoryApi inventoryApi;

    @Override
    public String type() {
        return MesDispositionTypeEnum.CONCESSION.name();
    }

    @Override
    public DispositionResult execute(DispositionDO disposition) {
        DispositionInventoryResolver.ResolvedInventory resolved = resolver.resolve(disposition);
        var balance = resolved.balance();
        QualityChangeReqDTO req = new QualityChangeReqDTO();
        req.setIdempotencyKey("MES:" + disposition.getDispositionNo() + ":CONCESSION:0");
        req.setSourceModule("MES");
        req.setSourceBizNo(disposition.getDispositionNo());
        req.setInvOrgId(balance.getInvOrgId());
        req.setPlantId(balance.getPlantId());
        req.setWhId(balance.getWhId());
        req.setLocId(balance.getLocId());
        req.setMatId(balance.getMatId());
        req.setBatchId(balance.getBatchId());
        req.setContainerId(balance.getContainerId());
        req.setProjectId(balance.getProjectId());
        req.setStockScope(balance.getStockScope());
        req.setMatCode(balance.getMatCode());
        req.setMatName(balance.getMatName());
        req.setUomCode(balance.getUomCode());
        req.setLotNo(balance.getLotNo());
        req.setLocCode(balance.getLocCode());
        req.setQualityStatusFrom(balance.getQualityStatus());
        req.setQualityStatusTo(WmsQualityStatusEnum.QUALIFIED.name());
        req.setQty(disposition.getQty());
        req.setRemark(disposition.getRemark());
        Long txnId = inventoryApi.changeQualityStatus(req);
        return new DispositionResult(MesBizTypeEnum.INV_TXN.name(), String.valueOf(txnId));
    }
}
