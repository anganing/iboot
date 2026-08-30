package com.iboot.pf.module.mdm.api.project.dto;

import lombok.Data;

/**
 * 项目 Response DTO
 *
 * @author iboot
 */
@Data
public class ProjectRespDTO {

    /** 项目编号 */
    private Long id;

    /** 项目编码 */
    private String code;

    /** 项目名称 */
    private String name;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
