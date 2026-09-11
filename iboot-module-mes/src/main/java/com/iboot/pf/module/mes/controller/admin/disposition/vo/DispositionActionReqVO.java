package com.iboot.pf.module.mes.controller.admin.disposition.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 处置单动作请求。 */
@Schema(description = "管理后台 - MES-处置单动作 Request VO")
@Data
public class DispositionActionReqVO {

    @Schema(description = "处置单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "处置单编号不能为空")
    private Long id;

    @Schema(description = "幂等键，留空由服务端按处置单号与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "动作原因或备注")
    @Size(max = 500, message = "动作原因长度不能超过 500")
    private String reason;
}
