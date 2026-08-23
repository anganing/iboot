package com.iboot.pf.module.mdm.controller.admin.rescalendar.vo;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 资源日历新增/修改 Request VO")
@Data
public class ResCalendarSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "3505")
    private Long id;

    @Schema(description = "资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5732")
    @NotNull(message = "资源编号不能为空")
    private Long resId;

    @Schema(description = "工作日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工作日期不能为空")
    private LocalDate workDate;

    @Schema(description = "班次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24842")
    @NotNull(message = "班次编号不能为空")
    private Long shiftId;

    @Schema(description = "可用产能分钟", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "可用产能分钟不能为空")
    private Integer availableMinutes;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}