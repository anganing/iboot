package com.iboot.pf.module.wms.controller.admin.inordermgr.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-入库单 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InOrderImportExcelVO {

    @ExcelProperty("入库单号")
    private String inNo;

    @ExcelProperty("入库业务类型：PO_IN、PROD_FINISH_IN、REWORK_FINISH_IN、SALES_RETURN_IN、PROD_RETURN_IN、TRANSFER_IN、COUNT_GAIN_IN、OTHER_IN")
    private String bizType;

    @ExcelProperty("直接来源类型")
    private String sourceType;

    @ExcelProperty("直接来源业务单号")
    private String sourceBizNo;

    @ExcelProperty("直接来源明细编号")
    private Long sourceLineId;

    @ExcelProperty("原始来源类型")
    private String originType;

    @ExcelProperty("原始来源单号")
    private String originNo;

    @ExcelProperty("到货登记编号")
    private Long arrivalId;

    @ExcelProperty("供应商编号")
    private Long supplierId;

    @ExcelProperty("客户编号")
    private Long customerId;

    @ExcelProperty("生产工单号快照")
    private String prodOrderNo;

    @ExcelProperty("作业工单号快照")
    private String workOrderNo;

    @ExcelProperty("转移或调拨单号")
    private String transferNo;

    @ExcelProperty("盘点单号")
    private String countOrderNo;

    @ExcelProperty("目标库存组织编号")
    private Long invOrgId;

    @ExcelProperty("目标工厂编号")
    private Long plantId;

    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @ExcelProperty("目标仓库编号")
    private Long whId;

    @ExcelProperty("目标仓库编码快照")
    private String whCode;

    @ExcelProperty("来源库存组织编号，0表示非调拨")
    private Long fromInvOrgId;

    @ExcelProperty("来源仓库编号，0表示非调拨")
    private Long fromWhId;

    @ExcelProperty("是否需要质检")
    private Boolean needQc;

    @ExcelProperty("来料检验模式")
    private String iqcMode;

    @ExcelProperty("入库质量状态")
    private String qualityStatus;

    @ExcelProperty("外部设备触发方式：NONE、WCS、AGV")
    private String hookMode;

    @ExcelProperty("状态：DRAFT、SUBMITTED、QC_PENDING、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION")
    private String status;

    @ExcelProperty("创建幂等键")
    private String idempotencyKey;

    @ExcelProperty("低频扩展属性")
    private String bizAttrsJson;

    @ExcelProperty("备注或人工原因")
    private String remark;

}