package com.iboot.pf.module.wms.controller.admin.containertype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - WMS-容器类型新增/修改 Request VO")
@Data
public class ContainerTypeSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "13691")
    private Long id;

    @Schema(description = "容器类型编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "容器类型编码不能为空")
    private String code;

    @Schema(description = "容器类型名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "容器类型名称不能为空")
    private String name;

    @Schema(description = "标准长度，单位毫米")
    private BigDecimal stdLength;

    @Schema(description = "标准宽度，单位毫米")
    private BigDecimal stdWidth;

    @Schema(description = "标准高度，单位毫米")
    private BigDecimal stdHeight;

    @Schema(description = "最大承重")
    private BigDecimal maxWeight;

    @Schema(description = "最大容积")
    private BigDecimal maxVolume;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态：0-启用，1-停用不能为空")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}