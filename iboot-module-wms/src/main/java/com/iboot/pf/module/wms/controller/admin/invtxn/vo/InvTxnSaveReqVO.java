package com.iboot.pf.module.wms.controller.admin.invtxn.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - WMS-库存流水新增/修改 Request VO")
@Data
public class InvTxnSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "23788")
    private Long id;

    @Schema(description = "库存事务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "库存事务编号不能为空")
    private String txnNo;

    @Schema(description = "事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST不能为空")
    private String txnType;

    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "来源业务类型不能为空")
    private String bizType;

    @Schema(description = "来源业务单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "来源业务单号不能为空")
    private String bizNo;

    @Schema(description = "来源业务行号")
    private String bizLineNo;

    @Schema(description = "收发方向：IN、OUT、MOVE、STATUS", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "收发方向：IN、OUT、MOVE、STATUS不能为空")
    private String ioType;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1936")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25362")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "16756")
    @NotNull(message = "仓库编号不能为空")
    private Long whId;

    @Schema(description = "来源库位编号，0表示无来源库位", requiredMode = Schema.RequiredMode.REQUIRED, example = "27072")
    @NotNull(message = "来源库位编号，0表示无来源库位不能为空")
    private Long fromLocId;

    @Schema(description = "目标库位编号，0表示无目标库位", requiredMode = Schema.RequiredMode.REQUIRED, example = "7656")
    @NotNull(message = "目标库位编号，0表示无目标库位不能为空")
    private Long toLocId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18046")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    @Schema(description = "批次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3534")
    @NotNull(message = "批次编号不能为空")
    private Long batchId;

    @Schema(description = "容器编号，0表示散件", requiredMode = Schema.RequiredMode.REQUIRED, example = "12402")
    @NotNull(message = "容器编号，0表示散件不能为空")
    private Long containerId;

    @Schema(description = "基准计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17728")
    @NotNull(message = "基准计量单位编号不能为空")
    private Long uomId;

    @Schema(description = "事务数量，使用正数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "事务数量，使用正数不能为空")
    private BigDecimal qty;

    @Schema(description = "过账前现存数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "过账前现存数量不能为空")
    private BigDecimal beforeQty;

    @Schema(description = "过账后现存数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "过账后现存数量不能为空")
    private BigDecimal afterQty;

    @Schema(description = "变更前质量状态")
    private String qualityStatusFrom;

    @Schema(description = "变更后质量状态")
    private String qualityStatusTo;

    @Schema(description = "变更前项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "变更前项目编号，0表示无项目不能为空")
    private Long projectIdFrom;

    @Schema(description = "变更前项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "变更前项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskIdFrom;

    @Schema(description = "变更后项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "变更后项目编号，0表示无项目不能为空")
    private Long projectIdTo;

    @Schema(description = "变更后项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "变更后项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskIdTo;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "物料编码快照不能为空")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "物料名称快照不能为空")
    private String matName;

    @Schema(description = "单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "单位编码快照不能为空")
    private String uomCode;

    @Schema(description = "批次号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "批次号快照不能为空")
    private String lotNo;

    @Schema(description = "来源库位编码快照")
    private String fromLocCode;

    @Schema(description = "目标库位编码快照")
    private String toLocCode;

    @Schema(description = "过账幂等键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "过账幂等键不能为空")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}