package com.iboot.pf.module.mes.api.report;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.report.dto.ProdReportReqDTO;
import com.iboot.pf.module.mes.api.report.dto.ProdReportRespDTO;
import com.iboot.pf.module.mes.api.report.dto.WipTxnRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/** 生产报工 API 实现。报工事实写入 MES，在库事实由 WMS 维护。 */
@Service
@Validated
public class ProdReportApiImpl implements ProdReportApi {

    @Override
    public ProdReportRespDTO reportProd(ProdReportReqDTO reqDTO) {
        throw new UnsupportedOperationException("生产报工命令尚未接入领域规则");
    }

    @Override
    @DataPermission(enable = false)
    public List<WipTxnRespDTO> getWipTxnList(Collection<Long> prodOrderIds) {
        throw new UnsupportedOperationException("在制流水查询尚未接入领域 Service");
    }

}
