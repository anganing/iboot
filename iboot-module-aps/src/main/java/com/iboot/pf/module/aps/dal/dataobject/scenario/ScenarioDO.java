package com.iboot.pf.module.aps.dal.dataobject.scenario;

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
 * 排程场景 DO
 *
 * @author iboot
 */
@TableName("aps_scenario")
@KeySequence("aps_scenario_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 排程场景编码
     */
    private String code;
    /**
     * 排程场景名称
     */
    private String name;
    /**
     * 工厂编号
     */
    private Long plantId;
    /**
     * 车间编号，0表示全工厂
     */
    private Long workshopId;
    /**
     * 排程策略编号
     */
    private Long strategyId;
    /**
     * 计划窗口开始时间
     */
    private LocalDateTime planStartTime;
    /**
     * 计划窗口结束时间
     */
    private LocalDateTime planEndTime;
    /**
     * 输入数据快照时间
     */
    private LocalDateTime snapshotTime;
    /**
     * 状态：DRAFT、SNAPSHOTTING、READY、SOLVING、SOLVED、CLOSED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;


}