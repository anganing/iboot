package com.iboot.pf.module.wms.controller.admin.transferorder.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-库存转移调拨单 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferOrderImportExcelVO {

    @ExcelProperty("转移或调拨单号")
    private String transferNo;

    @ExcelProperty("转移类型")
    private String transferType;

    @ExcelProperty("来源业务类型")
    private String sourceType;

    @ExcelProperty("来源业务单号")
    private String sourceBizNo;

    @ExcelProperty("来源库存组织编号")
    private Long fromInvOrgId;

    @ExcelProperty("目标库存组织编号")
    private Long toInvOrgId;

    @ExcelProperty("来源仓库编号")
    private Long fromWhId;

    @ExcelProperty("目标仓库编号")
    private Long toWhId;

    @ExcelProperty("来源项目编号，0表示公共库存")
    private Long fromProjectId;

    @ExcelProperty("目标项目编号，0表示公共库存")
    private Long toProjectId;

    @ExcelProperty("外部设备触发方式")
    private String hookMode;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("创建幂等键")
    private String idempotencyKey;

    @ExcelProperty("备注")
    private String remark;

}