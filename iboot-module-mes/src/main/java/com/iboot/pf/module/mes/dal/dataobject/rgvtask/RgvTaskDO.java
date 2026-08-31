package com.iboot.pf.module.mes.dal.dataobject.rgvtask;

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

import java.time.LocalDateTime;

/**
 * MES-RGV 工位流转任务 DO
 *
 * @author iboot
 */
@TableName("mes_rgv_task")
@KeySequence("mes_rgv_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RgvTaskDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * RGV 本地任务号，由 infra 编码规则 MES_RGV_TASK_NO 分配
     */
    private String taskNo;
    /**
     * RGV 外部任务号，与外部调度系统编码组成唯一索引
     */
    private String externalTaskNo;
    /**
     * 任务类型
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesRgvTaskTypeEnum}
     */
    private String taskType;
    /**
     * 作业工单编号
     */
    private Long workOrderId;
    /**
     * 来源工序任务编号
     */
    private Long opTaskId;
    /**
     * 目标工序任务编号，0 表示回库
     */
    private Long targetOpTaskId;
    /**
     * 来源工位或资源编号，来自 mdm_res
     */
    private Long fromStationId;
    /**
     * 目标工位或资源编号，来自 mdm_res
     */
    private Long toStationId;
    /**
     * 载具编号
     */
    private String carrierNo;
    /**
     * 外部调度系统编码
     */
    private String externalSystemCode;
    /**
     * 下发状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesRgvDispatchStatusEnum}
     */
    private String dispatchStatus;
    /**
     * 执行状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesRgvTaskStatusEnum}
     */
    private String status;
    /**
     * 业务幂等键，唯一索引防止外部系统重复下发
     */
    private String idempotencyKey;
    /**
     * 完成时间，由外部调度系统回报
     */
    private LocalDateTime completedTime;
    /**
     * 乐观锁版本
     */
    private Integer version;
    /**
     * 备注
     */
    private String remark;

}
