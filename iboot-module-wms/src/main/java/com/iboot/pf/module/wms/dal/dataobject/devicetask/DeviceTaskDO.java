package com.iboot.pf.module.wms.dal.dataobject.devicetask;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-设备搬运任务 DO
 *
 * @author iboot
 */
@TableName("wms_device_task")
@KeySequence("wms_device_task_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTaskDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 本地设备任务号
     */
    private String taskNo;
    /**
     * 外部设备任务号
     */
    private String externalTaskNo;
    /**
     * 任务类型：PUTAWAY、PICK、TRANSFER、DELIVERY、RETURN
     */
    private String taskType;
    /**
     * 设备类型：WCS、AGV、RGV
     */
    private String deviceType;
    /**
     * 来源业务类型
     */
    private String sourceBizType;
    /**
     * 来源业务单号
     */
    private String sourceBizNo;
    /**
     * 来源业务明细编号
     */
    private Long sourceLineId;
    /**
     * 容器编号，0表示无容器
     */
    private Long containerId;
    /**
     * 来源库位编号
     */
    private Long fromLocId;
    /**
     * 目标库位编号
     */
    private Long toLocId;
    /**
     * 设备回传实际库位编号
     */
    private Long actualLocId;
    /**
     * 外部设备系统编码
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
     * 库存过账状态：PENDING、POSTED、FAILED
     */
    private String postStatus;
    /**
     * 下发业务快照
     */
    private String requestSnapshotJson;
    /**
     * 完成回调快照
     */
    private String callbackSnapshotJson;
    /**
     * 业务幂等键
     */
    private String idempotencyKey;
    /**
     * 设备完成时间
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