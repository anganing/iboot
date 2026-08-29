package com.iboot.pf.module.mdm.controller.admin.calendar.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 日历规则新增/修改 Request VO")
@Data
public class CalendarRuleSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "日历编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "日历不能为空")
    private Long calendarId;

    @Schema(description = "星期几，1-7", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "星期几不能为空")
    @Min(value = 1, message = "星期几必须在 1-7 之间")
    @Max(value = 7, message = "星期几必须在 1-7 之间")
    private Integer dayOfWeek;

    @Schema(description = "班次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "班次不能为空")
    private Long shiftId;

    @Schema(description = "可用分钟数", requiredMode = Schema.RequiredMode.REQUIRED, example = "480")
    @NotNull(message = "可用分钟数不能为空")
    @Min(value = 0, message = "可用分钟数不能小于 0")
    private Integer availableMinutes;

}
