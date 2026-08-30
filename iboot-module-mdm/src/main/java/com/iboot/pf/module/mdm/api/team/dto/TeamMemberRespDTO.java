package com.iboot.pf.module.mdm.api.team.dto;

import lombok.Data;

/**
 * 班组成员 Response DTO
 *
 * @author iboot
 */
@Data
public class TeamMemberRespDTO {

    /** 成员记录编号 */
    private Long id;

    /** 班组编号 */
    private Long teamId;

    /** system 用户编号 */
    private Long userId;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
