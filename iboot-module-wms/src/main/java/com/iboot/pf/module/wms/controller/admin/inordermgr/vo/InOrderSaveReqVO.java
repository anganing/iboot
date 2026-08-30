package com.iboot.pf.module.wms.controller.admin.inordermgr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import com.iboot.pf.module.wms.dal.dataobject.inorderline.InOrderLineDO;

@Schema(description = "管理后台 - WMS-入库单新增/修改 Request VO")
@Data
public class InOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "22618")
    private Long id;

    @Schema(description = "入库单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "入库单号不能为空")
    private String inNo;

    @Schema(description = "入库业务类型：PO_IN、PROD_FINISH_IN、REWORK_FINISH_IN、SALES_RETURN_IN、PROD_RETURN_IN、TRANSFER_IN、COUNT_GAIN_IN、OTHER_IN", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "入库业务类型：PO_IN、PROD_FINISH_IN、REWORK_FINISH_IN、SALES_RETURN_IN、PROD_RETURN_IN、TRANSFER_IN、COUNT_GAIN_IN、OTHER_IN不能为空")
    private String bizType;

    @Schema(description = "直接来源类型", example = "2")
    private String sourceType;

    @Schema(description = "直接来源业务单号")
    private String sourceBizNo;

    @Schema(description = "直接来源明细编号", example = "17337")
    private Long sourceLineId;

    @Schema(description = "原始来源类型", example = "2")
    private String originType;

    @Schema(description = "原始来源单号")
    private String originNo;

    @Schema(description = "到货登记编号", example = "16148")
    private Long arrivalId;

    @Schema(description = "供应商编号", example = "21246")
    private Long supplierId;

    @Schema(description = "客户编号", example = "6109")
    private Long customerId;

    @Schema(description = "生产工单号快照")
    private String prodOrderNo;

    @Schema(description = "作业工单号快照")
    private String workOrderNo;

    @Schema(description = "转移或调拨单号")
    private String transferNo;

    @Schema(description = "盘点单号")
    private String countOrderNo;

    @Schema(description = "目标库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22369")
    @NotNull(message = "目标库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "目标工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20131")
    @NotNull(message = "目标工厂编号不能为空")
    private Long plantId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1028")
    @NotNull(message = "项目编号，0表示无项目不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "26608")
    @NotNull(message = "项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskId;

    @Schema(description = "目标仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10487")
    @NotNull(message = "目标仓库编号不能为空")
    private Long whId;

    @Schema(description = "目标仓库编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "目标仓库编码快照不能为空")
    private String whCode;

    @Schema(description = "来源库存组织编号，0表示非调拨", requiredMode = Schema.RequiredMode.REQUIRED, example = "24871")
    @NotNull(message = "来源库存组织编号，0表示非调拨不能为空")
    private Long fromInvOrgId;

    @Schema(description = "来源仓库编号，0表示非调拨", requiredMode = Schema.RequiredMode.REQUIRED, example = "31447")
    @NotNull(message = "来源仓库编号，0表示非调拨不能为空")
    private Long fromWhId;

    @Schema(description = "是否需要质检", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否需要质检不能为空")
    private Boolean needQc;

    @Schema(description = "来料检验模式")
    private String iqcMode;

    @Schema(description = "入库质量状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "入库质量状态不能为空")
    private String qualityStatus;

    @Schema(description = "外部设备触发方式：NONE、WCS、AGV", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "外部设备触发方式：NONE、WCS、AGV不能为空")
    private String hookMode;

    @Schema(description = "状态：DRAFT、SUBMITTED、QC_PENDING、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态：DRAFT、SUBMITTED、QC_PENDING、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION不能为空")
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "低频扩展属性")
    private String bizAttrsJson;

    @Schema(description = "备注或人工原因", example = "随便")
    private String remark;

}