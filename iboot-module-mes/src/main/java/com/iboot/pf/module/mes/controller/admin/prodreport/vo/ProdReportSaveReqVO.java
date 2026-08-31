package com.iboot.pf.module.mes.controller.admin.prodreport.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesProdReportStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产报工新增/修改 Request VO。
 *
 * <p>生产工单与作业工单编号由后端从工序任务反查带出，不由前端提交，避免三者对不上。
 */
@Schema(description = "管理后台 - MES-生产报工新增/修改 Request VO")
@Data
public class ProdReportSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "报工单号，留空由编码规则分配")
    @Size(max = 64, message = "报工单号长度不能超过 64")
    private String reportNo;

    @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工序任务编号不能为空")
    private Long opTaskId;

    @Schema(description = "报工作业人员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "报工作业人员不能为空")
    private Long workerId;

    @Schema(description = "生产资源编号，0 表示未指定", example = "1")
    private Long resId;

    @Schema(description = "班次编号，0 表示不区分班次", example = "1")
    private Long shiftId;

    @Schema(description = "合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "合格数量不能为空")
    @DecimalMin(value = "0", message = "合格数量不能为负数")
    private BigDecimal qtyGood;

    @Schema(description = "报废数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "报废数量不能为空")
    @DecimalMin(value = "0", message = "报废数量不能为负数")
    private BigDecimal qtyScrap;

    @Schema(description = "返工数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "返工数量不能为空")
    @DecimalMin(value = "0", message = "返工数量不能为负数")
    private BigDecimal qtyRework;

    @Schema(description = "本次作业开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "作业开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "本次作业结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "作业结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "报工时间，留空取当前时间")
    private LocalDateTime reportTime;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @NotEmpty(message = "状态不能为空")
    @InEnum(MesProdReportStatusEnum.class)
    private String status;

    @Schema(description = "报工幂等键")
    @Size(max = 128, message = "报工幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
