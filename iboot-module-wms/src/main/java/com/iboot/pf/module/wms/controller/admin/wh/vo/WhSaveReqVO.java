package com.iboot.pf.module.wms.controller.admin.wh.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - WMS-仓库新增/修改 Request VO")
@Data
public class WhSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10993")
    private Long id;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8586")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21479")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "仓库编码不能为空")
    private String code;

    @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "仓库名称不能为空")
    private String name;

    @Schema(description = "仓库类型：ASRS-立库，FLAT-平库，LINE_SIDE-线边仓，VIRTUAL-虚拟仓，QC-质检仓，IN_TRANSIT-在途仓", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "仓库类型：ASRS-立库，FLAT-平库，LINE_SIDE-线边仓，VIRTUAL-虚拟仓，QC-质检仓，IN_TRANSIT-在途仓不能为空")
    private String whType;

    @Schema(description = "管理方式：MANUAL、WCS、AGV、MIXED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "管理方式：MANUAL、WCS、AGV、MIXED不能为空")
    private String managedBy;

    @Schema(description = "外部设备系统编码")
    private String externalSystemCode;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态：0-启用，1-停用不能为空")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}