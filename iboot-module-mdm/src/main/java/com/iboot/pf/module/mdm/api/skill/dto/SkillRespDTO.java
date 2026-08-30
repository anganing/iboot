package com.iboot.pf.module.mdm.api.skill.dto;

import lombok.Data;

/**
 * 技能 Response DTO
 *
 * @author iboot
 */
@Data
public class SkillRespDTO {

    /** 技能编号 */
    private Long id;

    /** 技能编码 */
    private String code;

    /** 技能名称 */
    private String name;

    /** 等级体系说明 */
    private String levelDesc;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
