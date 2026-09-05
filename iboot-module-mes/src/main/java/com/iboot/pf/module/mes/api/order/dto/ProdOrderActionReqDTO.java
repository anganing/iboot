package com.iboot.pf.module.mes.api.order.dto;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 生产工单状态动作命令，下达与关闭共用。
 *
 * <p>目标状态不由调用方指定：动作名决定目标状态，状态机负责校验当前状态能否迁移过去。
 */
@Schema(description = "RPC 服务 - 生产工单状态动作 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProdOrderActionReqDTO extends MesCommandReqDTO {

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产工单编号不能为空")
    private Long prodOrderId;

    @Schema(description = "动作原因，挂起与取消时填写")
    private String reason;

}
