package com.iboot.pf.module.wms.controller.admin.zone.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-库区 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZoneImportExcelVO {

    @ExcelProperty("仓库编号")
    private Long whId;

    @ExcelProperty("库区编码")
    private String code;

    @ExcelProperty("库区名称")
    private String name;

    @ExcelProperty("库区类型")
    private String zoneType;

    @ExcelProperty("是否允许混放物料")
    private Boolean allowMixedMat;

    @ExcelProperty("是否允许混放批次")
    private Boolean allowMixedLot;

    @ExcelProperty("温区要求")
    private String tempRange;

    @ExcelProperty("状态")
    private Integer status;

    @ExcelProperty("备注")
    private String remark;

}