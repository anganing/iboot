package com.iboot.pf.module.mes.dal.dataobject.qctask;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-质检任务 DO
 *
 * @author iboot
 */
@TableName("mes_qc_task")
@KeySequence("mes_qc_task_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
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
     * 质检任务号
     */
    private String taskNo;
    /**
     * 命中的质检规则编号
     */
    private Long ruleId;
    /**
     * 锁定的检验方案编号
     */
    private Long qcPlanId;
    /**
     * 锁定的检验方案版本
     */
    private String qcPlanVersion;
    /**
     * 触发点：IQC、IPQC、FQC、OQC、FAI
     */
    private String triggerPoint;
    /**
     * 来源业务类型
     */
    private String bizType;
    /**
     * 来源业务单号
     */
    private String bizNo;
    /**
     * 来源业务明细编号
     */
    private Long bizLineId;
    /**
     * 检验物料编号
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
     * 检验批次编号
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
     * 抽样数量
     */
    private BigDecimal sampleQty;
    /**
     * 计量单位编号
     */
    private Long uomId;
    /**
     * 计量单位编码快照
     */
    private String uomCode;
    /**
     * 指派检验员编号
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
     * 检验结论：QUALIFIED、REJECTED、CONCESSION
     */
    private String conclusion;
    /**
     * 合格数量
     */
    private BigDecimal qtyQualified;
    /**
     * 不合格数量
     */
    private BigDecimal qtyRejected;
    /**
     * 状态：CREATED、ASSIGNED、INSPECTING、RESULT_POSTING、DONE、CANCELLED
     */
    private String status;
    /**
     * 创建幂等键
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