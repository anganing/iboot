package com.iboot.pf.module.wms.service.outorder.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 创建出库单的内部参数。 */
@Data
public class WmsCreateOutOrderBO {

    /** 创建幂等键，格式：sourceModule:sourceBizNo:action。 */
    private String idempotencyKey;

    /** 调用方模块：MES、APS、INTEGRATION、UI。 */
    private String sourceModule;

    /** 调用方业务单号。 */
    private String sourceBizNo;

    /** 出库单号，为空由编码规则生成。 */
    private String outNo;

    /** 出库业务类型：PROD_ISSUE_OUT、SALES_DELIVERY_OUT、PO_RETURN_OUT、SCRAP_OUT、TRANSFER_OUT、COUNT_LOSS_OUT、OTHER_OUT。 */
    private String bizType;

    /** 原始来源类型。 */
    private String originType;

    /** 原始来源单号。 */
    private String originNo;

    /** 客户编号。 */
    private Long customerId;

    /** 供应商编号。 */
    private Long supplierId;

    /** 生产工单号快照。 */
    private String prodOrderNo;

    /** 作业工单号快照。 */
    private String workOrderNo;

    /** 工序任务编号。 */
    private Long opTaskId;

    /** 领退补料申请单号。 */
    private String materialReqNo;

    /** 转移或调拨单号。 */
    private String transferNo;

    /** 盘点单号。 */
    private String countOrderNo;

    /** 来源库存组织编号。 */
    private Long invOrgId;

    /** 来源工厂编号。 */
    private Long plantId;

    /** 项目编号。 */
    private Long projectId;

    /** 项目任务编号。 */
    private Long projectTaskId;

    /** 来源仓库编号。 */
    private Long whId;

    /** 拣选策略编号。 */
    private Long pickStrategyId;

    /** 外部设备触发方式：NONE、WCS、AGV。 */
    private String hookMode;

    /** 备注。 */
    private String remark;

    /** 出库明细。 */
    private List<Line> lines;

    /** 出库明细。 */
    @Data
    public static class Line {

        /** 行号。 */
        private Integer lineNo;

        /** 来源明细编号。 */
        private Long sourceLineId;

        /** 来源明细行号。 */
        private String sourceLineNo;

        /** 物料编号。 */
        private Long matId;

        /** 物料编码快照。 */
        private String matCode;

        /** 物料名称快照。 */
        private String matName;

        /** 物料规格快照。 */
        private String matSpec;

        /** 指定批次编号，为空表示不限批次。 */
        private Long batchId;

        /** 批次号快照。 */
        private String lotNo;

        /** 计量单位编号。 */
        private Long uomId;

        /** 计量单位编码快照。 */
        private String uomCode;

        /** 来源库位编号。 */
        private Long fromLocId;

        /** 来源库位编码快照。 */
        private String fromLocCode;

        /** 要求的库存质量状态，为空按合格处理。 */
        private String qualityStatus;

        /** 计划出库数量。 */
        private BigDecimal planQty;

        /** 容器编号。 */
        private Long containerId;

        /** 备注。 */
        private String remark;

    }

}
