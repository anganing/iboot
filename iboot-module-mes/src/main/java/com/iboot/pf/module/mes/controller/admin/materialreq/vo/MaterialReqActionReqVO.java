package com.iboot.pf.module.mes.controller.admin.materialreq.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 领退补料申请动作请求，提交与取消共用。
 *
 * <p>两个动作都有库存后果，而后果由动作名决定、不由前端指定：提交逐行占预留（可用量下降），
 * 取消把预留还回去（可用量回升）。请求里带目标状态就等于让界面绕过这两步直接改状态。
 *
 * <p>{@code idempotencyKey} 允许为空，留空由领域 Service 按 {@code UI:{申请单号}:{命令类型}:0} 派生。
 * <b>这两个动作对键的稳定性依赖最强</b>：键不稳定，提交会占两笔预留、取消会把可用量回升两次
 * （等于凭空多出一批料）。
 */
@Schema(description = "管理后台 - MES-领退补料申请动作 Request VO")
@Data
public class MaterialReqActionReqVO {

    @Schema(description = "领退补料申请编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "领退补料申请编号不能为空")
    private Long id;

    @Schema(description = "幂等键，留空由服务端按申请单号与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "动作原因，写进申请备注；取消建议填写")
    @Size(max = 500, message = "动作原因长度不能超过 500")
    private String reason;

}
