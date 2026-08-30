package com.iboot.pf.module.mdm.api.tool.dto;

import lombok.Data;

/**
 * 工装模具刀具 Response DTO
 *
 * @author iboot
 */
@Data
public class ToolRespDTO {

    /** 工装编号 */
    private Long id;

    /** 工装编码 */
    private String code;

    /** 工装名称 */
    private String name;

    /** 工装类型：FIXTURE-工装，MOLD-模具，CUTTER-刀具，GAUGE-量具 */
    private String toolType;

    /** 所属工作中心编号 */
    private Long wcId;

    /** 保有数量 */
    private Integer qtyTotal;

    /** 寿命类型：COUNT-次数，TIME-时长 */
    private String lifeType;

    /** 寿命上限 */
    private Integer lifeLimit;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
