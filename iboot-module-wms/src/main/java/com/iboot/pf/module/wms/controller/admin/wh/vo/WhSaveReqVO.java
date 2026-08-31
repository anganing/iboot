package com.iboot.pf.module.wms.controller.admin.wh.vo;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsManagedByEnum;
import com.iboot.pf.module.wms.enums.WmsWhTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - WMS-仓库新增/修改 Request VO")
@Data
public class WhSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "WH001")
    @NotEmpty(message = "仓库编码不能为空")
    @Size(max = 64, message = "仓库编码长度不能超过 64")
    private String code;

    @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "仓库名称不能为空")
    @Size(max = 128, message = "仓库名称长度不能超过 128")
    private String name;

    @Schema(description = "仓库类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "ASRS")
    @NotEmpty(message = "仓库类型不能为空")
    @InEnum(WmsWhTypeEnum.class)
    private String whType;

    @Schema(description = "管理方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "MANUAL")
    @NotEmpty(message = "管理方式不能为空")
    @InEnum(WmsManagedByEnum.class)
    private String managedBy;

    @Schema(description = "外部设备系统编码，对接 WCS/AGV 时填写", example = "WCS-01")
    @Size(max = 64, message = "外部设备系统编码长度不能超过 64")
    private String externalSystemCode;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
