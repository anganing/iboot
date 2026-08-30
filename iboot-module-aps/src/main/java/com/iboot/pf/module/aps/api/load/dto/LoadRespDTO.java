package com.iboot.pf.module.aps.api.load.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/** 资源日负荷响应。 */
@Data
public class LoadRespDTO {
    /** 负荷记录编号。 */ private Long id;
    /** 版本编号。 */ private Long versionId;
    /** 资源编号。 */ private Long resId;
    /** 资源编码。 */ private String resCode;
    /** 资源名称。 */ private String resName;
    /** 负荷日期。 */ private LocalDate workDate;
    /** 计划负荷分钟数。 */ private Integer loadMinutes;
    /** 可用产能分钟数。 */ private Integer capacityMinutes;
    /** 负荷率。 */ private BigDecimal loadRate;
    /** 超负荷分钟数。 */ private Integer overloadMinutes;
}
