package com.iboot.pf.module.aps.dal.dataobject.tasklock;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 任务锁定 DO
 *
 * @author iboot
 */
@TableName("aps_task_lock")
@KeySequence("aps_task_lock_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskLockDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 排程任务编号
     */
    private Long taskId;
    /**
     * 锁定类型：STARTED、MANUAL、MATERIAL、RESOURCE
     */
    private String lockType;
    /**
     * 锁定原因
     */
    private String reason;
    /**
     * 锁定人员编号
     */
    private Long lockedBy;
    /**
     * 锁定时间
     */
    private LocalDateTime lockedTime;
    /**
     * 解锁时间
     */
    private LocalDateTime unlockTime;
    /**
     * 状态：LOCKED、UNLOCKED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;


}