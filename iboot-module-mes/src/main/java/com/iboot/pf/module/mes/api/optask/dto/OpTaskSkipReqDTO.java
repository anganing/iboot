package com.iboot.pf.module.mes.api.optask.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工序任务跳序命令。
 *
 * <p>单独成类而不复用开工暂停的 DTO：跳序要记原因分类与影响说明，这两个字段对开工暂停毫无意义，
 * 混在一起就又变成调用方一半字段用不上的大杂烩。
 *
 * <p>是否放行由工序任务的 {@code skip_policy} 决定：{@code ALLOW_WITH_REASON} 要求 {@code reason}
 * 与 {@code reasonType} 都有值，{@code APPROVAL_REQUIRED} 还要求 {@code effectRemark}。
 */
@Schema(description = "RPC 服务 - 工序任务跳序 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OpTaskSkipReqDTO extends OpTaskStatusReqDTO {

    /** 跳序原因分类，取值为 MesSkipReasonTypeEnum 的枚举名。 */
    @Schema(description = "跳序原因分类", example = "PROCESS_CHANGE")
    private String reasonType;

    @Schema(description = "影响说明，说明对后续工序、质检及排程的影响，需要审批时必填")
    private String effectRemark;

}
