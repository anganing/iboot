package com.iboot.pf.module.mdm.controller.admin.invorg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 库存组织新增/修改 Request VO")
@Data
public class InvOrgSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "WH01")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "华东库存组织")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "用于华东区域库存核算")
    private String remark;
}
