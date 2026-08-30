package com.iboot.pf.module.wms.dal.dataobject.strategy;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-仓储策略 DO
 *
 * @author iboot
 */
@TableName("wms_strategy")
@KeySequence("wms_strategy_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsStrategyDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 策略编码
     */
    private String code;
    /**
     * 策略名称
     */
    private String name;
    /**
     * 策略类型：PUTAWAY、PICK、REPLENISH
     */
    private String strategyType;
    /**
     * 适用仓库编号，0表示全局
     */
    private Long whId;
    /**
     * 策略版本号
     */
    private String versionNo;
    /**
     * 策略条件与排序规则配置
     */
    private String configJson;
    /**
     * 优先级，数值越大越优先
     */
    private Integer priority;
    /**
     * 状态：DRAFT、ENABLED、DISABLED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;


}