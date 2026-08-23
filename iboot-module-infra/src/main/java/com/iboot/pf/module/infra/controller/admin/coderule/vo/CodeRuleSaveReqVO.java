package com.iboot.pf.module.infra.controller.admin.coderule.vo;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.infra.enums.coderule.CodeResetCycleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 编码规则创建/修改 Request VO")
@Data
public class CodeRuleSaveReqVO {

    @Schema(description = "规则编号", example = "1024")
    private Long id;

    @Schema(description = "规则编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "purchase_order")
    @NotBlank(message = "规则编码不能为空")
    @Size(max = 100, message = "规则编码不能超过 100 个字符")
    private String ruleCode;

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "采购订单")
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称不能超过 100 个字符")
    private String name;

    @Size(max = 100, message = "前缀不能超过 100 个字符")
    private String prefix;
    @NotNull(message = "是否包含年不能为空")
    private Boolean yearEnabled;
    @NotNull(message = "是否包含月不能为空")
    private Boolean monthEnabled;
    @NotNull(message = "是否包含日不能为空")
    private Boolean dayEnabled;
    @Size(max = 100, message = "后缀不能超过 100 个字符")
    private String suffix;

    @NotNull(message = "流水号长度不能为空")
    @Min(value = 1, message = "流水号长度最小为 1")
    @Max(value = 18, message = "流水号长度最大为 18")
    private Integer serialLength;
    @NotNull(message = "流水号起始值不能为空")
    @Min(value = 1, message = "流水号起始值必须大于 0")
    private Long serialStart;
    @NotNull(message = "流水号步长不能为空")
    @Min(value = 1, message = "流水号步长必须大于 0")
    private Long serialStep;

    @NotNull(message = "重置周期不能为空")
    @InEnum(value = CodeResetCycleEnum.class, message = "重置周期必须是 {value}")
    private Integer resetCycle;
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;
    @Size(max = 500, message = "备注不能超过 500 个字符")
    private String remark;

}
