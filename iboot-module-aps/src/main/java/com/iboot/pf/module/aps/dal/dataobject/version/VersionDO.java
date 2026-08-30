package com.iboot.pf.module.aps.dal.dataobject.version;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * APS-排程版本 DO
 *
 * @author iboot
 */
@TableName("aps_version")
@KeySequence("aps_version_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionDO extends BaseDO {

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
     * 排程版本号
     */
    private Integer versionNo;
    /**
     * 状态：SOLVING、SOLVED、PENDING_CONFIRM、PUBLISHED、REPLACED、FAILED
     */
    private String status;
    /**
     * Timefold综合得分
     */
    private String score;
    /**
     * 硬约束得分
     */
    private Long hardScore;
    /**
     * 软约束得分
     */
    private Long softScore;
    /**
     * 求解开始时间
     */
    private LocalDateTime solveStartTime;
    /**
     * 求解结束时间
     */
    private LocalDateTime solveEndTime;
    /**
     * 发布时间
     */
    private LocalDateTime publishedTime;
    /**
     * 发布人员编号
     */
    private Long publishedBy;
    /**
     * 求解结果或失败信息
     */
    private String solverMessage;
    /**
     * 乐观锁版本
     */
    private Integer version;
    /**
     * 备注
     */
    private String remark;


}