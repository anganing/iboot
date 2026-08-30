package com.iboot.pf.module.aps.api.version.dto;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 生成排程版本命令。 */
@Data @EqualsAndHashCode(callSuper = true)
public class GenerateVersionReqDTO extends ApsCommandReqDTO {
    /** 场景编号。 */ @NotNull private Long scenarioId;
    /** 备注。 */ private String remark;
}
