package com.iboot.pf.module.mdm.api.skill.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 人员技能 Response DTO
 *
 * @author iboot
 */
@Data
public class MemberSkillRespDTO {

    /** 记录编号 */
    private Long id;

    /** system 用户编号 */
    private Long userId;

    /** 技能编号 */
    private Long skillId;

    /** 技能等级 */
    private Integer level;

    /** 生效日期 */
    private LocalDate effectiveDate;

    /** 失效日期 */
    private LocalDate expiryDate;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
