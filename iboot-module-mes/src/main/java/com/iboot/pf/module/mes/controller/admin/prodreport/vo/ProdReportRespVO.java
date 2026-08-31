package com.iboot.pf.module.mes.controller.admin.prodreport.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.mes.enums.MesDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产报工 Response VO。
 *
 * <p>单号类字段来自模块内 JOIN；人员、资源、班次名称由 Service 调 system 与 mdm 门面批量补全。
 */
@Schema(description = "管理后台 - MES-生产报工 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProdReportRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "报工单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("报工单号")
    private String reportNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long prodOrderId;

    @Schema(description = "生产工单号")
    @ExcelProperty("生产工单号")
    private String prodOrderNo;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workOrderId;

    @Schema(description = "作业工单号")
    @ExcelProperty("作业工单号")
    private String workOrderNo;

    @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long opTaskId;

    @Schema(description = "工序任务号")
    @ExcelProperty("工序任务号")
    private String opTaskNo;

    @Schema(description = "工序名称")
    @ExcelProperty("工序名称")
    private String opName;

    @Schema(description = "报工作业人员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workerId;

    @Schema(description = "报工作业人员")
    @ExcelProperty("报工人")
    private String workerName;

    @Schema(description = "生产资源编号", example = "1")
    private Long resId;

    @Schema(description = "生产资源名称")
    @ExcelProperty("生产资源")
    private String resName;

    @Schema(description = "班次编号", example = "1")
    private Long shiftId;

    @Schema(description = "班次名称")
    @ExcelProperty("班次")
    private String shiftName;

    @Schema(description = "合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("合格数量")
    private BigDecimal qtyGood;

    @Schema(description = "报废数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("报废数量")
    private BigDecimal qtyScrap;

    @Schema(description = "返工数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("返工数量")
    private BigDecimal qtyRework;

    @Schema(description = "本次作业开始时间")
    @ExcelProperty("作业开始时间")
    private LocalDateTime startTime;

    @Schema(description = "本次作业结束时间")
    @ExcelProperty("作业结束时间")
    private LocalDateTime endTime;

    @Schema(description = "报工时间")
    @ExcelProperty("报工时间")
    private LocalDateTime reportTime;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.PROD_REPORT_STATUS)
    private String status;

    @Schema(description = "报工幂等键")
    private String idempotencyKey;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
