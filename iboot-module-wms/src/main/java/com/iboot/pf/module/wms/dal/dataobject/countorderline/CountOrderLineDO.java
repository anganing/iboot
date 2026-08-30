package com.iboot.pf.module.wms.dal.dataobject.countorderline;

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
 * WMS-盘点明细 DO
 *
 * @author iboot
 */
@TableName("wms_count_order_line")
@KeySequence("wms_count_order_line_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountOrderLineDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 盘点单编号
     */
    private Long orderId;
    /**
     * 行号
     */
    private Integer lineNo;
    /**
     * 库存余额编号
     */
    private Long balanceId;
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
     * 库位编号
     */
    private Long locId;
    /**
     * 库位编码快照
     */
    private String locCode;
    /**
     * 容器编号，0表示散件
     */
    private Long containerId;
    /**
     * 质量状态
     */
    private String qualityStatus;
    /**
     * 项目编号，0表示无项目
     */
    private Long projectId;
    /**
     * 项目任务编号，0表示无项目任务
     */
    private Long projectTaskId;
    /**
     * 账面数量
     */
    private BigDecimal bookQty;
    /**
     * 实盘数量
     */
    private BigDecimal actualQty;
    /**
     * 差异数量
     */
    private BigDecimal diffQty;
    /**
     * 差异调整入出库单号
     */
    private String adjustBizNo;
    /**
     * 明细状态：PENDING、COUNTED、DIFF、ADJUSTED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;

}