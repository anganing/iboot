package com.iboot.pf.module.wms.controller.admin.strategy.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-仓储策略 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyImportExcelVO {

    @ExcelProperty("策略编码")
    private String code;

    @ExcelProperty("策略名称")
    private String name;

    @ExcelProperty("策略类型：PUTAWAY、PICK、REPLENISH")
    private String strategyType;

    @ExcelProperty("适用仓库编号，0表示全局")
    private Long whId;

    @ExcelProperty("策略版本号")
    private String versionNo;

    @ExcelProperty("策略条件与排序规则配置")
    private String configJson;

    @ExcelProperty("优先级，数值越大越优先")
    private Integer priority;

    @ExcelProperty("状态：DRAFT、ENABLED、DISABLED")
    private String status;

    @ExcelProperty("备注")
    private String remark;

}