package com.iboot.pf.module.mes.controller.admin.optask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 工序任务动作请求，开工、暂停、恢复共用，也是完工与跳序请求的父类。
 *
 * <p><b>目标状态不由前端指定</b>：动作名决定目标状态，能不能迁移过去由 {@code MesOpTaskStateMachine} 裁决。
 * 请求里带目标状态就等于把状态机变成建议，开工门禁、报工累计、完工数量校验、后序串联全都能被绕过。
 * 这也是本任务把工序任务的 {@code create} / {@code update} / {@code delete} 端点删掉的同一个理由：
 * 工序任务是工单下达的产物，手工改它必然与工艺路线对不上。
 *
 * <p>{@code operatorId} 只在开工时用得上，传给开工门禁校验技能资质：现场换人常有，界面上选的人比派工记录准。
 * 留空时门禁自己从 {@code mes_dispatch_task} 取，取不到就按"未指定作业人员"拦下。
 *
 * <p>{@code idempotencyKey} 允许为空，留空由领域 Service 按 {@code UI:{工序任务号}:{命令类型}:0} 派生。
 */
@Schema(description = "管理后台 - MES-工序任务动作 Request VO")
@Data
public class OpTaskActionReqVO {

    @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工序任务编号不能为空")
    private Long id;

    @Schema(description = "幂等键，留空由服务端按工序任务号与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "操作者用户编号，开工时用于校验技能资质；留空回落派工记录", example = "1")
    private Long operatorId;

    @Schema(description = "动作原因，写进在制流水；暂停必须填写")
    @Size(max = 500, message = "动作原因长度不能超过 500")
    private String reason;

}
