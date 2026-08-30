package com.iboot.pf.module.aps.api.version.dto;

import lombok.Data;
import java.time.LocalDateTime;

/** 排程版本响应。 */
@Data
public class VersionRespDTO {
    /** 版本编号。 */ private Long id;
    /** 场景编号。 */ private Long scenarioId;
    /** 场景内版本号。 */ private Integer versionNo;
    /** 状态：SOLVING、CONFIRMED、PUBLISHED、REPLACED、FAILED。 */ private String status;
    /** 综合得分。 */ private String score;
    /** 硬约束得分。 */ private Long hardScore;
    /** 软约束得分。 */ private Long softScore;
    /** 发布时间。 */ private LocalDateTime publishedTime;
    /** 发布人员编号。 */ private Long publishedBy;
    /** 求解信息。 */ private String solverMessage;
}
