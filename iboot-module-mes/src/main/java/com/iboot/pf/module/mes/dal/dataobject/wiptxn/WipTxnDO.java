package com.iboot.pf.module.mes.dal.dataobject.wiptxn;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-在制流转流水 DO
 *
 * @author iboot
 */
@TableName("mes_wip_txn")
@KeySequence("mes_wip_txn_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WipTxnDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 在制事务编号
     */
    private String txnNo;
    /**
     * 生产工单编号
     */
    private Long prodOrderId;
    /**
     * 作业工单编号
     */
    private Long workOrderId;
    /**
     * 工序任务编号
     */
    private Long opTaskId;
    /**
     * 事务类型：START、PAUSE、RESUME、REPORT、MOVE、SKIP、COMPLETE、REWORK、SCRAP
     */
    private String txnType;
    /**
     * 事务数量
     */
    private BigDecimal qty;
    /**
     * 变更前状态
     */
    private String fromStatus;
    /**
     * 变更后状态
     */
    private String toStatus;
    /**
     * 来源业务类型
     */
    private String bizType;
    /**
     * 来源业务单号
     */
    private String bizNo;
    /**
     * 来源工序任务编号，0表示无
     */
    private Long fromOpTaskId;
    /**
     * 目标工序任务编号，0表示无
     */
    private Long toOpTaskId;
    /**
     * 事务发生时间
     */
    private LocalDateTime txnTime;
    /**
     * 事务幂等键
     */
    private String idempotencyKey;
    /**
     * 备注
     */
    private String remark;


}