package com.iboot.pf.module.mdm.controller.admin.changeover.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 资源换型矩阵新增/修改 Request VO")
@Data
public class ResChangeoverSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "资源编号，0-按资源组生效", example = "1")
    @NotNull(message = "资源编号不能为空")
    @Min(value = 0, message = "资源编号不能小于 0")
    private Long resId;

    @Schema(description = "资源组，resId 为 0 时必填")
    @Size(max = 64, message = "资源组长度不能超过 64")
    private String resGroup;

    @Schema(description = "换出换型分组", requiredMode = Schema.RequiredMode.REQUIRED, example = "红色")
    @NotBlank(message = "换出换型分组不能为空")
    private String fromGroup;

    @Schema(description = "换入换型分组", requiredMode = Schema.RequiredMode.REQUIRED, example = "蓝色")
    @NotBlank(message = "换入换型分组不能为空")
    private String toGroup;

    @Schema(description = "换型分钟数", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    @NotNull(message = "换型分钟数不能为空")
    @Min(value = 0, message = "换型分钟数不能小于 0")
    private Integer changeoverMinutes;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
