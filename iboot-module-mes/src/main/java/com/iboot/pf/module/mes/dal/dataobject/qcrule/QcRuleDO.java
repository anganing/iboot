package com.iboot.pf.module.mes.dal.dataobject.qcrule;

import com.baomidou.mybatisplus.annotation.KeySequence;
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
 * MES-质检触发规则 DO
 *
 * @author iboot
 */
@TableName("mes_qc_rule")
@KeySequence("mes_qc_rule_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QcRuleDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 质检规则编码，由 infra 编码规则 MES_QC_RULE_CODE 分配
     */
    private String code;
    /**
     * 质检规则名称
     */
    private String name;
    /**
     * 触发点
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum}
     */
    private String triggerPoint;
    /**
     * 检验对象类型
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesQcObjectTypeEnum}
     */
    private String objectType;
    /**
     * 指定物料编号，0 表示不限
     */
    private Long matId;
    /**
     * 指定物料分类编号，0 表示不限
     */
    private Long matCatId;
    /**
     * 指定工序编号，0 表示不限
     */
    private Long opId;
    /**
     * 指定工作中心编号，0 表示不限
     */
    private Long wcId;
    /**
     * 指定供应商编号，0 表示不限
     */
    private Long supplierId;
    /**
     * 检验方案编号，只能引用已发布的方案
     */
    private Long qcPlanId;
    /**
     * 检验模式
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesQcModeEnum}
     */
    private String qcMode;
    /**
     * 优先级，数值越大越优先
     */
    private Integer priority;
    /**
     * 状态
     *
     * 枚举 {@link com.iboot.pf.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
