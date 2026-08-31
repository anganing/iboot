package com.iboot.pf.module.aps.dal.dataobject.scenario;

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

import java.time.LocalDateTime;

/**
 * APS-排程场景 DO。
 *
 * <p>一个场景 = 一段计划窗口 + 一套策略 + 一份输入快照，是排程的输入边界；
 * 版本、任务、缺料、负荷都挂在场景下的版本上。
 *
 * <p>主键用 {@link IdType#ASSIGN_ID}：aps 各表的 id 都不是数据库自增列，交给雪花算法分配。
 */
@TableName("aps_scenario")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsScenarioDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 排程场景编码，为空时由编码规则 APS_SCENARIO_CODE 分配。 */
    private String code;

    /** 排程场景名称。 */
    private String name;

    /** 工厂编号。 */
    private Long plantId;

    /** 车间编号，0 表示全工厂。 */
    private Long workshopId;

    /** 排程策略编号。 */
    private Long strategyId;

    /** 计划窗口开始时间。 */
    private LocalDateTime planStartTime;

    /** 计划窗口结束时间。 */
    private LocalDateTime planEndTime;

    /** 输入数据快照时间，投影刷新时写入。 */
    private LocalDateTime snapshotTime;

    /** 状态，取值见 {@link com.iboot.pf.module.aps.enums.ApsScenarioStatusEnum}。 */
    private String status;

    /** 备注。 */
    private String remark;

}
