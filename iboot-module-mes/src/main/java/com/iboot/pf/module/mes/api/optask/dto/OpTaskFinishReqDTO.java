package com.iboot.pf.module.mes.api.optask.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工序任务完工命令。
 *
 * <p>不带计划数量：完工校验的口径是"合格数 + 报废数 = 计划数"，计划数由服务端从工序任务上读，
 * 让调用方把基准值一起传进来，校验就等于自证，起不到拦截作用。
 */
@Schema(description = "RPC 服务 - 工序任务完工 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OpTaskFinishReqDTO extends OpTaskStatusReqDTO {

    @Schema(description = "本次完工合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "合格数量不能为空")
    private BigDecimal qtyGood;

    @Schema(description = "本次完工报废数量", example = "0")
    private BigDecimal qtyScrap;

    @Schema(description = "实际结束时间，为空时取服务端当前时间")
    private LocalDateTime actualEndTime;

}
