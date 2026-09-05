package com.iboot.pf.module.mes.api.optask.dto;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工序任务状态动作命令，开工与暂停共用。
 *
 * <p>不带目标状态：动作名决定目标状态，能不能迁移由工序任务状态机判定。
 */
@Schema(description = "RPC 服务 - 工序任务状态动作 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OpTaskStatusReqDTO extends MesCommandReqDTO {

    @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工序任务编号不能为空")
    private Long opTaskId;

    @Schema(description = "操作者编号，0 表示按派工记录取，开工门禁的技能校验用它", example = "1")
    private Long operatorId;

    @Schema(description = "动作原因，暂停时填写")
    private String reason;

}
