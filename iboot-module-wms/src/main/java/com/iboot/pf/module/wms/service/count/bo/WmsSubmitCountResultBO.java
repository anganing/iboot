package com.iboot.pf.module.wms.service.count.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 提交实盘结果的内部参数。 */
@Data
public class WmsSubmitCountResultBO {

    /** 命令幂等键，格式：sourceModule:sourceBizNo:action:lineNo。 */
    private String idempotencyKey;

    /** 调用方模块：MES、APS、INTEGRATION、UI。 */
    private String sourceModule;

    /** 调用方业务单号。 */
    private String sourceBizNo;

    /** 盘点单编号。 */
    private Long orderId;

    /** 盘点单号，与 orderId 二选一。 */
    private String countNo;

    /** 实盘结果。 */
    private List<LineResult> results;

    /** 实盘结果。 */
    @Data
    public static class LineResult {

        /** 盘点明细编号。 */
        private Long lineId;

        /** 行号，与 lineId 二选一。 */
        private Integer lineNo;

        /** 实盘数量。 */
        private BigDecimal actualQty;

        /** 备注。 */
        private String remark;

    }

}
