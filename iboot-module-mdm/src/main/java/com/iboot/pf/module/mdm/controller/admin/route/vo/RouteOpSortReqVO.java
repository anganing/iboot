package com.iboot.pf.module.mdm.controller.admin.route.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 工艺路线工序排序 Request VO")
@Data
public class RouteOpSortReqVO {

    @Schema(description = "工艺路线编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工艺路线编号不能为空")
    private Long routeId;

    @Schema(description = "排序后的工序编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "工序排序不能为空")
    private List<Long> orderedIds;

}
