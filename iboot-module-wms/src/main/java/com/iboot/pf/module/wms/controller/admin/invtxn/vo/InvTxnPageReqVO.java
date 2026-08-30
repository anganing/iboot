package com.iboot.pf.module.wms.controller.admin.invtxn.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-库存流水分页 Request VO")
@Data
public class InvTxnPageReqVO extends PageParam {

    @Schema(description = "库存事务编号")
    private String txnNo;

    @Schema(description = "事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST", example = "1")
    private String txnType;

    @Schema(description = "来源业务类型", example = "2")
    private String bizType;

    @Schema(description = "来源业务单号")
    private String bizNo;

    @Schema(description = "来源业务行号")
    private String bizLineNo;

    @Schema(description = "收发方向：IN、OUT、MOVE、STATUS", example = "1")
    private String ioType;

    @Schema(description = "库存组织编号", example = "1936")
    private Long invOrgId;

    @Schema(description = "工厂编号", example = "25362")
    private Long plantId;

    @Schema(description = "仓库编号", example = "16756")
    private Long whId;

    @Schema(description = "来源库位编号，0表示无来源库位", example = "27072")
    private Long fromLocId;

    @Schema(description = "目标库位编号，0表示无目标库位", example = "7656")
    private Long toLocId;

    @Schema(description = "物料编号", example = "18046")
    private Long matId;

    @Schema(description = "批次编号", example = "3534")
    private Long batchId;

    @Schema(description = "容器编号，0表示散件", example = "12402")
    private Long containerId;

    @Schema(description = "基准计量单位编号", example = "17728")
    private Long uomId;

    @Schema(description = "事务数量，使用正数")
    private BigDecimal qty;

    @Schema(description = "过账前现存数量")
    private BigDecimal beforeQty;

    @Schema(description = "过账后现存数量")
    private BigDecimal afterQty;

    @Schema(description = "变更前质量状态")
    private String qualityStatusFrom;

    @Schema(description = "变更后质量状态")
    private String qualityStatusTo;

    @Schema(description = "变更前项目编号，0表示无项目")
    private Long projectIdFrom;

    @Schema(description = "变更前项目任务编号，0表示无项目任务")
    private Long projectTaskIdFrom;

    @Schema(description = "变更后项目编号，0表示无项目")
    private Long projectIdTo;

    @Schema(description = "变更后项目任务编号，0表示无项目任务")
    private Long projectTaskIdTo;

    @Schema(description = "物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", example = "李四")
    private String matName;

    @Schema(description = "单位编码快照")
    private String uomCode;

    @Schema(description = "批次号快照")
    private String lotNo;

    @Schema(description = "来源库位编码快照")
    private String fromLocCode;

    @Schema(description = "目标库位编码快照")
    private String toLocCode;

    @Schema(description = "过账幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}