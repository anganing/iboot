package com.iboot.pf.module.mes.api.optask.dto;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 排程结果回写工序任务的命令。
 *
 * <p>APS 发布排程版本后，把“哪道工序在哪台设备上、什么时候做”写回 {@code mes_op_task} 的计划字段。
 * 只带计划口径的三个字段：资源、计划开始、计划结束；数量、状态推进、实际时间都由 MES 自己的业务动作维护。
 *
 * <p>整批下发而不是一条一条调：一个版本动辄几百道工序，逐条调用会变成 N 次事务与 N 次 SQL。
 */
@Schema(description = "RPC 服务 - 排程结果回写工序任务 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ApplyScheduleReqDTO extends MesCommandReqDTO {

    @Schema(description = "回写明细", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "回写明细不能为空")
    @Valid
    private List<Item> items;

    /** 单道工序的排程结果。 */
    @Schema(description = "RPC 服务 - 排程结果回写明细")
    @Data
    public static class Item {

        @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "工序任务编号不能为空")
        private Long opTaskId;

        @Schema(description = "排定生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "生产资源编号不能为空")
        private Long resId;

        @Schema(description = "计划开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "计划开始时间不能为空")
        private LocalDateTime plannedStartTime;

        @Schema(description = "计划结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "计划结束时间不能为空")
        private LocalDateTime plannedEndTime;

    }

}
