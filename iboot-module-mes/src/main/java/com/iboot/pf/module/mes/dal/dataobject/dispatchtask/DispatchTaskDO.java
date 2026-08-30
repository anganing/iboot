package com.iboot.pf.module.mes.dal.dataobject.dispatchtask;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-派工任务 DO
 *
 * @author iboot
 */
@TableName("mes_dispatch_task")
@KeySequence("mes_dispatch_task_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchTaskDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 派工单号
     */
    private String dispatchNo;
    /**
     * 工序任务编号
     */
    private Long opTaskId;
    /**
     * 作业人员编号
     */
    private Long operatorId;
    /**
     * 作业班组编号
     */
    private Long teamId;
    /**
     * 生产资源编号
     */
    private Long resId;
    /**
     * 班次编号
     */
    private Long shiftId;
    /**
     * 计划开始时间
     */
    private LocalDateTime plannedStartTime;
    /**
     * 计划结束时间
     */
    private LocalDateTime plannedEndTime;
    /**
     * 状态：PENDING、ACCEPTED、RUNNING、DONE、CANCELLED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;


}