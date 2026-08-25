package com.iboot.pf.module.mdm.controller.admin.route.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 工艺路线工序新增/修改 Request VO")
@Data
public class RouteOpSaveReqVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "工艺路线编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工艺路线编号不能为空")
    private Long routeId;

    @Schema(description = "标准工序编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "标准工序编号不能为空")
    private Long opId;

    @Schema(description = "工作中心编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工作中心编号不能为空")
    private Long wcId;

    @Schema(description = "并行组")
    private String parallelGroup;

    @Schema(description = "是否关键工序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否关键工序不能为空")
    private Boolean keyFlag;

    @Schema(description = "是否首件检验", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否首件检验不能为空")
    private Boolean faiFlag;

    @Schema(description = "备注")
    private String remark;

}
