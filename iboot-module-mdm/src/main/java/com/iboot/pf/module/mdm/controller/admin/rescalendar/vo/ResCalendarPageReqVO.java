package com.iboot.pf.module.mdm.controller.admin.rescalendar.vo;

import java.time.LocalDate;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 资源日历分页 Request VO")
@Data
public class ResCalendarPageReqVO extends PageParam {

    @Schema(description = "资源编号", example = "5732")
    private Long resId;

    @Schema(description = "工作日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] workDate;

    @Schema(description = "班次编号", example = "24842")
    private Long shiftId;

    @Schema(description = "可用产能分钟")
    private Integer availableMinutes;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}