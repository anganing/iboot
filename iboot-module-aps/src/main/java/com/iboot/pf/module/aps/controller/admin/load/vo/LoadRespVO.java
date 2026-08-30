package com.iboot.pf.module.aps.controller.admin.load.vo;


import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - APS-资源负荷 Response VO")
@Data
@ExcelIgnoreUnannotated
public class LoadRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "6171")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28723")
    @ExcelProperty("排程版本编号")
    private Long versionId;

    @Schema(description = "生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4341")
    @ExcelProperty("生产资源编号")
    private Long resId;

    @Schema(description = "资源编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("资源编码快照")
    private String resCode;

    @Schema(description = "资源名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("资源名称快照")
    private String resName;

    @Schema(description = "负荷日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("负荷日期")
    private LocalDate workDate;

    @Schema(description = "计划负荷分钟数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划负荷分钟数")
    private Integer loadMinutes;

    @Schema(description = "可用产能分钟数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("可用产能分钟数")
    private Integer capacityMinutes;

    @Schema(description = "负荷率", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("负荷率")
    private BigDecimal loadRate;

    @Schema(description = "超负荷分钟数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("超负荷分钟数")
    private Integer overloadMinutes;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "排程版本version_no")
    @ExcelProperty("排程版本version_no")
    private String versionVersion_no;

}