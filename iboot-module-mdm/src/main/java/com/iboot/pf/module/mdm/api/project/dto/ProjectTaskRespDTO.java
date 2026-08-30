package com.iboot.pf.module.mdm.api.project.dto;

import lombok.Data;

/**
 * 项目任务 Response DTO
 *
 * @author iboot
 */
@Data
public class ProjectTaskRespDTO {

    /** 项目任务编号 */
    private Long id;

    /** 项目编号 */
    private Long projectId;

    /** 任务编码 */
    private String code;

    /** 任务名称 */
    private String name;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
