package com.iboot.pf.module.aps.controller.admin.taskdep.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * APS-排程任务依赖 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDepImportExcelVO {

    @ExcelProperty("排程版本编号")
    private Long versionId;

    @ExcelProperty("前置排程任务编号")
    private Long fromTaskId;

    @ExcelProperty("后置排程任务编号")
    private Long toTaskId;

    @ExcelProperty("依赖类型：FINISH_START、START_START、FINISH_FINISH")
    private String depType;

    @ExcelProperty("依赖间隔分钟数")
    private Integer lagMinutes;

    @ExcelProperty("备注")
    private String remark;

}