package com.iboot.pf.module.mdm.controller.admin.rescalendar.vo;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 资源日历列表 Request VO")
@Data
public class ResCalendarListReqVO {

    @Schema(description = "资源编号列表，为空表示全部", example = "5732,5733")
    private List<Long> resIds;

    @Schema(description = "工作日期开始", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工作日期开始不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate dateFrom;

    @Schema(description = "工作日期结束", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工作日期结束不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate dateTo;

}
