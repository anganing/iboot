package com.iboot.pf.module.mes.controller.admin.qcresult.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-检验结果 Response VO")
@Data
@ExcelIgnoreUnannotated
public class QcResultRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "890")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1159")
    @ExcelProperty("质检任务编号")
    private Long taskId;

    @Schema(description = "检验项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25386")
    @ExcelProperty("检验项目编号")
    private Long itemId;

    @Schema(description = "样本序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("样本序号")
    private Integer sampleNo;

    @Schema(description = "检验项编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检验项编码快照")
    private String itemCode;

    @Schema(description = "检验项名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("检验项名称快照")
    private String itemName;

    @Schema(description = "实测值")
    @ExcelProperty("实测值")
    private String actualValue;

    @Schema(description = "是否合格", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否合格")
    private Boolean passFlag;

    @Schema(description = "检验员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29231")
    @ExcelProperty("检验员编号")
    private Long inspectorId;

    @Schema(description = "检验时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检验时间")
    private LocalDateTime inspectTime;

    @Schema(description = "本项判定合格数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本项判定合格数量")
    private BigDecimal qtyQualified;

    @Schema(description = "本项判定不合格数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本项判定不合格数量")
    private BigDecimal qtyRejected;

    @Schema(description = "项目结论：PASS、FAIL", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("项目结论：PASS、FAIL")
    private String conclusion;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "质检任务task_no")
    @ExcelProperty("质检任务task_no")
    private String taskTask_no;

}