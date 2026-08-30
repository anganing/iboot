package com.iboot.pf.module.aps.controller.admin.projectionresource.vo;


import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - APS-MDM资源与日历投影新增/修改 Request VO")
@Data
public class ProjectionResourceSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "14816")
    private Long id;

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5876")
    @NotNull(message = "排程场景编号不能为空")
    private Long scenarioId;

    @Schema(description = "MDM生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17785")
    @NotNull(message = "MDM生产资源编号不能为空")
    private Long sourceResId;

    @Schema(description = "资源编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "资源编码快照不能为空")
    private String resCode;

    @Schema(description = "资源名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "资源名称快照不能为空")
    private String resName;

    @Schema(description = "资源类型快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "资源类型快照不能为空")
    private String resType;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32372")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19808")
    @NotNull(message = "车间编号不能为空")
    private Long workshopId;

    @Schema(description = "工作中心编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24830")
    @NotNull(message = "工作中心编号不能为空")
    private Long wcId;

    @Schema(description = "产能日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "产能日期不能为空")
    private LocalDate workDate;

    @Schema(description = "班次编号，0表示日汇总", requiredMode = Schema.RequiredMode.REQUIRED, example = "25903")
    @NotNull(message = "班次编号，0表示日汇总不能为空")
    private Long shiftId;

    @Schema(description = "可用分钟数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "可用分钟数不能为空")
    private Integer availableMinutes;

    @Schema(description = "资源能力快照")
    private String capabilityJson;

    @Schema(description = "来源数据更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "来源数据更新时间不能为空")
    private LocalDateTime sourceUpdateTime;

    @Schema(description = "投影快照时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "投影快照时间不能为空")
    private LocalDateTime snapshotTime;

    @Schema(description = "备注", example = "随便")
    private String remark;

}