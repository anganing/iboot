package com.iboot.pf.module.mes.dal.dataobject.workorder;

import lombok.*;
import java.util.*;
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
 * MES-作业工单 DO
 *
 * @author iboot
 */
@TableName("mes_work_order")
@KeySequence("mes_work_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 作业工单号
     */
    private String workOrderNo;
    /**
     * 生产工单编号
     */
    private Long prodOrderId;
    /**
     * 生产工单号快照
     */
    private String prodOrderNo;
    /**
     * 工厂编号
     */
    private Long plantId;
    /**
     * 车间编号
     */
    private Long workshopId;
    /**
     * 工作中心编号
     */
    private Long wcId;
    /**
     * 项目编号，0表示无项目
     */
    private Long projectId;
    /**
     * 项目任务编号，0表示无项目任务
     */
    private Long projectTaskId;
    /**
     * 计划数量
     */
    private BigDecimal plannedQty;
    /**
     * 已完成数量
     */
    private BigDecimal completedQty;
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
     * 齐套状态
     */
    private String kitStatus;
    /**
     * 状态：PENDING、READY、RUNNING、HOLD、DONE、CANCELLED
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