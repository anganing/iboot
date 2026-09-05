package com.iboot.pf.module.mes.api.report;

import com.iboot.pf.module.mes.api.report.dto.ProdReportReqDTO;
import com.iboot.pf.module.mes.api.report.dto.ProdReportRespDTO;
import com.iboot.pf.module.mes.api.report.dto.WipTxnRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 生产报工与在制流水 API 门面。
 *
 * <p>报工事实写在 MES，成品的在库事实由 WMS 维护，两者靠完工入库单串起来。
 */
public interface ProdReportApi {

    /**
     * 提交报工并推进工序、作业工单、生产工单三级进度。
     *
     * @param reqDTO 报工命令
     * @return 报工记录事实
     */
    ProdReportRespDTO reportProd(ProdReportReqDTO reqDTO);

    /**
     * 按生产工单批量查询在制流水。
     *
     * @param prodOrderIds 生产工单编号集合
     * @return 在制流水列表
     */
    List<WipTxnRespDTO> getWipTxnList(Collection<Long> prodOrderIds);

}
