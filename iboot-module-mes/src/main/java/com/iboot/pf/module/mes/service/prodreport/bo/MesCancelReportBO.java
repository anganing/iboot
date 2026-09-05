package com.iboot.pf.module.mes.service.prodreport.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报工取消参数。
 *
 * <p>取消是状态推进型动作（{@code SUBMITTED} -&gt; {@code CANCELLED}），报工记录上的
 * {@code idempotency_key} 已经被提交那次占用，因此这一步走命令幂等。幂等键留空时由服务端按
 * {@code {来源模块}:{报工单号}:CANCEL_PROD_REPORT:0} 派生。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesCancelReportBO extends MesCommandReqDTO {

    /** 报工记录编号，必填。 */
    private Long prodReportId;

    /** 取消原因，写进反向流水的 remark。报错数量这类现场失误必须留痕，否则事后对不上账。 */
    private String reason;

}
