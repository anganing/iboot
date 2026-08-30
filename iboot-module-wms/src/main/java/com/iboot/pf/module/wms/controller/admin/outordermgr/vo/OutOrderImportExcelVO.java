package com.iboot.pf.module.wms.controller.admin.outordermgr.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-出库单 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutOrderImportExcelVO {

    @ExcelProperty("出库单号")
    private String outNo;

    @ExcelProperty("出库业务类型：PROD_ISSUE_OUT、SALES_DELIVERY_OUT、PO_RETURN_OUT、SCRAP_OUT、TRANSFER_OUT、COUNT_LOSS_OUT、OTHER_OUT")
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

    @ExcelProperty("客户编号")
    private Long customerId;

    @ExcelProperty("供应商编号")
    private Long supplierId;

    @ExcelProperty("生产工单号快照")
    private String prodOrderNo;

    @ExcelProperty("作业工单号快照")
    private String workOrderNo;

    @ExcelProperty("工序任务编号")
    private Long opTaskId;

    @ExcelProperty("领退补料申请单号")
    private String materialReqNo;

    @ExcelProperty("转移或调拨单号")
    private String transferNo;

    @ExcelProperty("盘点单号")
    private String countOrderNo;

    @ExcelProperty("来源库存组织编号")
    private Long invOrgId;

    @ExcelProperty("来源工厂编号")
    private Long plantId;

    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @ExcelProperty("来源仓库编号")
    private Long whId;

    @ExcelProperty("来源仓库编码快照")
    private String whCode;

    @ExcelProperty("目标库存组织编号，0表示非调拨")
    private Long toInvOrgId;

    @ExcelProperty("目标仓库编号，0表示非调拨")
    private Long toWhId;

    @ExcelProperty("拣选策略编号")
    private Long pickStrategyId;

    @ExcelProperty("外部设备触发方式：NONE、WCS、AGV")
    private String hookMode;

    @ExcelProperty("状态：DRAFT、SUBMITTED、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION")
    private String status;

    @ExcelProperty("创建幂等键")
    private String idempotencyKey;

    @ExcelProperty("低频扩展属性")
    private String bizAttrsJson;

    @ExcelProperty("备注或人工原因")
    private String remark;

}