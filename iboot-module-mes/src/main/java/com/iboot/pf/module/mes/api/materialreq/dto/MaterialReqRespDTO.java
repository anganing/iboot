package com.iboot.pf.module.mes.api.materialreq.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 领退补料申请跨模块只读事实。
 *
 * <p>带明细行：跨模块调用方（WMS 回查、作业页）拿到申请头没有物料与数量就什么都做不了，
 * 让它再调一次行接口就是 N+1。
 */
@Schema(description = "RPC 服务 - 领退补料申请 Response DTO")
@Data
public class MaterialReqRespDTO {

    @Schema(description = "领退补料申请编号", example = "1")
    private Long id;

    @Schema(description = "领退补料申请单号", example = "MR202609040001")
    private String reqNo;

    @Schema(description = "生产工单编号", example = "1")
    private Long prodOrderId;

    @Schema(description = "生产工单号快照", example = "PO202609040001")
    private String prodOrderNo;

    @Schema(description = "作业工单编号，0 表示工单级申请", example = "1")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0 表示非工序级申请", example = "1")
    private Long opTaskId;

    /** 申请类型，取值为 MesMaterialReqTypeEnum 的枚举名。 */
    @Schema(description = "申请类型", example = "ISSUE")
    private String reqType;

    @Schema(description = "库存组织编号", example = "1")
    private Long invOrgId;

    @Schema(description = "目标仓库或来源仓库编号", example = "1")
    private Long whId;

    @Schema(description = "项目编号，0 表示无项目", example = "1")
    private Long projectId;

    @Schema(description = "项目任务编号，0 表示无项目任务", example = "1")
    private Long projectTaskId;

    @Schema(description = "WMS 生成的出入库单号", example = "OUT202609040001")
    private String wmsBizNo;

    /** 申请状态，取值为 MesMaterialReqStatusEnum 的枚举名。 */
    @Schema(description = "申请状态", example = "SUBMITTED")
    private String status;

    @Schema(description = "申请明细行")
    private List<Line> lines;

    /** 领退补料申请明细行。 */
    @Schema(description = "RPC 服务 - 领退补料申请明细 Response DTO")
    @Data
    public static class Line {

        @Schema(description = "明细编号", example = "1")
        private Long id;

        @Schema(description = "行号", example = "1")
        private Integer lineNo;

        @Schema(description = "路线工序用料编号，0 表示非 BOM 用料", example = "1")
        private Long routeOpMatId;

        @Schema(description = "物料编号", example = "1")
        private Long matId;

        @Schema(description = "物料编码快照", example = "M-0001")
        private String matCode;

        @Schema(description = "物料名称快照")
        private String matName;

        @Schema(description = "物料规格快照")
        private String matSpec;

        @Schema(description = "申请数量", example = "10")
        private BigDecimal qty;

        @Schema(description = "WMS 已处理数量", example = "10")
        private BigDecimal processedQty;

        @Schema(description = "计量单位编号", example = "1")
        private Long uomId;

        @Schema(description = "计量单位编码快照", example = "PCS")
        private String uomCode;

        @Schema(description = "指定批次编号，0 表示不指定批次", example = "1")
        private Long batchId;

        @Schema(description = "批次号快照")
        private String lotNo;

        @Schema(description = "是否必须在开工前发料", example = "true")
        private Boolean mustIssueBeforeStart;

        /** 明细状态，取值为 MesMaterialReqLineStatusEnum 的枚举名。 */
        @Schema(description = "明细状态", example = "DONE")
        private String status;

    }

}
