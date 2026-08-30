package com.iboot.pf.module.wms.dal.dataobject.outorderline;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-出库单明细 DO
 *
 * @author iboot
 */
@TableName("wms_out_order_line")
@KeySequence("wms_out_order_line_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutOrderLineDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 出库单编号
     */
    private Long orderId;
    /**
     * 行号
     */
    private Integer lineNo;
    /**
     * 来源明细编号
     */
    private Long sourceLineId;
    /**
     * 来源明细行号
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
     * 指定或分配的批次编号
     */
    private Long batchId;
    /**
     * 批次号快照
     */
    private String lotNo;
    /**
     * 计量单位编号
     */
    private Long uomId;
    /**
     * 计量单位编码快照
     */
    private String uomCode;
    /**
     * 来源库位编号
     */
    private Long fromLocId;
    /**
     * 来源库位编码快照
     */
    private String fromLocCode;
    /**
     * 目标库位编号
     */
    private Long toLocId;
    /**
     * 要求的库存质量状态
     */
    private String qualityStatus;
    /**
     * 计划出库数量
     */
    private BigDecimal planQty;
    /**
     * 已分配数量
     */
    private BigDecimal allocatedQty;
    /**
     * 已拣选数量
     */
    private BigDecimal pickedQty;
    /**
     * 已过账数量
     */
    private BigDecimal postedQty;
    /**
     * 容器编号
     */
    private Long containerId;
    /**
     * 明细状态
     */
    private String status;
    /**
     * 备注
     */
    private String remark;

}