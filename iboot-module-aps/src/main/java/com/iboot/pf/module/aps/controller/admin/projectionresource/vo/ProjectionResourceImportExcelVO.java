package com.iboot.pf.module.aps.controller.admin.projectionresource.vo;


import java.time.LocalDate;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源投影 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectionResourceImportExcelVO {

    @ExcelProperty("排程场景编号")
    private Long scenarioId;

    @ExcelProperty("MDM生产资源编号")
    private Long sourceResId;

    @ExcelProperty("资源编码快照")
    private String resCode;

    @ExcelProperty("资源名称快照")
    private String resName;

    @ExcelProperty("资源类型快照")
    private String resType;

    @ExcelProperty("工厂编号")
    private Long plantId;

    @ExcelProperty("车间编号")
    private Long workshopId;

    @ExcelProperty("工作中心编号")
    private Long wcId;

    @ExcelProperty("产能日期")
    private LocalDate workDate;

    @ExcelProperty("班次编号，0表示日汇总")
    private Long shiftId;

    @ExcelProperty("可用分钟数")
    private Integer availableMinutes;

    @ExcelProperty("资源能力快照")
    private String capabilityJson;

    @ExcelProperty("来源数据更新时间")
    private LocalDateTime sourceUpdateTime;

    @ExcelProperty("投影快照时间")
    private LocalDateTime snapshotTime;

    @ExcelProperty("备注")
    private String remark;

}