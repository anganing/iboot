package com.iboot.pf.module.wms.service.inorder.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 创建入库单的内部参数。 */
@Data
public class WmsCreateInOrderBO {

    /** 创建幂等键，格式：sourceModule:sourceBizNo:action。 */
    private String idempotencyKey;

    /** 调用方模块：MES、APS、INTEGRATION、UI。 */
    private String sourceModule;

    /** 调用方业务单号。 */
    private String sourceBizNo;

    /** 入库单号，为空由编码规则生成。 */
    private String inNo;

    /** 入库业务类型：PO_IN、PROD_FINISH_IN、REWORK_FINISH_IN、SALES_RETURN_IN、PROD_RETURN_IN、TRANSFER_IN、COUNT_GAIN_IN、OTHER_IN。 */
    private String bizType;

    /** 原始来源类型。 */
    private String originType;

    /** 原始来源单号。 */
    private String originNo;

    /** 到货登记编号。 */
    private Long arrivalId;

    /** 供应商编号。 */
    private Long supplierId;

    /** 客户编号。 */
    private Long customerId;

    /** 生产工单号快照。 */
    private String prodOrderNo;

    /** 作业工单号快照。 */
    private String workOrderNo;

    /** 转移或调拨单号。 */
    private String transferNo;

    /** 盘点单号。 */
    private String countOrderNo;

    /** 目标库存组织编号。 */
    private Long invOrgId;

    /** 目标工厂编号。 */
    private Long plantId;

    /** 项目编号。 */
    private Long projectId;

    /** 项目任务编号。 */
    private Long projectTaskId;

    /** 目标仓库编号。 */
    private Long whId;

    /** 是否需要质检。 */
    private Boolean needQc;

    /** 来料检验模式。 */
    private String iqcMode;

    /** 入库质量状态，为空按合格处理。 */
    private String qualityStatus;

    /** 外部设备触发方式：NONE、WCS、AGV。 */
    private String hookMode;

    /** 备注。 */
    private String remark;

    /** 入库明细。 */
    private List<Line> lines;

    /** 入库明细。 */
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

        /** 批次编号，为空时按批次号注册。 */
        private Long batchId;

        /** 批次号快照，无批次物料使用默认批次。 */
        private String lotNo;

        /** 计量单位编号。 */
        private Long uomId;

        /** 计量单位编码快照。 */
        private String uomCode;

        /** 目标库位编号。 */
        private Long locId;

        /** 目标库位编码快照。 */
        private String locCode;

        /** 入库质量状态，为空沿用单据质量状态。 */
        private String qualityStatus;

        /** 计划入库数量。 */
        private BigDecimal planQty;

        /** 容器编号。 */
        private Long containerId;

        /** 供应商编号。 */
        private Long supplierId;

        /** 供应商编码快照。 */
        private String supplierCode;

        /** 供应商批次号。 */
        private String supplierLotNo;

        /** 炉批号。 */
        private String furnaceLotNo;

        /** 生产日期。 */
        private java.time.LocalDate produceDate;

        /** 失效日期。 */
        private java.time.LocalDate expiryDate;

        /** 备注。 */
        private String remark;

    }

}
