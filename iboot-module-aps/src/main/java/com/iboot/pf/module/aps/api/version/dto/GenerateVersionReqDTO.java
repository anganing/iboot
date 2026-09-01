package com.iboot.pf.module.aps.api.version.dto;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 生成排程版本命令。 */
@Data @EqualsAndHashCode(callSuper = true)
public class GenerateVersionReqDTO extends ApsCommandReqDTO {
    /** 场景编号。 */ @NotNull private Long scenarioId;
    /** 排程策略编号；为空时用场景上引用的策略。 */ private Long strategyId;
    /** 操作人员编号，求解进度按它定向推送。 */ private Long operatorId;
    /** 备注。 */ private String remark;
}
