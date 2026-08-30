package com.iboot.pf.module.mes.dal.dataobject.optask;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-工序任务 DO
 *
 * @author iboot
 */
@TableName("mes_op_task")
@KeySequence("mes_op_task_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpTaskDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 工序任务号
     */
    private String taskNo;
    /**
     * 作业工单编号
     */
    private Long workOrderId;
    /**
     * 作业工单号快照
     */
    private String workOrderNo;
    /**
     * 锁定的路线工序编号
     */
    private Long routeOpId;
    /**
     * 标准工序编号
     */
    private Long opId;
    /**
     * 工序编码快照
     */
    private String opCode;
    /**
     * 工序名称快照
     */
    private String opName;
    /**
     * 工序顺序号
     */
    private Integer seq;
    /**
     * 并行工序组
     */
    private String parallelGroup;
    /**
     * 前序工序任务编号
     */
    private Long prevTaskId;
    /**
     * 后序工序任务编号
     */
    private Long nextTaskId;
    /**
     * 执行模式：SEQUENTIAL、PARALLEL、OPTIONAL
     */
    private String execMode;
    /**
     * 质检模式：NONE、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE
     */
    private String qcMode;
    /**
     * 跳序策略：NOT_ALLOWED、ALLOW_WITH_REASON、APPROVAL_REQUIRED
     */
    private String skipPolicy;
    /**
     * 是否关键工序
     */
    private Boolean keyFlag;
    /**
     * 计划数量
     */
    private BigDecimal plannedQty;
    /**
     * 合格完成数量
     */
    private BigDecimal completedQty;
    /**
     * 报废数量
     */
    private BigDecimal scrapQty;
    /**
     * 当前分配资源编号
     */
    private Long resId;
    /**
     * 计划开始时间
     */
    private LocalDateTime plannedStartTime;
    /**
     * 计划结束时间
     */
    private LocalDateTime plannedEndTime;
    /**
     * 实际开始时间
     */
    private LocalDateTime actualStartTime;
    /**
     * 实际结束时间
     */
    private LocalDateTime actualEndTime;
    /**
     * 状态：PENDING、READY、RUNNING、PAUSED、QC_WAITING、HOLD、DONE、SKIPPED、CANCELLED
     */
    private String status;
    /**
     * 乐观锁版本
     */
    private Integer version;
    /**
     * 备注
     */
    private String remark;


}