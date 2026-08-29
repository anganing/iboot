package com.iboot.pf.module.mdm.controller.admin.calendar.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 日历例外 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CalendarExceptionRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "日历编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("日历编号")
    private Long calendarId;

    @Schema(description = "例外日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("例外日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate exceptionDate;

    @Schema(description = "例外类型：HOLIDAY-停工，OVERTIME-加班，MAINTENANCE-计划保养",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "HOLIDAY")
    @ExcelProperty("例外类型")
    private String exceptionType;

    @Schema(description = "班次编号；为空表示全天", example = "1")
    private Long shiftId;

    @Schema(description = "班次编码")
    @ExcelProperty("班次编码")
    private String shiftCode;

    @Schema(description = "班次名称")
    @ExcelProperty("班次名称")
    private String shiftName;

    @Schema(description = "可用分钟数", example = "480")
    @ExcelProperty("可用分钟数")
    private Integer availableMinutes;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
