package com.iboot.pf.module.aps.api.scenario.dto;

import lombok.Data;
import java.time.LocalDateTime;

/** 排程场景响应。 */
@Data
public class ScenarioRespDTO {
    /** 场景编号。 */ private Long id;
    /** 场景编码。 */ private String code;
    /** 场景名称。 */ private String name;
    /** 工厂编号。 */ private Long plantId;
    /** 车间编号，0 表示全工厂。 */ private Long workshopId;
    /** 策略编号。 */ private Long strategyId;
    /** 计划窗口开始时间。 */ private LocalDateTime planStartTime;
    /** 计划窗口结束时间。 */ private LocalDateTime planEndTime;
    /** 输入快照时间。 */ private LocalDateTime snapshotTime;
    /** 状态：DRAFT、SNAPSHOTTING、READY、SOLVING、SOLVED、CLOSED。 */ private String status;
    /** 备注。 */ private String remark;
}
