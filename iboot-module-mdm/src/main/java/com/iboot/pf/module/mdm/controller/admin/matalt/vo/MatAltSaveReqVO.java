package com.iboot.pf.module.mdm.controller.admin.matalt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 替代料新增/修改 Request VO")
@Data
public class MatAltSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10792")
    private Long id;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "19327")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    @Schema(description = "替代物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26792")
    @NotNull(message = "替代物料编号不能为空")
    private Long altMatId;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "优先级不能为空")
    private Integer priority;

    @Schema(description = "备注", example = "随便")
    private String remark;

}