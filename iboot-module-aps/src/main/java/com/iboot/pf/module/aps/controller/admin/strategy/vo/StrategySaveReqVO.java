package com.iboot.pf.module.aps.controller.admin.strategy.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.aps.enums.ApsStrategyStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 排程策略新增/修改 Request VO。 */
@Schema(description = "管理后台 - APS-排程策略新增/修改 Request VO")
@Data
public class StrategySaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "排程策略编码，留空由编码规则分配")
    @Size(max = 64, message = "排程策略编码长度不能超过 64")
    private String code;

    @Schema(description = "排程策略名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "排程策略名称不能为空")
    @Size(max = 128, message = "排程策略名称长度不能超过 128")
    private String name;

    @Schema(description = "策略版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "V1.0")
    @NotEmpty(message = "策略版本号不能为空")
    @Size(max = 32, message = "策略版本号长度不能超过 32")
    private String versionNo;

    @Schema(description = "优化目标说明", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "优化目标说明不能为空")
    @Size(max = 255, message = "优化目标说明长度不能超过 255")
    private String objective;

    @Schema(description = "硬约束、软约束及权重配置，JSON", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "约束权重配置不能为空")
    private String configJson;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @NotEmpty(message = "状态不能为空")
    @InEnum(ApsStrategyStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
