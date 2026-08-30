package com.iboot.pf.module.wms.service.transfer.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 创建转移调拨单的内部参数。 */
@Data
public class WmsCreateTransferOrderBO {

    /** 创建幂等键，格式：sourceModule:sourceBizNo:action。 */
    private String idempotencyKey;

    /** 调用方模块：MES、APS、INTEGRATION、UI。 */
    private String sourceModule;

    /** 调用方业务单号。 */
    private String sourceBizNo;

    /** 转移单号，为空由编码规则生成。 */
    private String transferNo;

    /** 转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER。 */
    private String transferType;

    /** 来源库存组织编号。 */
    private Long fromInvOrgId;

    /** 目标库存组织编号。 */
    private Long toInvOrgId;

    /** 来源仓库编号。 */
    private Long fromWhId;

    /** 目标仓库编号。 */
    private Long toWhId;

    /** 来源项目编号，0 表示公共库存。 */
    private Long fromProjectId;

    /** 目标项目编号，0 表示公共库存。 */
    private Long toProjectId;

    /** 外部设备触发方式：NONE、WCS、AGV。 */
    private String hookMode;

    /** 备注。 */
    private String remark;

    /** 转移明细。 */
    private List<Line> lines;

    /** 转移明细。 */
    @Data
    public static class Line {

        /** 行号。 */
        private Integer lineNo;

        /** 物料编号。 */
        private Long matId;

        /** 物料编码快照。 */
        private String matCode;

        /** 物料名称快照。 */
        private String matName;

        /** 批次编号。 */
        private Long batchId;

        /** 批次号快照。 */
        private String lotNo;

        /** 容器编号，0 表示散件。 */
        private Long containerId;

        /** 来源库位编号。 */
        private Long fromLocId;

        /** 目标库位编号。 */
        private Long toLocId;

        /** 来源库存范围：COMMON、PROJECT_DEDICATED。 */
        private String fromStockScope;

        /** 目标库存范围：COMMON、PROJECT_DEDICATED。 */
        private String toStockScope;

        /** 来源项目编号，0 表示公共库存。 */
        private Long fromProjectId;

        /** 目标项目编号，0 表示公共库存。 */
        private Long toProjectId;

        /** 来源项目任务编号，0 表示无项目任务。 */
        private Long fromProjectTaskId;

        /** 目标项目任务编号，0 表示无项目任务。 */
        private Long toProjectTaskId;

        /** 来源质量状态。 */
        private String qualityStatusFrom;

        /** 目标质量状态。 */
        private String qualityStatusTo;

        /** 基准计量单位编号。 */
        private Long uomId;

        /** 基准单位编码快照。 */
        private String uomCode;

        /** 计划转移数量。 */
        private BigDecimal planQty;

        /** 备注。 */
        private String remark;

    }

}
