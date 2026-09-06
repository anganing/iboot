package com.iboot.pf.module.mes.controller.admin.optask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 撤销跳序请求。
 *
 * <p>入口是<b>跳过记录</b>而不是工序任务：一道工序可能被跳过、撤销、再跳过，不指明撤哪一次，
 * 第二次跳序的记录会被第一次的撤销带走。记录编号从工序跳过记录列表取。
 *
 * <p>已生效的记录撤销后工序任务从已跳过退回待开工，待审批的记录撤销相当于撤回申请、工序状态本来就没动过。
 * 待审批记录只能靠这个动作撤回：BPM 审批本轮不接（阶段 3 的 T3-07），没有别的出口。
 */
@Schema(description = "管理后台 - MES-撤销跳序 Request VO")
@Data
public class OpTaskRevokeSkipReqVO {

    @Schema(description = "工序跳过记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工序跳过记录编号不能为空")
    private Long skipRecordId;

    @Schema(description = "幂等键，留空由服务端按工序任务号与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "撤销原因，写进反向流水与记录备注")
    @Size(max = 500, message = "撤销原因长度不能超过 500")
    private String reason;

}
