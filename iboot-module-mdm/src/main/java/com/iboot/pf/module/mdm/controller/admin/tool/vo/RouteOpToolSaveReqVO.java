package com.iboot.pf.module.mdm.controller.admin.tool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 工序工装新增/修改 Request VO")
@Data
public class RouteOpToolSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "路线工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "路线工序不能为空")
    private Long routeOpId;

    @Schema(description = "工装编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工装不能为空")
    private Long toolId;

    @Schema(description = "需求数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "需求数量不能为空")
    @Min(value = 1, message = "需求数量必须大于 0")
    private Integer qty;

    @Schema(description = "是否必备")
    private Boolean requiredFlag;

}
