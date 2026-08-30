package com.iboot.pf.module.mes.dal.dataobject.rgvtask;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-RGV工位流转任务 DO
 *
 * @author iboot
 */
@TableName("mes_rgv_task")
@KeySequence("mes_rgv_task_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
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
     * RGV本地任务号
     */
    private String taskNo;
    /**
     * RGV外部任务号
     */
    private String externalTaskNo;
    /**
     * 任务类型：STATION_TRANSFER、BUFFER_TRANSFER、RETURN
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
     * 目标工序任务编号
     */
    private Long targetOpTaskId;
    /**
     * 来源工位或资源编号
     */
    private Long fromStationId;
    /**
     * 目标工位或资源编号
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
     * 下发状态：PENDING、SENT、FAILED
     */
    private String dispatchStatus;
    /**
     * 执行状态：CREATED、RUNNING、DONE、CANCELLED、EXCEPTION
     */
    private String status;
    /**
     * 业务幂等键
     */
    private String idempotencyKey;
    /**
     * 完成时间
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