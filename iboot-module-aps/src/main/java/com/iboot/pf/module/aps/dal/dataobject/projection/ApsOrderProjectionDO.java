package com.iboot.pf.module.aps.dal.dataobject.projection;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * APS-MES 需求工单任务投影 DO。
 *
 * <p>场景冻结的一份待排产需求副本（uk_aps_projection_order_deleted：场景 + 来源类型 + 来源编号）。
 * 已开工的记录带 lockedFlag，求解时不允许挪动。
 */
@TableName("aps_projection_order")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsOrderProjectionDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 排程场景编号。 */
    private Long scenarioId;

    /** 来源类型，取值见 {@link com.iboot.pf.module.aps.enums.ApsProjectionSourceTypeEnum}。 */
    private String sourceType;

    /** MES 来源对象编号。 */
    private Long sourceId;

    /** MES 来源对象单号。 */
    private String sourceNo;

    /** 生产工单编号，0 表示尚未生成工单。 */
    private Long prodOrderId;

    /** 作业工单编号，0 表示尚未生成作业工单。 */
    private Long workOrderId;

    /** 工序任务编号，0 表示非工序投影。 */
    private Long opTaskId;

    /** 工厂编号。 */
    private Long plantId;

    /** 车间编号，0 表示未指定。 */
    private Long workshopId;

    /** 项目编号，0 表示无项目。 */
    private Long projectId;

    /** 项目任务编号，0 表示无项目任务。 */
    private Long projectTaskId;

    /** 生产物料编号。 */
    private Long matId;

    /** 物料编码快照。 */
    private String matCode;

    /** 路线工序编号，0 表示需求或整单。 */
    private Long routeOpId;

    /** 当前资源编号，0 表示未分配。 */
    private Long resId;

    /** 计划数量或剩余数量。 */
    private BigDecimal qty;

    /** 已完成数量快照。 */
    private BigDecimal completedQty;

    /** 交期。 */
    private LocalDateTime dueDate;

    /** 优先级，数值越大越优先。 */
    private Integer priority;

    /** 来源对象状态快照，取值见字典 mes_op_task_status。 */
    private String currentStatus;

    /** 实际开始时间。 */
    private LocalDateTime actualStartTime;

    /** 实际结束时间。 */
    private LocalDateTime actualEndTime;

    /** 是否已开工或业务锁定。 */
    private Boolean lockedFlag;

    /** 来源数据更新时间。 */
    private LocalDateTime sourceUpdateTime;

    /** 投影快照时间。 */
    private LocalDateTime snapshotTime;

    /** 备注。 */
    private String remark;

}
