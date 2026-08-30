package com.iboot.pf.module.aps.dal.dataobject.task;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * APS-工序排程任务 DO
 *
 * @author iboot
 */
@TableName("aps_task")
@KeySequence("aps_task_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 排程版本编号
     */
    private Long versionId;
    /**
     * 排程任务编号
     */
    private String taskNo;
    /**
     * 生产工单编号
     */
    private Long prodOrderId;
    /**
     * 生产工单号快照
     */
    private String prodOrderNo;
    /**
     * 作业工单编号
     */
    private Long workOrderId;
    /**
     * MES工序任务编号
     */
    private Long opTaskId;
    /**
     * 路线工序编号
     */
    private Long routeOpId;
    /**
     * 工序编码快照
     */
    private String opCode;
    /**
     * 工序名称快照
     */
    private String opName;
    /**
     * 工厂编号
     */
    private Long plantId;
    /**
     * 车间编号
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
     * 排定生产资源编号
     */
    private Long resId;
    /**
     * 资源编码快照
     */
    private String resCode;
    /**
     * 计划开始时间
     */
    private LocalDateTime startTime;
    /**
     * 计划结束时间
     */
    private LocalDateTime endTime;
    /**
     * 计划占用分钟数
     */
    private Integer durationMinutes;
    /**
     * 资源内执行顺序号
     */
    private Integer sequenceNo;
    /**
     * 是否锁定不可移动
     */
    private Boolean lockedFlag;
    /**
     * 状态：PLANNED、LOCKED、RELEASED、STARTED、DONE、CANCELLED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;


}