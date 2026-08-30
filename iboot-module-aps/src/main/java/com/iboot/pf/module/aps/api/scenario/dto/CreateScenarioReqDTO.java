package com.iboot.pf.module.aps.api.scenario.dto;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 创建排程场景命令。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateScenarioReqDTO extends ApsCommandReqDTO {
    /** 场景编码。 */ @NotBlank private String code;
    /** 场景名称。 */ @NotBlank private String name;
    /** 工厂编号。 */ @NotNull private Long plantId;
    /** 车间编号，0 表示全工厂。 */ private Long workshopId;
    /** 排程策略编号。 */ @NotNull private Long strategyId;
    /** 计划窗口开始时间。 */ @NotNull private LocalDateTime planStartTime;
    /** 计划窗口结束时间。 */ @NotNull private LocalDateTime planEndTime;
    /** 备注。 */ private String remark;
}
