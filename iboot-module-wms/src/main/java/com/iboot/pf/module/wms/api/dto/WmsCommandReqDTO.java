package com.iboot.pf.module.wms.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * WMS 跨模块命令 DTO 基类。
 *
 * <p>所有写操作都必须带幂等键：实现方先按 {@code idempotencyKey} 查询是否已受理，
 * 已受理则直接返回原结果，不重复执行。
 */
@Data
public abstract class WmsCommandReqDTO {

    /** 幂等键，全局唯一，建议格式：sourceModule:sourceBizNo:action:sourceBizLineNo。 */
    @Schema(description = "幂等键，建议格式 sourceModule:sourceBizNo:action:sourceBizLineNo", requiredMode = Schema.RequiredMode.REQUIRED, example = "MES:MO20260830001:RESERVE:10")
    @NotBlank(message = "幂等键不能为空")
    private String idempotencyKey;

    /** 调用方模块：MES、APS、INTEGRATION、UI。 */
    @Schema(description = "调用方模块", requiredMode = Schema.RequiredMode.REQUIRED, example = "MES")
    @NotBlank(message = "调用方模块不能为空")
    private String sourceModule;

    /** 调用方业务单号。 */
    @Schema(description = "调用方业务单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "MO20260830001")
    @NotBlank(message = "调用方业务单号不能为空")
    private String sourceBizNo;

    /** 行级业务号，有明细时必填。 */
    @Schema(description = "行级业务号，有明细时必填", example = "10")
    private String sourceBizLineNo;

}
