package com.iboot.pf.module.mdm.controller.admin.calendar.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 日历规则 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CalendarRuleRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "日历编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("日历编号")
    private Long calendarId;

    @Schema(description = "星期几，1-7", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("星期几")
    private Integer dayOfWeek;

    @Schema(description = "班次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("班次编号")
    private Long shiftId;

    @Schema(description = "班次编码")
    @ExcelProperty("班次编码")
    private String shiftCode;

    @Schema(description = "班次名称")
    @ExcelProperty("班次名称")
    private String shiftName;

    @Schema(description = "可用分钟数", requiredMode = Schema.RequiredMode.REQUIRED, example = "480")
    @ExcelProperty("可用分钟数")
    private Integer availableMinutes;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
