package com.iboot.pf.module.mdm.controller.admin.calendar.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 资源日历生成 Request VO")
@Data
public class CalendarGenerateReqVO {

    @Schema(description = "日历编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "日历不能为空")
    private Long calendarId;

    @Schema(description = "资源编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "资源不能为空")
    private List<Long> resIds;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate dateFrom;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate dateTo;

}
