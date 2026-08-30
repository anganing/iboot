package com.iboot.pf.module.wms.dal.dataobject.transferorderline;

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
 * WMS-库存转移调拨明细 DO
 *
 * @author iboot
 */
@TableName("wms_transfer_order_line")
@KeySequence("wms_transfer_order_line_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferOrderLineDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 转移调拨单编号
     */
    private Long orderId;
    /**
     * 行号
     */
    private Integer lineNo;
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
     * 批次编号
     */
    private Long batchId;
    /**
     * 批次号快照
     */
    private String lotNo;
    /**
     * 容器编号，0表示散件
     */
    private Long containerId;
    /**
     * 来源库位编号
     */
    private Long fromLocId;
    /**
     * 目标库位编号
     */
    private Long toLocId;
    /**
     * 来源库存范围
     */
    private String fromStockScope;
    /**
     * 目标库存范围
     */
    private String toStockScope;
    /**
     * 来源项目编号，0表示公共库存
     */
    private Long fromProjectId;
    /**
     * 目标项目编号，0表示公共库存
     */
    private Long toProjectId;
    /**
     * 来源项目任务编号，0表示无项目任务
     */
    private Long fromProjectTaskId;
    /**
     * 目标项目任务编号，0表示无项目任务
     */
    private Long toProjectTaskId;
    /**
     * 来源质量状态
     */
    private String qualityStatusFrom;
    /**
     * 目标质量状态
     */
    private String qualityStatusTo;
    /**
     * 基准计量单位编号
     */
    private Long uomId;
    /**
     * 基准单位编码快照
     */
    private String uomCode;
    /**
     * 计划转移数量
     */
    private BigDecimal planQty;
    /**
     * 已搬运数量
     */
    private BigDecimal movedQty;
    /**
     * 已过账数量
     */
    private BigDecimal postedQty;
    /**
     * 明细状态
     */
    private String status;
    /**
     * 备注
     */
    private String remark;

}