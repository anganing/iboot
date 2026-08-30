package com.iboot.pf.module.mes.controller.admin.qcrule.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-质检触发规则 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QcRuleImportExcelVO {

    @ExcelProperty("质检规则编码")
    private String code;

    @ExcelProperty("质检规则名称")
    private String name;

    @ExcelProperty("触发点：IQC、IPQC、FQC、OQC、FAI")
    private String triggerPoint;

    @ExcelProperty("检验对象类型：MATERIAL、BATCH、OP_TASK、PROD_ORDER、OUT_ORDER")
    private String objectType;

    @ExcelProperty("指定物料编号，0表示不限")
    private Long matId;

    @ExcelProperty("指定物料分类编号，0表示不限")
    private Long matCatId;

    @ExcelProperty("指定工序编号，0表示不限")
    private Long opId;

    @ExcelProperty("指定工作中心编号，0表示不限")
    private Long wcId;

    @ExcelProperty("指定供应商编号，0表示不限")
    private Long supplierId;

    @ExcelProperty("检验方案编号")
    private Long qcPlanId;

    @ExcelProperty("检验模式：PRE_INBOUND、POST_INBOUND_PENDING、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE")
    private String qcMode;

    @ExcelProperty("优先级，数值越大越优先")
    private Integer priority;

    @ExcelProperty("状态：0-启用，1-停用")
    private Integer status;

    @ExcelProperty("备注")
    private String remark;

}