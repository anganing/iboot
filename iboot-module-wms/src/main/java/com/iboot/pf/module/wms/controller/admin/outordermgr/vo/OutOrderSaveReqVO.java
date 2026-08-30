package com.iboot.pf.module.wms.controller.admin.outordermgr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import com.iboot.pf.module.wms.dal.dataobject.outorderline.OutOrderLineDO;

@Schema(description = "管理后台 - WMS-出库单新增/修改 Request VO")
@Data
public class OutOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30708")
    private Long id;

    @Schema(description = "出库单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "出库单号不能为空")
    private String outNo;

    @Schema(description = "出库业务类型：PROD_ISSUE_OUT、SALES_DELIVERY_OUT、PO_RETURN_OUT、SCRAP_OUT、TRANSFER_OUT、COUNT_LOSS_OUT、OTHER_OUT", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "出库业务类型：PROD_ISSUE_OUT、SALES_DELIVERY_OUT、PO_RETURN_OUT、SCRAP_OUT、TRANSFER_OUT、COUNT_LOSS_OUT、OTHER_OUT不能为空")
    private String bizType;

    @Schema(description = "直接来源类型", example = "1")
    private String sourceType;

    @Schema(description = "直接来源业务单号")
    private String sourceBizNo;

    @Schema(description = "直接来源明细编号", example = "25991")
    private Long sourceLineId;

    @Schema(description = "原始来源类型", example = "1")
    private String originType;

    @Schema(description = "原始来源单号")
    private String originNo;

    @Schema(description = "客户编号", example = "21374")
    private Long customerId;

    @Schema(description = "供应商编号", example = "9944")
    private Long supplierId;

    @Schema(description = "生产工单号快照")
    private String prodOrderNo;

    @Schema(description = "作业工单号快照")
    private String workOrderNo;

    @Schema(description = "工序任务编号", example = "23912")
    private Long opTaskId;

    @Schema(description = "领退补料申请单号")
    private String materialReqNo;

    @Schema(description = "转移或调拨单号")
    private String transferNo;

    @Schema(description = "盘点单号")
    private String countOrderNo;

    @Schema(description = "来源库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18418")
    @NotNull(message = "来源库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "来源工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11271")
    @NotNull(message = "来源工厂编号不能为空")
    private Long plantId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "3537")
    @NotNull(message = "项目编号，0表示无项目不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "24854")
    @NotNull(message = "项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskId;

    @Schema(description = "来源仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28281")
    @NotNull(message = "来源仓库编号不能为空")
    private Long whId;

    @Schema(description = "来源仓库编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "来源仓库编码快照不能为空")
    private String whCode;

    @Schema(description = "目标库存组织编号，0表示非调拨", requiredMode = Schema.RequiredMode.REQUIRED, example = "2380")
    @NotNull(message = "目标库存组织编号，0表示非调拨不能为空")
    private Long toInvOrgId;

    @Schema(description = "目标仓库编号，0表示非调拨", requiredMode = Schema.RequiredMode.REQUIRED, example = "24608")
    @NotNull(message = "目标仓库编号，0表示非调拨不能为空")
    private Long toWhId;

    @Schema(description = "拣选策略编号", example = "31437")
    private Long pickStrategyId;

    @Schema(description = "外部设备触发方式：NONE、WCS、AGV", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "外部设备触发方式：NONE、WCS、AGV不能为空")
    private String hookMode;

    @Schema(description = "状态：DRAFT、SUBMITTED、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态：DRAFT、SUBMITTED、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION不能为空")
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "低频扩展属性")
    private String bizAttrsJson;

    @Schema(description = "备注或人工原因", example = "随便")
    private String remark;

}