package com.iboot.pf.module.mdm.dal.dataobject.op;

import lombok.*;
import java.math.BigDecimal;
import java.util.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 标准工序 DO
 *
 * @author iboot
 */
@TableName("mdm_op")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 编码
     */
    private String code;
    /**
     * 名称
     */
    private String name;
    /**
     * 标准时长（分钟），仅 FIXED 时长类型取值
     */
    private Integer stdDuration;
    /**
     * 准备/换型工时（分钟）
     */
    private Integer setupMinutes;
    /**
     * 单件加工工时（分钟）
     */
    private BigDecimal stdMinutesPerUnit;
    /**
     * 收尾工时（分钟）
     */
    private Integer teardownMinutes;
    /**
     * 排队等待时间（分钟）
     */
    private Integer queueMinutes;
    /**
     * 转移时间（分钟）
     */
    private Integer moveMinutes;
    /**
     * 时长类型：FIXED-固定工时，PER_UNIT-按件累加
     */
    private String durationType;
    /**
     * 标准人数
     */
    private Integer manQty;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;


}
