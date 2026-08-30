package com.iboot.pf.module.mes.dal.dataobject.qcrule;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-质检触发规则 DO
 *
 * @author iboot
 */
@TableName("mes_qc_rule")
@KeySequence("mes_qc_rule_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
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
     * 质检规则编码
     */
    private String code;
    /**
     * 质检规则名称
     */
    private String name;
    /**
     * 触发点：IQC、IPQC、FQC、OQC、FAI
     */
    private String triggerPoint;
    /**
     * 检验对象类型：MATERIAL、BATCH、OP_TASK、PROD_ORDER、OUT_ORDER
     */
    private String objectType;
    /**
     * 指定物料编号，0表示不限
     */
    private Long matId;
    /**
     * 指定物料分类编号，0表示不限
     */
    private Long matCatId;
    /**
     * 指定工序编号，0表示不限
     */
    private Long opId;
    /**
     * 指定工作中心编号，0表示不限
     */
    private Long wcId;
    /**
     * 指定供应商编号，0表示不限
     */
    private Long supplierId;
    /**
     * 检验方案编号
     */
    private Long qcPlanId;
    /**
     * 检验模式：PRE_INBOUND、POST_INBOUND_PENDING、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE
     */
    private String qcMode;
    /**
     * 优先级，数值越大越优先
     */
    private Integer priority;
    /**
     * 状态：0-启用，1-停用
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;


}