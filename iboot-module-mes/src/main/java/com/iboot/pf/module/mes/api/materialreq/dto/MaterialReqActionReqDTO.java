package com.iboot.pf.module.mes.api.materialreq.dto;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 领退补料申请状态动作命令，取消与确认共用。 */
@Schema(description = "RPC 服务 - 领退补料申请状态动作 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialReqActionReqDTO extends MesCommandReqDTO {

    @Schema(description = "领退补料申请编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "领退补料申请编号不能为空")
    private Long materialReqId;

    @Schema(description = "动作原因，取消时填写")
    private String reason;

}
