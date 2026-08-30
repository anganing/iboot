package com.iboot.pf.module.wms.controller.admin.wh.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-仓库 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WhImportExcelVO {

    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @ExcelProperty("工厂编号")
    private Long plantId;

    @ExcelProperty("仓库编码")
    private String code;

    @ExcelProperty("仓库名称")
    private String name;

    @ExcelProperty("仓库类型：ASRS-立库，FLAT-平库，LINE_SIDE-线边仓，VIRTUAL-虚拟仓，QC-质检仓，IN_TRANSIT-在途仓")
    private String whType;

    @ExcelProperty("管理方式：MANUAL、WCS、AGV、MIXED")
    private String managedBy;

    @ExcelProperty("外部设备系统编码")
    private String externalSystemCode;

    @ExcelProperty("状态：0-启用，1-停用")
    private Integer status;

    @ExcelProperty("备注")
    private String remark;

}