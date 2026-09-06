package com.iboot.pf.module.mes.controller.admin.optask.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesSkipReasonTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工序任务跳序请求。
 *
 * <p>跳序是绕过工艺，因此原因类型与原因说明（继承来的 {@code reason}）两档都必填 —— 待审批的记录没有原因，
 * 审批人无从下手。
 *
 * <p>服务端按工序任务上的 {@code skip_policy} 分三路：{@code NOT_ALLOWED} 直接拒绝且连记录都不留；
 * {@code ALLOW_WITH_REASON} 立即生效并把后序推到待开工；{@code APPROVAL_REQUIRED} 只落待审批记录、
 * 工序状态一行不动，此时 {@code effectRemark} 必填。
 *
 * <p>策略来自工艺路线工序的配置：{@code mdm_route_op.skip_policy}（T1-32 补的配置位）在下达展开时被快照到
 * {@code mes_op_task.skip_policy}，路线上没配时落 {@code NOT_ALLOWED} —— 跳序是绕过工艺，
 * 配置缺失时放行的代价远大于拦下。
 */
@Schema(description = "管理后台 - MES-工序任务跳序 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OpTaskSkipReqVO extends OpTaskActionReqVO {

    @Schema(description = "跳序原因类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "EQUIPMENT_FAULT")
    @NotEmpty(message = "跳序原因类型不能为空")
    @InEnum(MesSkipReasonTypeEnum.class)
    private String skipReasonType;

    @Schema(description = "对后续工序、质检及排程的影响说明，需审批的跳序必填")
    @Size(max = 500, message = "影响说明长度不能超过 500")
    private String effectRemark;

}
