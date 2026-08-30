package com.iboot.pf.module.wms.dal.dataobject.inorderline;

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
 * WMS-入库单明细 DO
 *
 * @author iboot
 */
@TableName("wms_in_order_line")
@KeySequence("wms_in_order_line_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InOrderLineDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 入库单编号
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
     * 批次编号，过账前可为空
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
     * 目标库位编号
     */
    private Long locId;
    /**
     * 目标库位编码快照
     */
    private String locCode;
    /**
     * 入库质量状态
     */
    private String qualityStatus;
    /**
     * 计划入库数量
     */
    private BigDecimal planQty;
    /**
     * 已完成数量
     */
    private BigDecimal completedQty;
    /**
     * 已过账数量
     */
    private BigDecimal postedQty;
    /**
     * 容器编号
     */
    private Long containerId;
    /**
     * 质检任务编号
     */
    private Long qcTaskId;
    /**
     * 明细状态
     */
    private String status;
    /**
     * 备注
     */
    private String remark;

}