package com.iboot.pf.module.wms.controller.admin.countorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提交实盘结果请求，一次提交多行。
 *
 * <p>按行提交而不是一行一个请求：现场盘完一个库区是一次录入，逐行调接口会让
 * "部分行提交成功、单据状态已推进到盘点中、剩余行还没录"变成常态。
 */
@Schema(description = "管理后台 - WMS-盘点单提交实盘结果 Request VO")
@Data
public class CountOrderResultSubmitReqVO {

    @Schema(description = "盘点单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "盘点单编号不能为空")
    private Long id;

    @Schema(description = "幂等键，留空由服务端按单据编号与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "实盘结果", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "实盘结果不能为空")
    @Valid
    private List<LineResult> results;

    /** 实盘结果，字段名与 {@code WmsSubmitCountResultBO.LineResult} 对齐。 */
    @Schema(description = "管理后台 - WMS-盘点单实盘结果行")
    @Data
    public static class LineResult {

        @Schema(description = "盘点明细编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "盘点明细编号不能为空")
        private Long lineId;

        @Schema(description = "实盘数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        @NotNull(message = "实盘数量不能为空")
        @DecimalMin(value = "0", message = "实盘数量不能为负数")
        private BigDecimal actualQty;

        @Schema(description = "备注")
        @Size(max = 500, message = "备注长度不能超过 500")
        private String remark;

    }

}
