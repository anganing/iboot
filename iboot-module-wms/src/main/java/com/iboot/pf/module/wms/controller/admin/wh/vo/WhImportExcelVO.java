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

    @ExcelProperty("仓库类型")
    private String whType;

    @ExcelProperty("管理方式")
    private String managedBy;

    @ExcelProperty("外部设备系统编码")
    private String externalSystemCode;

    @ExcelProperty("状态")
    private Integer status;

    @ExcelProperty("备注")
    private String remark;

}