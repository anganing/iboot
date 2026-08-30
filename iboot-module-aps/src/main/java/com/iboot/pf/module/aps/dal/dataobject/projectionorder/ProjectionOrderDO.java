package com.iboot.pf.module.aps.dal.dataobject.projectionorder;

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
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * APS-MES需求工单任务投影 DO
 *
 * @author iboot
 */
@TableName("aps_projection_order")
@KeySequence("aps_projection_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectionOrderDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 排程场景编号
     */
    private Long scenarioId;
    /**
     * 来源类型：DEMAND、PROD_ORDER、OP_TASK
     */
    private String sourceType;
    /**
     * MES来源对象编号
     */
    private Long sourceId;
    /**
     * MES来源对象单号
     */
    private String sourceNo;
    /**
     * 生产工单编号，0表示尚未生成工单
     */
    private Long prodOrderId;
    /**
     * 作业工单编号，0表示尚未生成作业工单
     */
    private Long workOrderId;
    /**
     * 工序任务编号，0表示非工序投影
     */
    private Long opTaskId;
    /**
     * 工厂编号
     */
    private Long plantId;
    /**
     * 车间编号，0表示未指定
     */
    private Long workshopId;
    /**
     * 项目编号，0表示无项目
     */
    private Long projectId;
    /**
     * 项目任务编号，0表示无项目任务
     */
    private Long projectTaskId;
    /**
     * 生产物料编号
     */
    private Long matId;
    /**
     * 物料编码快照
     */
    private String matCode;
    /**
     * 路线工序编号，0表示需求或整单
     */
    private Long routeOpId;
    /**
     * 当前资源编号，0表示未分配
     */
    private Long resId;
    /**
     * 计划数量或剩余数量
     */
    private BigDecimal qty;
    /**
     * 已完成数量快照
     */
    private BigDecimal completedQty;
    /**
     * 交期
     */
    private LocalDateTime dueDate;
    /**
     * 优先级
     */
    private Integer priority;
    /**
     * 来源对象状态快照
     */
    private String currentStatus;
    /**
     * 实际开始时间
     */
    private LocalDateTime actualStartTime;
    /**
     * 实际结束时间
     */
    private LocalDateTime actualEndTime;
    /**
     * 是否已开工或业务锁定
     */
    private Boolean lockedFlag;
    /**
     * 来源数据更新时间
     */
    private LocalDateTime sourceUpdateTime;
    /**
     * 投影快照时间
     */
    private LocalDateTime snapshotTime;
    /**
     * 备注
     */
    private String remark;


}