package com.iboot.pf.module.wms.controller.admin.wave.vo;


import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-出库波次 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WaveImportExcelVO {

    @ExcelProperty("波次编号")
    private String waveNo;

    @ExcelProperty("仓库编号")
    private Long whId;

    @ExcelProperty("波次类型")
    private String waveType;

    @ExcelProperty("计划开始时间")
    private LocalDateTime planStartTime;

    @ExcelProperty("计划结束时间")
    private LocalDateTime planEndTime;

    @ExcelProperty("状态：DRAFT、RELEASED、PICKING、DONE、CANCELLED")
    private String status;

    @ExcelProperty("备注")
    private String remark;

}