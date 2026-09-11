package com.iboot.pf.module.mes.service.disposition.handler;

import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.enums.MesDispositionTypeEnum;
import com.iboot.pf.module.wms.enums.WmsOutBizTypeEnum;
import org.springframework.stereotype.Component;

/** 报废处置：生成报废出库单并过账。 */
@Component
public class ScrapHandler extends AbstractOutDispositionHandler {

    @Override
    public String type() {
        return MesDispositionTypeEnum.SCRAP.name();
    }

    @Override
    public DispositionResult execute(DispositionDO disposition) {
        return executeOut(disposition, WmsOutBizTypeEnum.SCRAP_OUT.name());
    }
}
