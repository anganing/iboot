package com.iboot.pf.module.aps.controller.admin.projectionresource.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.aps.enums.ApsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资源投影 Response VO。
 *
 * <p>资源编码、名称、类型与可用分钟是刷新时从 mdm 取的快照；场景由 Mapper JOIN 取出，
 * 工厂、车间、工作中心、班次名称由 Service 走门面批量补全。
 */
@Schema(description = "管理后台 - APS-资源投影 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProjectionResourceRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long scenarioId;

    @Schema(description = "排程场景编码")
    @ExcelProperty("排程场景编码")
    private String scenarioCode;

    @Schema(description = "排程场景名称")
    @ExcelProperty("排程场景")
    private String scenarioName;

    @Schema(description = "MDM 生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long sourceResId;

    @Schema(description = "资源编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("资源编码")
    private String resCode;

    @Schema(description = "资源名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("资源名称")
    private String resName;

    @Schema(description = "资源类型快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "EQUIPMENT")
    @ExcelProperty(value = "资源类型", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.RES_TYPE)
    private String resType;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long plantId;

    @Schema(description = "工厂名称")
    @ExcelProperty("工厂")
    private String plantName;

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workshopId;

    @Schema(description = "车间名称")
    @ExcelProperty("车间")
    private String workshopName;

    @Schema(description = "工作中心编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long wcId;

    @Schema(description = "工作中心名称")
    @ExcelProperty("工作中心")
    private String wcName;

    @Schema(description = "产能日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产能日期")
    private LocalDate workDate;

    @Schema(description = "班次编号，0 表示日汇总", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long shiftId;

    @Schema(description = "班次名称")
    @ExcelProperty("班次")
    private String shiftName;

    @Schema(description = "可用分钟数", requiredMode = Schema.RequiredMode.REQUIRED, example = "480")
    @ExcelProperty("可用分钟数")
    private Integer availableMinutes;

    @Schema(description = "资源能力快照，JSON")
    private String capabilityJson;

    @Schema(description = "来源数据更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源数据更新时间")
    private LocalDateTime sourceUpdateTime;

    @Schema(description = "投影快照时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("投影快照时间")
    private LocalDateTime snapshotTime;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
