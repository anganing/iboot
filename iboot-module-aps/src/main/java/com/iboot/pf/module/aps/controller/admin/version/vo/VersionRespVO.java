package com.iboot.pf.module.aps.controller.admin.version.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 排程版本 Response VO")
@Data
@ExcelIgnoreUnannotated
public class VersionRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2759")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17546")
    @ExcelProperty("排程场景编号")
    private Long scenarioId;

    @Schema(description = "排程版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排程版本号")
    private Integer versionNo;

    @Schema(description = "状态：SOLVING、SOLVED、PENDING_CONFIRM、PUBLISHED、REPLACED、FAILED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：SOLVING、SOLVED、PENDING_CONFIRM、PUBLISHED、REPLACED、FAILED")
    private String status;

    @Schema(description = "Timefold综合得分")
    @ExcelProperty("Timefold综合得分")
    private String score;

    @Schema(description = "硬约束得分")
    @ExcelProperty("硬约束得分")
    private Long hardScore;

    @Schema(description = "软约束得分")
    @ExcelProperty("软约束得分")
    private Long softScore;

    @Schema(description = "求解开始时间")
    @ExcelProperty("求解开始时间")
    private LocalDateTime solveStartTime;

    @Schema(description = "求解结束时间")
    @ExcelProperty("求解结束时间")
    private LocalDateTime solveEndTime;

    @Schema(description = "发布时间")
    @ExcelProperty("发布时间")
    private LocalDateTime publishedTime;

    @Schema(description = "发布人员编号")
    @ExcelProperty("发布人员编号")
    private Long publishedBy;

    @Schema(description = "求解结果或失败信息")
    @ExcelProperty("求解结果或失败信息")
    private String solverMessage;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("乐观锁版本")
    private Integer version;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "排程场景编码")
    @ExcelProperty("排程场景编码")
    private String scenarioCode;

    @Schema(description = "排程场景名称")
    @ExcelProperty("排程场景名称")
    private String scenarioName;

}