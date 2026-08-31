package com.iboot.pf.module.mes.dal.dataobject.qctask;

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
import java.time.LocalDateTime;

/**
 * MES-质检任务 DO
 *
 * @author iboot
 */
@TableName("mes_qc_task")
@KeySequence("mes_qc_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QcTaskDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 质检任务号，由 infra 编码规则 MES_QC_TASK_NO 分配
     */
    private String taskNo;
    /**
     * 命中的质检规则编号，0 表示手工建单
     */
    private Long ruleId;
    /**
     * 锁定的检验方案编号
     */
    private Long qcPlanId;
    /**
     * 锁定的检验方案版本快照
     */
    private String qcPlanVersion;
    /**
     * 触发点
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum}
     */
    private String triggerPoint;
    /**
     * 来源业务类型
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesBizTypeEnum}
     */
    private String bizType;
    /**
     * 来源业务单号
     */
    private String bizNo;
    /**
     * 来源业务明细编号，0 表示无明细
     */
    private Long bizLineId;
    /**
     * 检验物料编号，来自 mdm_mat
     */
    private Long matId;
    /**
     * 物料编码快照
     */
    private String matCode;
    /**
     * 物料名称快照
     */
    private String matName;
    /**
     * 物料规格快照
     */
    private String matSpec;
    /**
     * 检验批次编号，0 表示不区分批次，来自 wms_batch
     */
    private Long batchId;
    /**
     * 批次号快照
     */
    private String lotNo;
    /**
     * 送检数量
     */
    private BigDecimal qty;
    /**
     * 抽样数量，不能大于送检数量
     */
    private BigDecimal sampleQty;
    /**
     * 计量单位编号，来自 mdm_uom
     */
    private Long uomId;
    /**
     * 计量单位编码快照
     */
    private String uomCode;
    /**
     * 指派检验员编号，0 表示未指派，来自 system_users
     */
    private Long assignedInspectorId;
    /**
     * 检验开始时间
     */
    private LocalDateTime inspectStartTime;
    /**
     * 检验结束时间
     */
    private LocalDateTime inspectEndTime;
    /**
     * 检验结论
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesQcConclusionEnum}
     */
    private String conclusion;
    /**
     * 合格数量，由检验结果汇总
     */
    private BigDecimal qtyQualified;
    /**
     * 不合格数量，由检验结果汇总
     */
    private BigDecimal qtyRejected;
    /**
     * 状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum}
     */
    private String status;
    /**
     * 创建幂等键，唯一索引防止同一业务重复生成质检任务
     */
    private String idempotencyKey;
    /**
     * 乐观锁版本
     */
    private Integer version;
    /**
     * 备注
     */
    private String remark;

}
