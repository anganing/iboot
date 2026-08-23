package com.iboot.pf.module.mdm.controller.admin.uomconv.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 单位换算新增/修改 Request VO")
@Data
public class UomConvSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "31484")
    private Long id;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "9455")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    @Schema(description = "来源计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7295")
    @NotNull(message = "来源计量单位编号不能为空")
    private Long fromUomId;

    @Schema(description = "目标计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "14009")
    @NotNull(message = "目标计量单位编号不能为空")
    private Long toUomId;

    @Schema(description = "换算比例", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "换算比例不能为空")
    private BigDecimal ratio;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}