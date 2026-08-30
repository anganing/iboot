package com.iboot.pf.module.mdm.api.skill.dto;

import lombok.Data;

/**
 * 工序技能要求 Response DTO
 *
 * @author iboot
 */
@Data
public class OpSkillReqRespDTO {

    /** 记录编号 */
    private Long id;

    /** 标准工序编号 */
    private Long opId;

    /** 技能编号 */
    private Long skillId;

    /** 最低技能等级 */
    private Integer minLevel;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
