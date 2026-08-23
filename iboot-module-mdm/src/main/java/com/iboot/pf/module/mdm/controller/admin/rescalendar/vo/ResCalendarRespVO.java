package com.iboot.pf.module.mdm.controller.admin.rescalendar.vo;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 资源日历 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ResCalendarRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "3505")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5732")
    @ExcelProperty("资源编号")
    private Long resId;

    @Schema(description = "工作日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("工作日期")
    private LocalDate workDate;

    @Schema(description = "班次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24842")
    @ExcelProperty("班次编号")
    private Long shiftId;

    @Schema(description = "可用产能分钟", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("可用产能分钟")
    private Integer availableMinutes;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}