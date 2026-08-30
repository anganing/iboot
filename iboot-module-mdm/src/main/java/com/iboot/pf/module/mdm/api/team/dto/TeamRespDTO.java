package com.iboot.pf.module.mdm.api.team.dto;

import lombok.Data;

/**
 * 班组 Response DTO
 *
 * @author iboot
 */
@Data
public class TeamRespDTO {

    /** 班组编号 */
    private Long id;

    /** 班组编码 */
    private String code;

    /** 班组名称 */
    private String name;

    /** 所属车间编号 */
    private Long workshopId;

    /** 常白/常夜班次编号 */
    private Long shiftId;

    /** 班组长用户编号 */
    private Long leaderUserId;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
