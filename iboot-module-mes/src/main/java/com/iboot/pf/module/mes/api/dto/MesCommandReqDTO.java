package com.iboot.pf.module.mes.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** MES 跨模块命令公共字段。 */
@Data
public abstract class MesCommandReqDTO {

    /** 全局唯一幂等键。 */
    @NotBlank(message = "幂等键不能为空")
    @Schema(description = "全局唯一幂等键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String idempotencyKey;

    /** 调用方模块：MES、APS、INTEGRATION、UI。 */
    @NotBlank(message = "来源模块不能为空")
    @Schema(description = "来源模块", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceModule;

    /** 调用方业务单号。 */
    @NotBlank(message = "来源业务单号不能为空")
    @Schema(description = "来源业务单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceBizNo;

    /** 调用方业务行号。 */
    @Schema(description = "来源业务行号")
    private String sourceBizLineNo;
}
