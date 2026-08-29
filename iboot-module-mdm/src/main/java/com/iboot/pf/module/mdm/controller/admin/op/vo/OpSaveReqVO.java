package com.iboot.pf.module.mdm.controller.admin.op.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 标准工序新增/修改 Request VO")
@Data
public class OpSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "144")
    private Long id;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "标准时长（分钟），FIXED 时长类型取值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "标准时长（分钟）不能为空")
    private Integer stdDuration;

    @Schema(description = "准备/换型工时（分钟）")
    @Min(value = 0, message = "准备工时不能小于 0")
    private Integer setupMinutes;

    @Schema(description = "单件加工工时（分钟），PER_UNIT 时长类型必填")
    @DecimalMin(value = "0", message = "单件加工工时不能小于 0")
    private BigDecimal stdMinutesPerUnit;

    @Schema(description = "收尾工时（分钟）")
    @Min(value = 0, message = "收尾工时不能小于 0")
    private Integer teardownMinutes;

    @Schema(description = "排队等待时间（分钟）")
    @Min(value = 0, message = "排队等待时间不能小于 0")
    private Integer queueMinutes;

    @Schema(description = "转移时间（分钟）")
    @Min(value = 0, message = "转移时间不能小于 0")
    private Integer moveMinutes;

    @Schema(description = "时长类型：FIXED-固定工时，PER_UNIT-按件累加", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "时长类型不能为空")
    @Pattern(regexp = "FIXED|PER_UNIT", message = "时长类型只能是 FIXED 或 PER_UNIT")
    private String durationType;

    @Schema(description = "标准人数")
    @Min(value = 1, message = "标准人数必须大于 0")
    private Integer manQty;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}
