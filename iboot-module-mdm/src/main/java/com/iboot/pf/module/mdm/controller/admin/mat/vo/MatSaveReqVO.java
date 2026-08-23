package com.iboot.pf.module.mdm.controller.admin.mat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 物料主档新增/修改 Request VO")
@Data
public class MatSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "29413")
    private Long id;

    @Schema(description = "物料分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8083")
    @NotNull(message = "物料分类编号不能为空")
    private Long catId;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "规格型号")
    private String spec;

    @Schema(description = "图号")
    private String drawingNo;

    @Schema(description = "物料类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "物料类型不能为空")
    private String matType;

    @Schema(description = "基准计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "35")
    @NotNull(message = "基准计量单位编号不能为空")
    private Long baseUomId;

    @Schema(description = "是否批次管控", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否批次管控不能为空")
    private Boolean batchFlag;

    @Schema(description = "是否SN管控", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否SN管控不能为空")
    private Boolean snFlag;

    @Schema(description = "保质期（天）")
    private Integer shelfLifeDays;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}