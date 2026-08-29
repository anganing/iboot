package com.iboot.pf.module.mdm.controller.admin.rescapability.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 资源能力新增/修改 Request VO")
@Data
public class ResCapabilitySaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产资源不能为空")
    private Long resId;

    @Schema(description = "标准工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "标准工序不能为空")
    private Long opId;

    @Schema(description = "是否首选资源")
    private Boolean preferredFlag;

    @Schema(description = "专属效率系数")
    @DecimalMin(value = "0.0001", message = "效率系数必须大于 0")
    @DecimalMax(value = "10", message = "效率系数不能大于 10")
    private BigDecimal efficiency;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
