package com.iboot.pf.module.aps.dal.dataobject.solversnapshot;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * APS-Timefold求解快照 DO
 *
 * @author iboot
 */
@TableName("aps_solver_snapshot")
@KeySequence("aps_solver_snapshot_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolverSnapshotDO extends BaseDO {

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
     * 快照类型：INPUT、SOLUTION、CONSTRAINT_MATCH、ERROR
     */
    private String snapshotType;
    /**
     * Timefold求解输入、结果或约束命中快照
     */
    private String snapshotJson;
    /**
     * 快照内容校验值
     */
    private String checksum;
    /**
     * 快照生成时间
     */
    private LocalDateTime snapshotTime;
    /**
     * 备注
     */
    private String remark;


}