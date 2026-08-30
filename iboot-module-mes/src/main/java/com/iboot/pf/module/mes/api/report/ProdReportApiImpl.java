package com.iboot.pf.module.mes.api.report;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.dto.MesCommandDTOs.FinishReq;
import com.iboot.pf.module.mes.api.dto.MesRespDTO;
import com.iboot.pf.module.mes.service.MesApiService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.util.Collection;
import java.util.List;

/** 生产报工 API 实现。报工事实写入 MES，在库事实由 WMS 维护。 */
@Service @Validated
public class ProdReportApiImpl implements ProdReportApi {
    @Resource private MesApiService service;
    @Override public Long reportProd(FinishReq reqDTO) { return service.create("report", reqDTO); }
    @Override @DataPermission(enable = false) public List<MesRespDTO> getWipTxnList(Collection<Long> ids) { return service.listBy("wip-txn", "prodOrderId", ids); }
}
