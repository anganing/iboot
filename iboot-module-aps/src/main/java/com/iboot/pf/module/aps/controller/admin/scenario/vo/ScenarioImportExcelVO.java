package com.iboot.pf.module.aps.controller.admin.scenario.vo;


import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * APS-排程场景 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScenarioImportExcelVO {

    @ExcelProperty("排程场景编码")
    private String code;

    @ExcelProperty("排程场景名称")
    private String name;

    @ExcelProperty("工厂编号")
    private Long plantId;

    @ExcelProperty("车间编号，0表示全工厂")
    private Long workshopId;

    @ExcelProperty("排程策略编号")
    private Long strategyId;

    @ExcelProperty("计划窗口开始时间")
    private LocalDateTime planStartTime;

    @ExcelProperty("计划窗口结束时间")
    private LocalDateTime planEndTime;

    @ExcelProperty("输入数据快照时间")
    private LocalDateTime snapshotTime;

    @ExcelProperty("状态：DRAFT、SNAPSHOTTING、READY、SOLVING、SOLVED、CLOSED")
    private String status;

    @ExcelProperty("备注")
    private String remark;

}