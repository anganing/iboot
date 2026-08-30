package com.iboot.pf.module.aps.dal.dataobject.taskdep;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 任务依赖 DO
 *
 * @author iboot
 */
@TableName("aps_task_dep")
@KeySequence("aps_task_dep_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDepDO extends BaseDO {

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
     * 前置排程任务编号
     */
    private Long fromTaskId;
    /**
     * 后置排程任务编号
     */
    private Long toTaskId;
    /**
     * 依赖类型：FINISH_START、START_START、FINISH_FINISH
     */
    private String depType;
    /**
     * 依赖间隔分钟数
     */
    private Integer lagMinutes;
    /**
     * 备注
     */
    private String remark;


}