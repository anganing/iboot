package com.iboot.pf.module.wms.controller.admin.batchmgr.vo;


import java.time.LocalDate;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-批次主档 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchImportExcelVO {

    @ExcelProperty("物料编号")
    private Long matId;

    @ExcelProperty("物料编码快照")
    private String matCode;

    @ExcelProperty("物料名称快照")
    private String matName;

    @ExcelProperty("批次号；无批次物料使用默认批次")
    private String lotNo;

    @ExcelProperty("序列号；SN物料以数量为1的批次承载")
    private String snNo;

    @ExcelProperty("供应商编号")
    private Long supplierId;

    @ExcelProperty("供应商编码快照")
    private String supplierCode;

    @ExcelProperty("供应商批次号")
    private String supplierLotNo;

    @ExcelProperty("炉批号")
    private String furnaceLotNo;

    @ExcelProperty("生产日期")
    private LocalDate produceDate;

    @ExcelProperty("失效日期")
    private LocalDate expiryDate;

    @ExcelProperty("状态")
    private Integer status;

    @ExcelProperty("备注")
    private String remark;

}