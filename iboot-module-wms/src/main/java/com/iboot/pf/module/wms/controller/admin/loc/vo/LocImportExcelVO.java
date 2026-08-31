package com.iboot.pf.module.wms.controller.admin.loc.vo;


import java.math.BigDecimal;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-库位 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocImportExcelVO {

    @ExcelProperty("仓库编号")
    private Long whId;

    @ExcelProperty("库区编号")
    private Long zoneId;

    @ExcelProperty("库位编码")
    private String code;

    @ExcelProperty("库位类型")
    private String locType;

    @ExcelProperty("巷道号")
    private String aisleNo;

    @ExcelProperty("排号")
    private String rowNo;

    @ExcelProperty("列号")
    private String colNo;

    @ExcelProperty("层号")
    private String levelNo;

    @ExcelProperty("纵深号")
    private Integer depthNo;

    @ExcelProperty("容器最大长度，单位毫米")
    private BigDecimal maxLength;

    @ExcelProperty("容器最大宽度，单位毫米")
    private BigDecimal maxWidth;

    @ExcelProperty("容器最大高度，单位毫米")
    private BigDecimal maxHeight;

    @ExcelProperty("最大承重")
    private BigDecimal maxWeight;

    @ExcelProperty("最大容积")
    private BigDecimal maxVolume;

    @ExcelProperty("最大容器数量")
    private Integer maxContainerQty;

    @ExcelProperty("尺寸校验模式")
    private String dimensionCheckMode;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("备注")
    private String remark;

}