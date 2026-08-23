package com.iboot.pf.module.mdm.controller.admin.routeopmat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 工序用料新增/修改 Request VO")
@Data
public class RouteOpMatSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30511")
    private Long id;

    @Schema(description = "路线工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30682")
    @NotNull(message = "路线工序编号不能为空")
    private Long routeOpId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11509")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须大于 0")
    private BigDecimal qty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25404")
    @NotNull(message = "计量单位编号不能为空")
    private Long uomId;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}
