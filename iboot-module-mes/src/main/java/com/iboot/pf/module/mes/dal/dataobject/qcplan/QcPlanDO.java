package com.iboot.pf.module.mes.dal.dataobject.qcplan;

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

import java.math.BigDecimal;

/**
 * MES-检验方案 DO
 *
 * @author iboot
 */
@TableName("mes_qc_plan")
@KeySequence("mes_qc_plan_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QcPlanDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 检验方案编码，由 infra 编码规则 MES_QC_PLAN_CODE 分配
     */
    private String code;
    /**
     * 检验方案名称
     */
    private String name;
    /**
     * 方案版本号，与编码组成唯一索引
     */
    private String versionNo;
    /**
     * 抽样类型
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesQcSampleTypeEnum}
     */
    private String sampleType;
    /**
     * 固定数量或抽样比例
     */
    private BigDecimal sampleValue;
    /**
     * AQL 接收质量限
     */
    private String aql;
    /**
     * 接收判定规则
     */
    private String acceptanceRule;
    /**
     * 状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum}
     */
    private String status;
    /**
     * 备注
     */
    private String remark;

}
