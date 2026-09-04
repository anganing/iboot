package com.iboot.pf.module.wms.controller.admin.strategy.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-仓储策略 Excel 导入 VO
 *
 * <p>不含策略配置：筛选条件与排序链是嵌套结构，往 Excel 一格里塞 JSON 既不可读也没法校验，
 * 填错了还得回来问是哪个字段。导入只负责批量建出策略骨架（编码、名称、类型、仓库、优先级），
 * 配置在页面上用结构化编辑器配——那里能看到这个类型有哪些字段可选。
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

    @ExcelProperty("策略类型")
    private String strategyType;

    @ExcelProperty("适用仓库编号，0表示全局")
    private Long whId;

    @ExcelProperty("策略版本号")
    private String versionNo;

    @ExcelProperty("优先级，数值越大越优先")
    private Integer priority;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("备注")
    private String remark;

}