package com.iboot.pf.module.aps.api.scenario.dto;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 复制排程场景命令。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CopyScenarioReqDTO extends ApsCommandReqDTO {
    /** 来源场景编号。 */ @NotNull private Long sourceScenarioId;
    /** 新场景编码。 */ @NotBlank private String code;
    /** 新场景名称。 */ @NotBlank private String name;
}
