package com.iboot.pf.module.mdm.controller.admin.route.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

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

    @Schema(description = "覆盖准备工时（分钟），为空继承标准工序")
    @Min(value = 0, message = "准备工时不能小于 0")
    private Integer overrideSetupMinutes;

    @Schema(description = "覆盖单件加工工时（分钟），为空继承标准工序")
    @DecimalMin(value = "0", message = "单件加工工时不能小于 0")
    private BigDecimal overrideStdMinutesPerUnit;

    @Schema(description = "覆盖排队等待时间（分钟），为空继承标准工序")
    @Min(value = 0, message = "排队等待时间不能小于 0")
    private Integer overrideQueueMinutes;

    @Schema(description = "覆盖转移时间（分钟），为空继承标准工序")
    @Min(value = 0, message = "转移时间不能小于 0")
    private Integer overrideMoveMinutes;

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
