package com.iboot.pf.module.aps.dal.dataobject.projectionresource;


import java.time.LocalDate;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * APS-MDM资源与日历投影 DO
 *
 * @author iboot
 */
@TableName("aps_projection_resource")
@KeySequence("aps_projection_resource_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectionResourceDO extends BaseDO {

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
     * MDM生产资源编号
     */
    private Long sourceResId;
    /**
     * 资源编码快照
     */
    private String resCode;
    /**
     * 资源名称快照
     */
    private String resName;
    /**
     * 资源类型快照
     */
    private String resType;
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
     * 产能日期
     */
    private LocalDate workDate;
    /**
     * 班次编号，0表示日汇总
     */
    private Long shiftId;
    /**
     * 可用分钟数
     */
    private Integer availableMinutes;
    /**
     * 资源能力快照
     */
    private String capabilityJson;
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