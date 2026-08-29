package com.iboot.pf.module.mdm.controller.admin.tool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "管理后台 - 工装模具刀具新增/修改 Request VO")
@Data
public class ToolSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "夹具A")
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "工装类型：FIXTURE-工装，MOLD-模具，CUTTER-刀具，GAUGE-量具",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "FIXTURE")
    @NotBlank(message = "工装类型不能为空")
    @Pattern(regexp = "FIXTURE|MOLD|CUTTER|GAUGE", message = "工装类型不合法")
    private String toolType;

    @Schema(description = "所属工作中心编号")
    private Long wcId;

    @Schema(description = "保有数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "保有数量不能为空")
    @Min(value = 1, message = "保有数量必须大于 0")
    private Integer qtyTotal;

    @Schema(description = "寿命类型：COUNT-次数，TIME-时长")
    @Pattern(regexp = "COUNT|TIME", message = "寿命类型只能是 COUNT 或 TIME")
    private String lifeType;

    @Schema(description = "寿命上限")
    @Min(value = 0, message = "寿命上限不能小于 0")
    private Integer lifeLimit;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
