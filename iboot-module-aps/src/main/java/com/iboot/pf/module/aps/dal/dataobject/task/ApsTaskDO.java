package com.iboot.pf.module.aps.dal.dataobject.task;

import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import java.time.LocalDateTime;

/** APS 工序排程任务。 */
@TableName("aps_task") @Data @EqualsAndHashCode(callSuper = true) @Builder @NoArgsConstructor @AllArgsConstructor
public class ApsTaskDO extends BaseDO {
    @TableId(type = IdType.ASSIGN_ID) private Long id;
    private Long versionId; private String taskNo; private Long prodOrderId; private String prodOrderNo;
    private Long workOrderId; private Long opTaskId; private Long routeOpId; private String opCode; private String opName;
    private Long plantId; private Long workshopId; private Long projectId; private Long projectTaskId;
    private Long resId; private String resCode; private LocalDateTime startTime; private LocalDateTime endTime;
    private Integer durationMinutes; private Integer sequenceNo; private Boolean lockedFlag; private String status; private String remark;
}
