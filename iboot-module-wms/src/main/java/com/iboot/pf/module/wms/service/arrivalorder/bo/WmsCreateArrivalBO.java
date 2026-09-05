package com.iboot.pf.module.wms.service.arrivalorder.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建到货登记的内部参数。
 *
 * <p>到货登记只是"货要来了"的预告，不动库存。物料、单位的可读字段是快照，由 Service 从 mdm
 * 门面批量取一次落库，调用方不需要传。
 */
@Data
public class WmsCreateArrivalBO {

    /** 到货登记单号，为空由编码规则生成。 */
    private String arrivalNo;

    /** 来源类型：PURCHASE_ORDER、ASN、SALES_RETURN、OTHER。 */
    private String sourceType;

    /** 来源单号。 */
    private String sourceNo;

    /** 供应商编号。 */
    private Long supplierId;

    /** 供应商编码快照。 */
    private String supplierCode;

    /** 供应商名称快照。 */
    private String supplierName;

    /** 库存组织编号。 */
    private Long invOrgId;

    /** 工厂编号。 */
    private Long plantId;

    /** 目标仓库编号。 */
    private Long whId;

    /** 预计到货日期。 */
    private LocalDate expectDate;

    /** 备注。 */
    private String remark;

    /** 到货明细。 */
    private List<Line> lines;

    /** 到货明细。 */
    @Data
    public static class Line {

        /** 行号。 */
        private Integer lineNo;

        /** 来源单据行号。 */
        private String sourceLineNo;

        /** 物料编号。 */
        private Long matId;

        /** 到货批次号，为空时确认到货按默认批次号注册。 */
        private String lotNo;

        /** 供应商批次号。 */
        private String supplierLotNo;

        /** 到货数量。 */
        private BigDecimal arrivalQty;

        /** 计量单位编号。 */
        private Long uomId;

        /**
         * 是否需要质检，为空按不需要处理。
         *
         * <p>当前由调用方按经验填，规则解析（按物料、分类、供应商匹配 {@code mes_qc_rule}）在 T2-06。
         */
        private Boolean needQc;

        /** 来料检验模式：PRE_INBOUND 先检后入、POST_INBOUND_PENDING 先入待检。 */
        private String iqcMode;

        /** 备注。 */
        private String remark;

    }

}
