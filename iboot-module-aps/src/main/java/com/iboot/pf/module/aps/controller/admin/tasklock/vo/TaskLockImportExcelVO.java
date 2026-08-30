package com.iboot.pf.module.aps.controller.admin.tasklock.vo;


import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * APS-排程任务锁定记录 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskLockImportExcelVO {

    @ExcelProperty("排程任务编号")
    private Long taskId;

    @ExcelProperty("锁定类型：STARTED、MANUAL、MATERIAL、RESOURCE")
    private String lockType;

    @ExcelProperty("锁定原因")
    private String reason;

    @ExcelProperty("锁定人员编号")
    private Long lockedBy;

    @ExcelProperty("锁定时间")
    private LocalDateTime lockedTime;

    @ExcelProperty("解锁时间")
    private LocalDateTime unlockTime;

    @ExcelProperty("状态：LOCKED、UNLOCKED")
    private String status;

    @ExcelProperty("备注")
    private String remark;

}