package com.iboot.pf.module.mdm.controller.admin.route.vo;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;

@Schema(description = "管理后台 - 工艺路线新增/修改 Request VO")
@Data
public class RouteSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "11381")
    private Long id;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25476")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "版本号不能为空")
    private String version;

    @Schema(description = "状态：DRAFT-草稿，PUBLISHED-已发布", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @Pattern(regexp = "DRAFT|PUBLISHED", message = "状态只能为 DRAFT 或 PUBLISHED")
    private String status;

    @Schema(description = "生效日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "生效日期不能为空")
    private LocalDate effectiveDate;

    @Schema(description = "失效日期")
    private LocalDate expiryDate;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}
