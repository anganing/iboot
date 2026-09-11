package com.iboot.pf.module.wms.api.arrivalorder;

import com.iboot.pf.module.wms.api.arrivalorder.dto.CompleteIqcReqDTO;

/** 到货登记跨模块门面。 */
public interface ArrivalOrderApi {

    /** 回写 IQC 规则解析结果；命中时挂任务，未命中时直接放行。 */
    void resolveIqcTask(Long arrivalLineId, Long qcTaskId);

    /** 按 IQC 结论回写到货明细，并在先入待检模式下完成质量状态迁移。 */
    void completeIqc(CompleteIqcReqDTO reqDTO);
}
