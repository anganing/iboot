package com.iboot.pf.module.mes.dal.dataobject.optask;

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
 * MES-工序任务 DO
 *
 * @author iboot
 */
@TableName("mes_op_task")
@KeySequence("mes_op_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpTaskDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 工序任务号，由 infra 编码规则 MES_OP_TASK_NO 分配
     */
    private String taskNo;
    /**
     * 作业工单编号
     */
    private Long workOrderId;
    /**
     * 作业工单号快照
     */
    private String workOrderNo;
    /**
     * 锁定的路线工序编号，来自 mdm_route_op
     */
    private Long routeOpId;
    /**
     * 标准工序编号，来自 mdm_op
     */
    private Long opId;
    /**
     * 工序编码快照
     */
    private String opCode;
    /**
     * 工序名称快照
     */
    private String opName;
    /**
     * 工序顺序号，取自路线工序
     */
    private Integer seq;
    /**
     * 并行工序组，取自路线工序
     */
    private String parallelGroup;
    /**
     * 前序工序任务编号，0 表示首道工序
     */
    private Long prevTaskId;
    /**
     * 后序工序任务编号，0 表示末道工序
     */
    private Long nextTaskId;
    /**
     * 执行模式
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesOpExecModeEnum}
     */
    private String execMode;
    /**
     * 质检模式
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesQcModeEnum}
     */
    private String qcMode;
    /**
     * 跳序策略
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesSkipPolicyEnum}
     */
    private String skipPolicy;
    /**
     * 是否关键工序，取自路线工序
     */
    private Boolean keyFlag;
    /**
     * 计划数量
     */
    private BigDecimal plannedQty;
    /**
     * 合格完成数量，由报工推进
     */
    private BigDecimal completedQty;
    /**
     * 报废数量，由报工推进
     */
    private BigDecimal scrapQty;
    /**
     * 当前分配资源编号，0 表示未分配，来自 mdm_res
     */
    private Long resId;
    /**
     * 计划开始时间
     */
    private LocalDateTime plannedStartTime;
    /**
     * 计划结束时间
     */
    private LocalDateTime plannedEndTime;
    /**
     * 实际开始时间，由开工动作写入
     */
    private LocalDateTime actualStartTime;
    /**
     * 实际结束时间，由完工动作写入
     */
    private LocalDateTime actualEndTime;
    /**
     * 状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum}
     */
    private String status;
    /**
     * 乐观锁版本，并发报工时由数据库保证不丢更新
     */
    private Integer version;
    /**
     * 备注
     */
    private String remark;

}
