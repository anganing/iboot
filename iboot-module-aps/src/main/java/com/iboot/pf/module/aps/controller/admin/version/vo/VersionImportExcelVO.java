package com.iboot.pf.module.aps.controller.admin.version.vo;


import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * APS-排程版本 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VersionImportExcelVO {

    @ExcelProperty("排程场景编号")
    private Long scenarioId;

    @ExcelProperty("排程版本号")
    private Integer versionNo;

    @ExcelProperty("状态：SOLVING、SOLVED、PENDING_CONFIRM、PUBLISHED、REPLACED、FAILED")
    private String status;

    @ExcelProperty("Timefold综合得分")
    private String score;

    @ExcelProperty("硬约束得分")
    private Long hardScore;

    @ExcelProperty("软约束得分")
    private Long softScore;

    @ExcelProperty("求解开始时间")
    private LocalDateTime solveStartTime;

    @ExcelProperty("求解结束时间")
    private LocalDateTime solveEndTime;

    @ExcelProperty("发布时间")
    private LocalDateTime publishedTime;

    @ExcelProperty("发布人员编号")
    private Long publishedBy;

    @ExcelProperty("求解结果或失败信息")
    private String solverMessage;

    @ExcelProperty("乐观锁版本")
    private Integer version;

    @ExcelProperty("备注")
    private String remark;

}