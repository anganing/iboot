package com.iboot.pf.module.mes.service.disposition.handler;

import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.enums.MesDispositionTypeEnum;
import com.iboot.pf.module.wms.enums.WmsOutBizTypeEnum;
import org.springframework.stereotype.Component;

/** 退供处置：生成采购退货出库单并过账。 */
@Component
public class ReturnSupplierHandler extends AbstractOutDispositionHandler {

    @Override
    public String type() {
        return MesDispositionTypeEnum.RETURN_SUPPLIER.name();
    }

    @Override
    public DispositionResult execute(DispositionDO disposition) {
        return executeOut(disposition, WmsOutBizTypeEnum.PO_RETURN_OUT.name());
    }
}
