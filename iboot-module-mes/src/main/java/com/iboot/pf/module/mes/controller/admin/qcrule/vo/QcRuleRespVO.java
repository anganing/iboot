package com.iboot.pf.module.mes.controller.admin.qcrule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-质检触发规则 Response VO")
@Data
@ExcelIgnoreUnannotated
public class QcRuleRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "32013")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "质检规则编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("质检规则编码")
    private String code;

    @Schema(description = "质检规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("质检规则名称")
    private String name;

    @Schema(description = "触发点：IQC、IPQC、FQC、OQC、FAI", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("触发点：IQC、IPQC、FQC、OQC、FAI")
    private String triggerPoint;

    @Schema(description = "检验对象类型：MATERIAL、BATCH、OP_TASK、PROD_ORDER、OUT_ORDER", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("检验对象类型：MATERIAL、BATCH、OP_TASK、PROD_ORDER、OUT_ORDER")
    private String objectType;

    @Schema(description = "指定物料编号，0表示不限", requiredMode = Schema.RequiredMode.REQUIRED, example = "21254")
    @ExcelProperty("指定物料编号，0表示不限")
    private Long matId;

    @Schema(description = "指定物料分类编号，0表示不限", requiredMode = Schema.RequiredMode.REQUIRED, example = "13962")
    @ExcelProperty("指定物料分类编号，0表示不限")
    private Long matCatId;

    @Schema(description = "指定工序编号，0表示不限", requiredMode = Schema.RequiredMode.REQUIRED, example = "3438")
    @ExcelProperty("指定工序编号，0表示不限")
    private Long opId;

    @Schema(description = "指定工作中心编号，0表示不限", requiredMode = Schema.RequiredMode.REQUIRED, example = "27812")
    @ExcelProperty("指定工作中心编号，0表示不限")
    private Long wcId;

    @Schema(description = "指定供应商编号，0表示不限", requiredMode = Schema.RequiredMode.REQUIRED, example = "2574")
    @ExcelProperty("指定供应商编号，0表示不限")
    private Long supplierId;

    @Schema(description = "检验方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13413")
    @ExcelProperty("检验方案编号")
    private Long qcPlanId;

    @Schema(description = "检验模式：PRE_INBOUND、POST_INBOUND_PENDING、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("检验模式：PRE_INBOUND、POST_INBOUND_PENDING、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE")
    private String qcMode;

    @Schema(description = "优先级，数值越大越优先", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("优先级，数值越大越优先")
    private Integer priority;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：0-启用，1-停用")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "检验方案编码")
    @ExcelProperty("检验方案编码")
    private String qcPlanCode;

    @Schema(description = "检验方案名称")
    @ExcelProperty("检验方案名称")
    private String qcPlanName;

}