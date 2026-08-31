package com.iboot.pf.module.mdm.api.op.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 标准工序 Response DTO
 *
 * @author iboot
 */
@Data
public class OpRespDTO {

    /** 标准工序编号 */
    private Long id;

    /** 工序编码 */
    private String code;

    /** 工序名称 */
    private String name;

    /** 标准时长（分钟），仅 FIXED 时长类型取值 */
    private Integer stdDuration;

    /** 准备/换型工时（分钟） */
    private Integer setupMinutes;

    /** 单件加工工时（分钟） */
    private BigDecimal stdMinutesPerUnit;

    /** 收尾工时（分钟） */
    private Integer teardownMinutes;

    /** 排队等待时间（分钟） */
    private Integer queueMinutes;

    /** 转移时间（分钟） */
    private Integer moveMinutes;

    /** 时长类型：FIXED-固定工时，PER_UNIT-按件累加 */
    private String durationType;

    /** 标准人数 */
    private Integer manQty;

    /** 状态：0-启用，1-停用 */
    private Integer status;

}
