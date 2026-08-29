package com.iboot.pf.module.mdm.controller.admin.team.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 班组成员新增/修改 Request VO")
@Data
public class TeamMemberSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "班组编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "班组不能为空")
    private Long teamId;

    @Schema(description = "system 用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "用户不能为空")
    private Long userId;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
