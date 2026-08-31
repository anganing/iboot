package com.iboot.pf.module.wms.controller.admin.invlock.vo;


import java.math.BigDecimal;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-库存冻结记录 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvLockImportExcelVO {

    @ExcelProperty("冻结编号")
    private String lockNo;

    @ExcelProperty("来源业务类型")
    private String bizType;

    @ExcelProperty("来源业务单号")
    private String bizNo;

    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @ExcelProperty("仓库编号")
    private Long whId;

    @ExcelProperty("库位编号")
    private Long locId;

    @ExcelProperty("物料编号")
    private Long matId;

    @ExcelProperty("批次编号")
    private Long batchId;

    @ExcelProperty("容器编号，0表示散件")
    private Long containerId;

    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @ExcelProperty("质量状态")
    private String qualityStatus;

    @ExcelProperty("冻结数量，基准单位")
    private BigDecimal qty;

    @ExcelProperty("已解冻数量")
    private BigDecimal releasedQty;

    @ExcelProperty("冻结原因")
    private String reason;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("冻结幂等键")
    private String idempotencyKey;

    @ExcelProperty("备注")
    private String remark;

}