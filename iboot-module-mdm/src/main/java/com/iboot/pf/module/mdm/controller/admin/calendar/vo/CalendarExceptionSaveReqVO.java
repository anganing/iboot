package com.iboot.pf.module.mdm.controller.admin.calendar.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 日历例外新增/修改 Request VO")
@Data
public class CalendarExceptionSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "日历编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "日历不能为空")
    private Long calendarId;

    @Schema(description = "例外日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "例外日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate exceptionDate;

    @Schema(description = "例外类型：HOLIDAY-停工，OVERTIME-加班，MAINTENANCE-计划保养",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "HOLIDAY")
    @NotBlank(message = "例外类型不能为空")
    @Pattern(regexp = "HOLIDAY|OVERTIME|MAINTENANCE", message = "例外类型只能是 HOLIDAY、OVERTIME 或 MAINTENANCE")
    private String exceptionType;

    @Schema(description = "班次编号；为空表示全天，仅停工例外支持全天", example = "1")
    private Long shiftId;

    @Schema(description = "可用分钟数，停工例外可为空（默认 0）", example = "480")
    @Min(value = 0, message = "可用分钟数不能小于 0")
    private Integer availableMinutes;

}
