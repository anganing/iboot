package com.iboot.pf.module.mes.controller.admin.qcplan.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-检验方案 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QcPlanImportExcelVO {

    @ExcelProperty("检验方案编码")
    private String code;

    @ExcelProperty("检验方案名称")
    private String name;

    @ExcelProperty("方案版本号")
    private String versionNo;

    @ExcelProperty("抽样类型：FULL、FIXED_QTY、PERCENT、AQL")
    private String sampleType;

    @ExcelProperty("固定数量或抽样比例")
    private BigDecimal sampleValue;

    @ExcelProperty("AQL接收质量限")
    private String aql;

    @ExcelProperty("接收判定规则")
    private String acceptanceRule;

    @ExcelProperty("状态：DRAFT、PUBLISHED、DISABLED")
    private String status;

    @ExcelProperty("备注")
    private String remark;

}