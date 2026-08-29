package com.iboot.pf.module.mdm.controller.admin.team.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 班组新增/修改 Request VO")
@Data
public class TeamSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "装配一班")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "所属车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "车间不能为空")
    private Long workshopId;

    @Schema(description = "常白/常夜班次编号")
    private Long shiftId;

    @Schema(description = "班组长用户编号")
    private Long leaderUserId;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
