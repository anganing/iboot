package com.iboot.pf.module.mes.controller.admin.prodorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 生产工单动作请求，挂起、恢复、取消共用，也是关闭请求的父类。
 *
 * <p><b>目标状态不由前端指定</b>：动作名决定目标状态，能不能迁移过去由
 * {@code MesProdOrderStateMachine} 裁决。请求里带目标状态就等于把状态机变成建议，
 * 界面可以直接把工单推到任意状态，下达、齐套、领料、报工全都能绕过。
 *
 * <p>{@code idempotencyKey} 允许为空，留空由领域 Service 按 {@code UI:{工单号}:{命令类型}:0} 派生。
 * 取消动作会级联把作业工单与工序任务一起置取消，重复执行的代价不小。
 */
@Schema(description = "管理后台 - MES-生产工单动作 Request VO")
@Data
public class ProdOrderActionReqVO {

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产工单编号不能为空")
    private Long id;

    @Schema(description = "幂等键，留空由服务端按工单号与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "动作原因，写进在制流水；挂起与取消建议填写")
    @Size(max = 500, message = "动作原因长度不能超过 500")
    private String reason;

}
