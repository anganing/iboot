package com.iboot.pf.module.mdm.api.resource.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 资源日历 Response DTO
 *
 * @author iboot
 */
@Data
public class ResourceCalendarRespDTO {

    /** 资源日历编号 */
    private Long id;

    /** 资源编号 */
    private Long resId;

    /** 工作日期 */
    private LocalDate workDate;

    /** 班次编号 */
    private Long shiftId;

    /** 可用产能分钟 */
    private Integer availableMinutes;

    /** 是否模板生成 */
    private Boolean generatedFlag;

}
