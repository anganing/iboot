package com.iboot.pf.module.mdm.controller.admin.rescalendar.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 资源日历批量排班 Request VO")
@Data
public class ResCalendarBatchSaveReqVO {

    @Schema(description = "资源编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "5732,5733")
    @NotEmpty(message = "资源编号列表不能为空")
    private List<Long> resIds;

    @Schema(description = "工作日期开始", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工作日期开始不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate dateFrom;

    @Schema(description = "工作日期结束", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工作日期结束不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate dateTo;

    @Schema(description = "参与排班的星期，1 表示周一、7 表示周日；为空表示区间内每天都排", example = "1,2,3,4,5")
    private List<Integer> weekdays;

    @Schema(description = "班次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24842")
    @NotNull(message = "班次编号不能为空")
    private Long shiftId;

    @Schema(description = "可用产能分钟", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "可用产能分钟不能为空")
    private Integer availableMinutes;

    @Schema(description = "备注", example = "国庆加班")
    private String remark;

}
