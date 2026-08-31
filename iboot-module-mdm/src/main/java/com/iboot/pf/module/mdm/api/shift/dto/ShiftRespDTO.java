package com.iboot.pf.module.mdm.api.shift.dto;

import lombok.Data;

import java.time.LocalTime;

/**
 * 班次 Response DTO
 *
 * @author iboot
 */
@Data
public class ShiftRespDTO {

    /** 班次编号 */
    private Long id;

    /** 班次编码 */
    private String code;

    /** 班次名称 */
    private String name;

    /** 开始时间 */
    private LocalTime startTime;

    /** 结束时间 */
    private LocalTime endTime;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
