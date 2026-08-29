package com.iboot.pf.module.mdm.controller.admin.skill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 技能新增/修改 Request VO")
@Data
public class SkillSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "技能编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "技能名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "焊接")
    @NotEmpty(message = "技能名称不能为空")
    private String name;

    @Schema(description = "等级体系说明")
    private String levelDesc;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
