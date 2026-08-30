package com.iboot.pf.module.aps.controller.admin.load.vo;


import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 资源负荷新增/修改 Request VO")
@Data
public class LoadSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "6171")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28723")
    @NotNull(message = "排程版本编号不能为空")
    private Long versionId;

    @Schema(description = "生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4341")
    @NotNull(message = "生产资源编号不能为空")
    private Long resId;

    @Schema(description = "资源编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "资源编码快照不能为空")
    private String resCode;

    @Schema(description = "资源名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "资源名称快照不能为空")
    private String resName;

    @Schema(description = "负荷日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "负荷日期不能为空")
    private LocalDate workDate;

    @Schema(description = "计划负荷分钟数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划负荷分钟数不能为空")
    private Integer loadMinutes;

    @Schema(description = "可用产能分钟数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "可用产能分钟数不能为空")
    private Integer capacityMinutes;

    @Schema(description = "负荷率", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "负荷率不能为空")
    private BigDecimal loadRate;

    @Schema(description = "超负荷分钟数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "超负荷分钟数不能为空")
    private Integer overloadMinutes;

    @Schema(description = "备注", example = "随便")
    private String remark;

}