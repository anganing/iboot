package com.iboot.pf.module.aps.api.scenario.dto;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 关闭排程场景命令。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CloseScenarioReqDTO extends ApsCommandReqDTO {
    /** 场景编号。 */ @NotNull private Long scenarioId;
}
