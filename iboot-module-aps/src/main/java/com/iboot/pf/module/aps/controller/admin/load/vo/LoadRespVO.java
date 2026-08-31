package com.iboot.pf.module.aps.controller.admin.load.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资源负荷 Response VO。
 *
 * <p>资源编码与名称是写入时的快照；版本号与场景由 Mapper JOIN 取出。
 */
@Schema(description = "管理后台 - APS-资源负荷 Response VO")
@Data
@ExcelIgnoreUnannotated
public class LoadRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long versionId;

    @Schema(description = "排程版本号")
    @ExcelProperty("排程版本号")
    private Integer versionNo;

    @Schema(description = "排程场景编码")
    @ExcelProperty("排程场景编码")
    private String scenarioCode;

    @Schema(description = "排程场景名称")
    @ExcelProperty("排程场景")
    private String scenarioName;

    @Schema(description = "生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long resId;

    @Schema(description = "资源编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("资源编码")
    private String resCode;

    @Schema(description = "资源名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("资源名称")
    private String resName;

    @Schema(description = "负荷日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("负荷日期")
    private LocalDate workDate;

    @Schema(description = "计划负荷分钟数", requiredMode = Schema.RequiredMode.REQUIRED, example = "480")
    @ExcelProperty("计划负荷分钟数")
    private Integer loadMinutes;

    @Schema(description = "可用产能分钟数", requiredMode = Schema.RequiredMode.REQUIRED, example = "480")
    @ExcelProperty("可用产能分钟数")
    private Integer capacityMinutes;

    @Schema(description = "负荷率", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0000")
    @ExcelProperty("负荷率")
    private BigDecimal loadRate;

    @Schema(description = "超负荷分钟数", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("超负荷分钟数")
    private Integer overloadMinutes;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
