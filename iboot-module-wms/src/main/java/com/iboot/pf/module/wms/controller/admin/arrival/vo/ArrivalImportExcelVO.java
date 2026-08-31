package com.iboot.pf.module.wms.controller.admin.arrival.vo;


import java.time.LocalDate;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-到货登记 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArrivalImportExcelVO {

    @ExcelProperty("到货登记单号")
    private String arrivalNo;

    @ExcelProperty("来源类型")
    private String sourceType;

    @ExcelProperty("来源单号")
    private String sourceNo;

    @ExcelProperty("供应商编号")
    private Long supplierId;

    @ExcelProperty("供应商编码快照")
    private String supplierCode;

    @ExcelProperty("供应商名称快照")
    private String supplierName;

    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @ExcelProperty("工厂编号")
    private Long plantId;

    @ExcelProperty("目标仓库编号")
    private Long whId;

    @ExcelProperty("预计到货日期")
    private LocalDate expectDate;

    @ExcelProperty("实际到货时间")
    private LocalDateTime arrivalTime;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("备注")
    private String remark;

}