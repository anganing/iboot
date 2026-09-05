package com.iboot.pf.module.mes.api.quality.dto;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 创建不合格品处置单命令。
 *
 * <p>联动目标由处置类型决定（返工建返工任务、报废走报废过账、让步接收放行入库），
 * 因此不带目标单号，那是处置执行后回写的结果。
 */
@Schema(description = "RPC 服务 - 创建不合格品处置单 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateDispositionReqDTO extends MesCommandReqDTO {

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检任务编号不能为空")
    private Long qcTaskId;

    /** 处置类型，取值为 MesDispositionTypeEnum 的枚举名。 */
    @Schema(description = "处置类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "REWORK")
    private String dispositionType;

    @Schema(description = "处置数量，不能超过质检任务的不合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "处置数量不能为空")
    private BigDecimal qty;

    @Schema(description = "处置说明")
    private String remark;

}
