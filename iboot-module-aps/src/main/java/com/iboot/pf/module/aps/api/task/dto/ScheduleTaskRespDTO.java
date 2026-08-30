package com.iboot.pf.module.aps.api.task.dto;

import lombok.Data;
import java.time.LocalDateTime;

/** 工序排程任务响应。 */
@Data
public class ScheduleTaskRespDTO {
    /** 任务编号。 */ private Long id;
    /** 版本编号。 */ private Long versionId;
    /** 任务单号。 */ private String taskNo;
    /** 生产工单编号。 */ private Long prodOrderId;
    /** 生产工单号。 */ private String prodOrderNo;
    /** 作业工单编号。 */ private Long workOrderId;
    /** MES 工序任务编号。 */ private Long opTaskId;
    /** 路线工序编号。 */ private Long routeOpId;
    /** 工序编码。 */ private String opCode;
    /** 工序名称。 */ private String opName;
    /** 资源编号。 */ private Long resId;
    /** 资源编码。 */ private String resCode;
    /** 开始时间。 */ private LocalDateTime startTime;
    /** 结束时间。 */ private LocalDateTime endTime;
    /** 占用分钟数。 */ private Integer durationMinutes;
    /** 是否锁定。 */ private Boolean lockedFlag;
    /** 状态：PLANNED、LOCKED、RELEASED、STARTED、DONE、CANCELLED。 */ private String status;
}
