package com.iboot.pf.module.wms.controller.admin.strategy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsStrategyStatusEnum;
import com.iboot.pf.module.wms.enums.WmsStrategyTypeEnum;

@Schema(description = "管理后台 - WMS-仓储策略新增/修改 Request VO")
@Data
public class StrategySaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "策略编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "策略编码不能为空")
    private String code;

    @Schema(description = "策略名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "策略名称不能为空")
    private String name;

    @Schema(description = "策略类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "策略类型不能为空")
    @InEnum(WmsStrategyTypeEnum.class)
    private String strategyType;

    @Schema(description = "适用仓库编号，0表示全局", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "适用仓库编号，0表示全局不能为空")
    private Long whId;

    @Schema(description = "策略版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "策略版本号不能为空")
    private String versionNo;

    @Schema(description = "策略条件与排序规则配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "策略条件与排序规则配置不能为空")
    private String configJson;

    @Schema(description = "优先级，数值越大越优先", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "优先级，数值越大越优先不能为空")
    private Integer priority;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "状态不能为空")
    @InEnum(WmsStrategyStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    private String remark;

}
