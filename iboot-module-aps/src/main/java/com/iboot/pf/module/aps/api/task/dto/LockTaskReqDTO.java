package com.iboot.pf.module.aps.api.task.dto;

import com.iboot.pf.module.aps.api.dto.ApsCommandReqDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 锁定或解锁排程任务命令。 */
@Data @EqualsAndHashCode(callSuper = true)
public class LockTaskReqDTO extends ApsCommandReqDTO {
    /** 任务编号。 */ @NotNull private Long taskId;
    /** 锁定类型：STARTED、MANUAL、MATERIAL、RESOURCE。 */ private String lockType;
    /** 锁定或解锁原因。 */ private String reason;
    /** 操作人员编号。 */ private Long operatorId;
}
