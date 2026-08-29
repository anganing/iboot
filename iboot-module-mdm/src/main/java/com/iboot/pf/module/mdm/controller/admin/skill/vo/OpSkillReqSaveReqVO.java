package com.iboot.pf.module.mdm.controller.admin.skill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 工序技能要求新增/修改 Request VO")
@Data
public class OpSkillReqSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "标准工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "标准工序不能为空")
    private Long opId;

    @Schema(description = "技能编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "技能不能为空")
    private Long skillId;

    @Schema(description = "最低技能等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "最低技能等级不能为空")
    @Min(value = 1, message = "最低技能等级必须大于 0")
    @Max(value = 99, message = "最低技能等级不能大于 99")
    private Integer minLevel;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
