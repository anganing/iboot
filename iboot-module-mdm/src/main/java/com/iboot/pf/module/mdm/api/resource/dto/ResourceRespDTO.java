package com.iboot.pf.module.mdm.api.resource.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 生产资源 Response DTO
 *
 * @author iboot
 */
@Data
public class ResourceRespDTO {

    /** 资源编号 */
    private Long id;

    /** 工作中心编号 */
    private Long wcId;

    /** 资源类型 */
    private String resType;

    /** 资源编码 */
    private String code;

    /** 资源名称 */
    private String name;

    /** 效率系数 */
    private BigDecimal efficiency;

    /** 可同时承载任务数 */
    private Integer parallelCapacity;

    /** 资源组，同组可互替排程 */
    private String resGroup;

    /** 默认日历模板编号 */
    private Long calendarId;

    /** 工厂编号（冗余） */
    private Long plantId;

    /** 车间编号（冗余） */
    private Long workshopId;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
