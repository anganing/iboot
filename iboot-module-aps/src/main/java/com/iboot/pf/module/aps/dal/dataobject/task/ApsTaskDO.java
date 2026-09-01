package com.iboot.pf.module.aps.dal.dataobject.task;

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

import java.time.LocalDateTime;

/**
 * APS-工序排程任务 DO。
 *
 * <p>排程结果的最小单位：一道工序在某个资源上的一段时间。工单号、工序编码、资源编码都是写入时
 * 落库的快照，主数据后续改名不影响已发布的计划。
 */
@TableName("aps_task")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsTaskDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 排程版本编号。 */
    private Long versionId;

    /** 排程任务编号，为空时由编码规则 APS_TASK_NO 分配。 */
    private String taskNo;

    /** 生产工单编号。 */
    private Long prodOrderId;

    /** 生产工单号快照。 */
    private String prodOrderNo;

    /** 作业工单编号，0 表示无作业工单。 */
    private Long workOrderId;

    /** MES 工序任务编号。 */
    private Long opTaskId;

    /** 路线工序编号。 */
    private Long routeOpId;

    /** 工序编码快照。 */
    private String opCode;

    /** 工序名称快照。 */
    private String opName;

    /** 工厂编号。 */
    private Long plantId;

    /** 车间编号。 */
    private Long workshopId;

    /** 项目编号，0 表示无项目。 */
    private Long projectId;

    /** 项目任务编号，0 表示无项目任务。 */
    private Long projectTaskId;

    /** 排定生产资源编号。 */
    private Long resId;

    /** 资源编码快照。 */
    private String resCode;

    /** 计划开始时间。 */
    private LocalDateTime startTime;

    /** 计划结束时间。 */
    private LocalDateTime endTime;

    /** 计划占用分钟数。 */
    private Integer durationMinutes;

    /** 本次换型损失分钟数，已含在 {@link #durationMinutes} 内，单独留一份便于甘特着色与损失复盘。 */
    private Integer changeoverMinutes;

    /** 资源内执行顺序号。 */
    private Integer sequenceNo;

    /** 是否锁定不可移动。 */
    private Boolean lockedFlag;

    /** 状态，取值见 {@link com.iboot.pf.module.aps.enums.ApsTaskStatusEnum}。 */
    private String status;

    /** 备注。 */
    private String remark;

}
