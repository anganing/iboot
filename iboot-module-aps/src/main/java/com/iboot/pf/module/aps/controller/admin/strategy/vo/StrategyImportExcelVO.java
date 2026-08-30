package com.iboot.pf.module.aps.controller.admin.strategy.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排程策略 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyImportExcelVO {

    @ExcelProperty("排程策略编码")
    private String code;

    @ExcelProperty("排程策略名称")
    private String name;

    @ExcelProperty("策略版本号")
    private String versionNo;

    @ExcelProperty("优化目标说明")
    private String objective;

    @ExcelProperty("硬约束、软约束及权重配置")
    private String configJson;

    @ExcelProperty("状态：DRAFT、ENABLED、DISABLED")
    private String status;

    @ExcelProperty("备注")
    private String remark;

}