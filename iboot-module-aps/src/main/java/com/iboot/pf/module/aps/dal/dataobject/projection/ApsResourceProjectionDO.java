package com.iboot.pf.module.aps.dal.dataobject.projection;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * APS-MDM 资源与日历投影 DO。
 *
 * <p>场景冻结的一份资源产能副本（uk_aps_projection_res_day_deleted：场景 + 资源 + 日期 + 班次）。
 * 班次编号 0 表示当日汇总产能，负荷校验的可用分钟数就取自这里。
 */
@TableName("aps_projection_resource")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsResourceProjectionDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 排程场景编号。 */
    private Long scenarioId;

    /** MDM 生产资源编号。 */
    private Long sourceResId;

    /** 资源编码快照。 */
    private String resCode;

    /** 资源名称快照。 */
    private String resName;

    /** 资源类型快照，取值见字典 mdm_res_type。 */
    private String resType;

    /** 工厂编号。 */
    private Long plantId;

    /** 车间编号。 */
    private Long workshopId;

    /** 工作中心编号。 */
    private Long wcId;

    /** 产能日期。 */
    private LocalDate workDate;

    /** 班次编号，0 表示日汇总。 */
    private Long shiftId;

    /** 可用分钟数。 */
    private Integer availableMinutes;

    /** 资源能力快照，JSON。 */
    private String capabilityJson;

    /** 来源数据更新时间。 */
    private LocalDateTime sourceUpdateTime;

    /** 投影快照时间。 */
    private LocalDateTime snapshotTime;

    /** 备注。 */
    private String remark;

}
