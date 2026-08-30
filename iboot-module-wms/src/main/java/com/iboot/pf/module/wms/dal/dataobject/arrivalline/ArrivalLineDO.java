package com.iboot.pf.module.wms.dal.dataobject.arrivalline;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-到货登记明细 DO
 *
 * @author iboot
 */
@TableName("wms_arrival_line")
@KeySequence("wms_arrival_line_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArrivalLineDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 到货登记编号
     */
    private Long arrivalId;
    /**
     * 行号
     */
    private Integer lineNo;
    /**
     * 来源单据行号
     */
    private String sourceLineNo;
    /**
     * 物料编号
     */
    private Long matId;
    /**
     * 物料编码快照
     */
    private String matCode;
    /**
     * 物料名称快照
     */
    private String matName;
    /**
     * 物料规格快照
     */
    private String matSpec;
    /**
     * 到货批次号
     */
    private String lotNo;
    /**
     * 供应商批次号
     */
    private String supplierLotNo;
    /**
     * 到货数量
     */
    private BigDecimal arrivalQty;
    /**
     * 计量单位编号
     */
    private Long uomId;
    /**
     * 计量单位编码快照
     */
    private String uomCode;
    /**
     * 是否需要质检
     */
    private Boolean needQc;
    /**
     * 来料检验模式：PRE_INBOUND、POST_INBOUND_PENDING
     */
    private String iqcMode;
    /**
     * 质检任务编号
     */
    private Long qcTaskId;
    /**
     * 合格数量
     */
    private BigDecimal qualifiedQty;
    /**
     * 不合格数量
     */
    private BigDecimal rejectedQty;
    /**
     * 状态：PENDING、QC_PENDING、READY、DONE、CANCELLED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;

}