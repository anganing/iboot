package com.iboot.pf.module.wms.controller.admin.outordermgr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - WMS-出库单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OutOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30708")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "出库单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("出库单号")
    private String outNo;

    @Schema(description = "出库业务类型：PROD_ISSUE_OUT、SALES_DELIVERY_OUT、PO_RETURN_OUT、SCRAP_OUT、TRANSFER_OUT、COUNT_LOSS_OUT、OTHER_OUT", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("出库业务类型：PROD_ISSUE_OUT、SALES_DELIVERY_OUT、PO_RETURN_OUT、SCRAP_OUT、TRANSFER_OUT、COUNT_LOSS_OUT、OTHER_OUT")
    private String bizType;

    @Schema(description = "直接来源类型", example = "1")
    @ExcelProperty("直接来源类型")
    private String sourceType;

    @Schema(description = "直接来源业务单号")
    @ExcelProperty("直接来源业务单号")
    private String sourceBizNo;

    @Schema(description = "直接来源明细编号", example = "25991")
    @ExcelProperty("直接来源明细编号")
    private Long sourceLineId;

    @Schema(description = "原始来源类型", example = "1")
    @ExcelProperty("原始来源类型")
    private String originType;

    @Schema(description = "原始来源单号")
    @ExcelProperty("原始来源单号")
    private String originNo;

    @Schema(description = "客户编号", example = "21374")
    @ExcelProperty("客户编号")
    private Long customerId;

    @Schema(description = "供应商编号", example = "9944")
    @ExcelProperty("供应商编号")
    private Long supplierId;

    @Schema(description = "生产工单号快照")
    @ExcelProperty("生产工单号快照")
    private String prodOrderNo;

    @Schema(description = "作业工单号快照")
    @ExcelProperty("作业工单号快照")
    private String workOrderNo;

    @Schema(description = "工序任务编号", example = "23912")
    @ExcelProperty("工序任务编号")
    private Long opTaskId;

    @Schema(description = "领退补料申请单号")
    @ExcelProperty("领退补料申请单号")
    private String materialReqNo;

    @Schema(description = "转移或调拨单号")
    @ExcelProperty("转移或调拨单号")
    private String transferNo;

    @Schema(description = "盘点单号")
    @ExcelProperty("盘点单号")
    private String countOrderNo;

    @Schema(description = "来源库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18418")
    @ExcelProperty("来源库存组织编号")
    private Long invOrgId;

    @Schema(description = "来源工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11271")
    @ExcelProperty("来源工厂编号")
    private Long plantId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "3537")
    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "24854")
    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @Schema(description = "来源仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28281")
    @ExcelProperty("来源仓库编号")
    private Long whId;

    @Schema(description = "来源仓库编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源仓库编码快照")
    private String whCode;

    @Schema(description = "目标库存组织编号，0表示非调拨", requiredMode = Schema.RequiredMode.REQUIRED, example = "2380")
    @ExcelProperty("目标库存组织编号，0表示非调拨")
    private Long toInvOrgId;

    @Schema(description = "目标仓库编号，0表示非调拨", requiredMode = Schema.RequiredMode.REQUIRED, example = "24608")
    @ExcelProperty("目标仓库编号，0表示非调拨")
    private Long toWhId;

    @Schema(description = "拣选策略编号", example = "31437")
    @ExcelProperty("拣选策略编号")
    private Long pickStrategyId;

    @Schema(description = "外部设备触发方式：NONE、WCS、AGV", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("外部设备触发方式：NONE、WCS、AGV")
    private String hookMode;

    @Schema(description = "状态：DRAFT、SUBMITTED、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：DRAFT、SUBMITTED、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION")
    private String status;

    @Schema(description = "创建幂等键")
    @ExcelProperty("创建幂等键")
    private String idempotencyKey;

    @Schema(description = "低频扩展属性")
    @ExcelProperty("低频扩展属性")
    private String bizAttrsJson;

    @Schema(description = "备注或人工原因", example = "随便")
    @ExcelProperty("备注或人工原因")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "来源仓库名称")
    @ExcelProperty("来源仓库名称")
    private String whName;

}