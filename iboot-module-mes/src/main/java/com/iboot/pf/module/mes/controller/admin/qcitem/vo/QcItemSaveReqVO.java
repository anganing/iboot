package com.iboot.pf.module.mes.controller.admin.qcitem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - MES-检验项目新增/修改 Request VO")
@Data
public class QcItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "12885")
    private Long id;

    @Schema(description = "检验方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25866")
    @NotNull(message = "检验方案编号不能为空")
    private Long planId;

    @Schema(description = "检验项编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "检验项编码不能为空")
    private String itemCode;

    @Schema(description = "检验项名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "检验项名称不能为空")
    private String itemName;

    @Schema(description = "检验项类型：NUMERIC、TEXT、BOOLEAN", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "检验项类型：NUMERIC、TEXT、BOOLEAN不能为空")
    private String itemType;

    @Schema(description = "检验值单位")
    private String unit;

    @Schema(description = "标准值")
    private String stdValue;

    @Schema(description = "数值上限")
    private BigDecimal upperLimit;

    @Schema(description = "数值下限")
    private BigDecimal lowerLimit;

    @Schema(description = "是否必检", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否必检不能为空")
    private Boolean requiredFlag;

    @Schema(description = "排序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序号不能为空")
    private Integer sortNo;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态：0-启用，1-停用不能为空")
    private Integer status;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}