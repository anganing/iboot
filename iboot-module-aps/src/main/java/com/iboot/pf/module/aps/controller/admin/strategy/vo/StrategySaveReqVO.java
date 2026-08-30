package com.iboot.pf.module.aps.controller.admin.strategy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 排程策略新增/修改 Request VO")
@Data
public class StrategySaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "29641")
    private Long id;

    @Schema(description = "排程策略编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "排程策略编码不能为空")
    private String code;

    @Schema(description = "排程策略名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "排程策略名称不能为空")
    private String name;

    @Schema(description = "策略版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "策略版本号不能为空")
    private String versionNo;

    @Schema(description = "优化目标说明", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "优化目标说明不能为空")
    private String objective;

    @Schema(description = "硬约束、软约束及权重配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "硬约束、软约束及权重配置不能为空")
    private String configJson;

    @Schema(description = "状态：DRAFT、ENABLED、DISABLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态：DRAFT、ENABLED、DISABLED不能为空")
    private String status;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}