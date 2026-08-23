package com.iboot.pf.module.mdm.controller.admin.workshop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 车间新增/修改 Request VO")
@Data
public class WorkshopSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2564")
    private Long id;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13333")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}