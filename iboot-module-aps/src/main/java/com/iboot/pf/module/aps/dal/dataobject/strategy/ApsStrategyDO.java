package com.iboot.pf.module.aps.dal.dataobject.strategy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * APS-排程策略 DO。
 *
 * <p>一个策略 = 一份优化目标 + 一套硬软约束权重，场景引用策略后由求解器按它排程。
 * 编码与版本号联合唯一（uk_aps_strategy_code_ver_deleted），同一策略可保留多个版本做对比。
 */
@TableName("aps_strategy")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsStrategyDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 排程策略编码，为空时由编码规则 APS_STRATEGY_CODE 分配。 */
    private String code;

    /** 排程策略名称。 */
    private String name;

    /** 策略版本号。 */
    private String versionNo;

    /** 优化目标说明。 */
    private String objective;

    /** 硬约束、软约束及权重配置，JSON。 */
    private String configJson;

    /** 状态，取值见 {@link com.iboot.pf.module.aps.enums.ApsStrategyStatusEnum}。 */
    private String status;

    /** 备注。 */
    private String remark;

}
