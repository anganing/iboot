package com.iboot.pf.module.mes.controller.admin.qcrule.vo;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesQcModeEnum;
import com.iboot.pf.module.mes.enums.MesQcObjectTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 质检触发规则新增/修改 Request VO。
 *
 * <p>物料、物料分类、工序、工作中心、供应商都是可选匹配维度，0 表示不限；
 * 引用的检验方案必须是已发布状态，由 Service 校验。
 */
@Schema(description = "管理后台 - MES-质检触发规则新增/修改 Request VO")
@Data
public class QcRuleSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "质检规则编码，留空由编码规则分配")
    @Size(max = 64, message = "质检规则编码长度不能超过 64")
    private String code;

    @Schema(description = "质检规则名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "质检规则名称不能为空")
    @Size(max = 128, message = "质检规则名称长度不能超过 128")
    private String name;

    @Schema(description = "触发点", requiredMode = Schema.RequiredMode.REQUIRED, example = "IQC")
    @NotEmpty(message = "触发点不能为空")
    @InEnum(MesQcTriggerPointEnum.class)
    private String triggerPoint;

    @Schema(description = "检验对象类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "MATERIAL")
    @NotEmpty(message = "检验对象类型不能为空")
    @InEnum(MesQcObjectTypeEnum.class)
    private String objectType;

    @Schema(description = "指定物料编号，0 表示不限", example = "1")
    private Long matId;

    @Schema(description = "指定物料分类编号，0 表示不限", example = "1")
    private Long matCatId;

    @Schema(description = "指定工序编号，0 表示不限", example = "1")
    private Long opId;

    @Schema(description = "指定工作中心编号，0 表示不限", example = "1")
    private Long wcId;

    @Schema(description = "指定供应商编号，0 表示不限", example = "1")
    private Long supplierId;

    @Schema(description = "检验方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检验方案编号不能为空")
    private Long qcPlanId;

    @Schema(description = "检验模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "PROCESS")
    @NotEmpty(message = "检验模式不能为空")
    @InEnum(MesQcModeEnum.class)
    private String qcMode;

    @Schema(description = "优先级，数值越大越优先", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "优先级不能为空")
    @Min(value = 0, message = "优先级不能小于零")
    private Integer priority;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
