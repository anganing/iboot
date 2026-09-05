package com.iboot.pf.module.mes.api.demand.dto;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 生产需求状态动作命令，当前用于取消。 */
@Schema(description = "RPC 服务 - 生产需求状态动作 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProdDemandActionReqDTO extends MesCommandReqDTO {

    @Schema(description = "生产需求编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产需求编号不能为空")
    private Long demandId;

    @Schema(description = "动作原因")
    private String reason;

}
