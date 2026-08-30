package com.iboot.pf.module.aps.controller.admin.projectionresource.vo;


import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - APS-MDM资源与日历投影 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProjectionResourceRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "14816")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5876")
    @ExcelProperty("排程场景编号")
    private Long scenarioId;

    @Schema(description = "MDM生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17785")
    @ExcelProperty("MDM生产资源编号")
    private Long sourceResId;

    @Schema(description = "资源编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("资源编码快照")
    private String resCode;

    @Schema(description = "资源名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("资源名称快照")
    private String resName;

    @Schema(description = "资源类型快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("资源类型快照")
    private String resType;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32372")
    @ExcelProperty("工厂编号")
    private Long plantId;

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19808")
    @ExcelProperty("车间编号")
    private Long workshopId;

    @Schema(description = "工作中心编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24830")
    @ExcelProperty("工作中心编号")
    private Long wcId;

    @Schema(description = "产能日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产能日期")
    private LocalDate workDate;

    @Schema(description = "班次编号，0表示日汇总", requiredMode = Schema.RequiredMode.REQUIRED, example = "25903")
    @ExcelProperty("班次编号，0表示日汇总")
    private Long shiftId;

    @Schema(description = "可用分钟数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("可用分钟数")
    private Integer availableMinutes;

    @Schema(description = "资源能力快照")
    @ExcelProperty("资源能力快照")
    private String capabilityJson;

    @Schema(description = "来源数据更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源数据更新时间")
    private LocalDateTime sourceUpdateTime;

    @Schema(description = "投影快照时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("投影快照时间")
    private LocalDateTime snapshotTime;

    @Schema(description = "备注", example = "随便")
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