package com.iboot.pf.module.mes.api.report.dto;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产报工命令。
 *
 * <p>只给工序任务编号，生产工单与作业工单由服务端反查带出：调用方传三级编号一旦对不上，
 * 推进链就会把数量加到错误的工单上。
 *
 * <p>{@code qtyRework} 记在报工记录上但不参与三级数量推进，返工件的归属由返工任务决定。
 */
@Schema(description = "RPC 服务 - 生产报工 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProdReportReqDTO extends MesCommandReqDTO {

    @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工序任务编号不能为空")
    private Long opTaskId;

    @Schema(description = "报工作业人员编号，0 表示取当前登录用户", example = "1")
    private Long workerId;

    @Schema(description = "生产资源编号，0 表示未指定", example = "1")
    private Long resId;

    @Schema(description = "班次编号，0 表示不区分班次", example = "1")
    private Long shiftId;

    @Schema(description = "合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    @NotNull(message = "合格数量不能为空")
    private BigDecimal qtyGood;

    @Schema(description = "报废数量", example = "1")
    private BigDecimal qtyScrap;

    @Schema(description = "返工数量", example = "0")
    private BigDecimal qtyRework;

    @Schema(description = "本次作业开始时间")
    private LocalDateTime startTime;

    @Schema(description = "本次作业结束时间")
    private LocalDateTime endTime;

    @Schema(description = "备注")
    private String remark;

}
