package com.iboot.pf.module.mes.controller.admin.qcplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 检验方案动作请求，发布、停用共用。
 *
 * <p><b>目标状态不由前端指定</b>：动作名决定目标状态，能不能迁移过去由当前状态校验裁决。
 * 请求里带目标状态就等于把状态校验变成建议，空方案可以直接标成已发布。
 *
 * <p>{@code idempotencyKey} 允许为空，留空由领域 Service 按 {@code UI:{方案编码}:{命令类型}:0} 派生。
 */
@Schema(description = "管理后台 - MES-检验方案动作 Request VO")
@Data
public class QcPlanActionReqVO {

    @Schema(description = "检验方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检验方案编号不能为空")
    private Long id;

    @Schema(description = "幂等键，留空由服务端按方案编码与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

}
