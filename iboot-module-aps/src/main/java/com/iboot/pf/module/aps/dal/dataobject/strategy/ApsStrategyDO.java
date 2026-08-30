package com.iboot.pf.module.aps.dal.dataobject.strategy;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 排程策略 DO
 *
 * @author iboot
 */
@TableName("aps_strategy")
@KeySequence("aps_strategy_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsStrategyDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 排程策略编码
     */
    private String code;
    /**
     * 排程策略名称
     */
    private String name;
    /**
     * 策略版本号
     */
    private String versionNo;
    /**
     * 优化目标说明
     */
    private String objective;
    /**
     * 硬约束、软约束及权重配置
     */
    private String configJson;
    /**
     * 状态：DRAFT、ENABLED、DISABLED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;


}