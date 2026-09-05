package com.iboot.pf.module.mes.api.rework.dto;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 创建返工任务命令。
 *
 * <p>不带返工工艺路线版本：版本按返工路线的生效版本锁定，与生产工单锁版本同一套口径。
 */
@Schema(description = "RPC 服务 - 创建返工任务 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateReworkReqDTO extends MesCommandReqDTO {

    @Schema(description = "来源质检任务编号，0 表示手工发起", example = "1")
    private Long sourceQcTaskId;

    @Schema(description = "返工起始工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "返工起始工序任务编号不能为空")
    private Long opTaskId;

    @Schema(description = "返工数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "返工数量不能为空")
    private BigDecimal qty;

    @Schema(description = "返工工艺路线编号，0 表示沿用原路线", example = "1")
    private Long routeId;

    @Schema(description = "备注")
    private String remark;

}
