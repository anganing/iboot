package com.iboot.pf.module.mes.api.report;

import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.FinishReq;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;
import java.util.Collection;
import java.util.List;

/** 生产报工与在制流水 API 门面。 */
public interface ProdReportApi {
    Long reportProd(FinishReq reqDTO);
    List<MesRespDTO> getWipTxnList(Collection<Long> prodOrderIds);
}
