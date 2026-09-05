package com.iboot.pf.module.wms.dal.dataobject.outorder;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/** 出库单明细。 */
@TableName("wms_out_order_line")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsOutOrderLineDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 出库单编号。 */
    private Long orderId;

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

    /** 指定或分配的批次编号。 */
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

    /** 目标库位编号。 */
    private Long toLocId;

    /** 要求的库存质量状态。 */
    private String qualityStatus;

    /** 计划出库数量。 */
    private BigDecimal planQty;

    /** 已分配数量。 */
    private BigDecimal allocatedQty;

    /** 已拣选数量。 */
    private BigDecimal pickedQty;

    /** 已过账数量。 */
    private BigDecimal postedQty;

    /** 容器编号。 */
    private Long containerId;

    /** 关联库存预留编号，0 表示无预留。过账后按该编号核销预留，避免预留与扣减双重占用可用量。 */
    private Long reservationId;

    /** 明细状态。 */
    private String status;

    /** 备注。 */
    private String remark;

}
