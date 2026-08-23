package com.iboot.pf.module.infra.dal.dataobject.coderule;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import com.iboot.pf.framework.tenant.core.aop.TenantIgnore;
import com.iboot.pf.module.infra.enums.coderule.CodeResetCycleEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 编码规则。
 */
@TableName("infra_code_rule")
@KeySequence("infra_code_rule_seq")
@TenantIgnore
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CodeRuleDO extends BaseDO {

    @TableId
    private Long id;
    private String ruleCode;
    private String name;
    private String prefix;
    private Boolean yearEnabled;
    private Boolean monthEnabled;
    private Boolean dayEnabled;
    private String suffix;
    private Integer serialLength;
    private Long serialStart;
    private Long serialStep;
    /** @see CodeResetCycleEnum */
    private Integer resetCycle;
    /** @see CommonStatusEnum */
    private Integer status;
    private String remark;

}
