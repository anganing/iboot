package com.iboot.pf.module.wms.controller.admin.containermgr.vo;


import java.math.BigDecimal;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-容器主档 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContainerImportExcelVO {

    @ExcelProperty("容器编号或条码")
    private String containerNo;

    @ExcelProperty("容器类型编号")
    private Long typeId;

    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @ExcelProperty("当前仓库编号")
    private Long whId;

    @ExcelProperty("当前库位编号，0表示未落位")
    private Long locId;

    @ExcelProperty("实际长度，单位毫米")
    private BigDecimal length;

    @ExcelProperty("实际宽度，单位毫米")
    private BigDecimal width;

    @ExcelProperty("实际高度，单位毫米")
    private BigDecimal height;

    @ExcelProperty("实际毛重")
    private BigDecimal grossWeight;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("乐观锁版本")
    private Integer version;

    @ExcelProperty("备注")
    private String remark;

}