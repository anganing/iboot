package com.iboot.pf.module.aps.controller.admin.version.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 生成排程版本 Request VO。 */
@Schema(description = "管理后台 - APS-生成排程版本 Request VO")
@Data
public class GenerateVersionReqVO {

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排程场景不能为空")
    private Long scenarioId;

    @Schema(description = "排程策略编号，留空则用场景上引用的策略", example = "1")
    private Long strategyId;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
