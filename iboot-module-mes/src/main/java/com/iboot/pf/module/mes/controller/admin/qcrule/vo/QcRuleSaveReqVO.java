package com.iboot.pf.module.mes.controller.admin.qcrule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - MES-质检触发规则新增/修改 Request VO")
@Data
public class QcRuleSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "32013")
    private Long id;

    @Schema(description = "质检规则编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "质检规则编码不能为空")
    private String code;

    @Schema(description = "质检规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "质检规则名称不能为空")
    private String name;

    @Schema(description = "触发点：IQC、IPQC、FQC、OQC、FAI", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "触发点：IQC、IPQC、FQC、OQC、FAI不能为空")
    private String triggerPoint;

    @Schema(description = "检验对象类型：MATERIAL、BATCH、OP_TASK、PROD_ORDER、OUT_ORDER", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "检验对象类型：MATERIAL、BATCH、OP_TASK、PROD_ORDER、OUT_ORDER不能为空")
    private String objectType;

    @Schema(description = "指定物料编号，0表示不限", requiredMode = Schema.RequiredMode.REQUIRED, example = "21254")
    @NotNull(message = "指定物料编号，0表示不限不能为空")
    private Long matId;

    @Schema(description = "指定物料分类编号，0表示不限", requiredMode = Schema.RequiredMode.REQUIRED, example = "13962")
    @NotNull(message = "指定物料分类编号，0表示不限不能为空")
    private Long matCatId;

    @Schema(description = "指定工序编号，0表示不限", requiredMode = Schema.RequiredMode.REQUIRED, example = "3438")
    @NotNull(message = "指定工序编号，0表示不限不能为空")
    private Long opId;

    @Schema(description = "指定工作中心编号，0表示不限", requiredMode = Schema.RequiredMode.REQUIRED, example = "27812")
    @NotNull(message = "指定工作中心编号，0表示不限不能为空")
    private Long wcId;

    @Schema(description = "指定供应商编号，0表示不限", requiredMode = Schema.RequiredMode.REQUIRED, example = "2574")
    @NotNull(message = "指定供应商编号，0表示不限不能为空")
    private Long supplierId;

    @Schema(description = "检验方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13413")
    @NotNull(message = "检验方案编号不能为空")
    private Long qcPlanId;

    @Schema(description = "检验模式：PRE_INBOUND、POST_INBOUND_PENDING、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "检验模式：PRE_INBOUND、POST_INBOUND_PENDING、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE不能为空")
    private String qcMode;

    @Schema(description = "优先级，数值越大越优先", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "优先级，数值越大越优先不能为空")
    private Integer priority;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态：0-启用，1-停用不能为空")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}