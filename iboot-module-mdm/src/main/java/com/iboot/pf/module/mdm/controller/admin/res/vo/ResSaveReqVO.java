package com.iboot.pf.module.mdm.controller.admin.res.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 生产资源新增/修改 Request VO")
@Data
public class ResSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "32120")
    private Long id;

    @Schema(description = "工作中心编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20144")
    @NotNull(message = "工作中心编号不能为空")
    private Long wcId;

    @Schema(description = "资源类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "资源类型不能为空")
    private String resType;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "效率系数")
    @DecimalMin(value = "0.0001", message = "效率系数必须大于 0")
    @DecimalMax(value = "10", message = "效率系数不能大于 10")
    private BigDecimal efficiency;

    @Schema(description = "可同时承载任务数")
    @Min(value = 1, message = "可同时承载任务数必须大于 0")
    private Integer parallelCapacity;

    @Schema(description = "资源组，同组可互替排程")
    @Size(max = 64, message = "资源组长度不能超过 64")
    private String resGroup;

    @Schema(description = "默认日历模板编号")
    private Long calendarId;

    @Schema(description = "工厂编号（冗余）")
    private Long plantId;

    @Schema(description = "车间编号（冗余）")
    private Long workshopId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}