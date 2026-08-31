package com.iboot.pf.module.aps.dal.dataobject.tasklock;

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
 * APS-排程任务锁定记录 DO。
 *
 * <p>任务被锁定的留痕：谁在什么时候、因为什么锁住了这道工序。同一任务同时只允许一条锁定中的记录，
 * 解锁时把状态改为已解锁并写解锁时间，而不是物理删除，否则查不到锁定历史。
 */
@TableName("aps_task_lock")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsTaskLockDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 排程任务编号。 */
    private Long taskId;

    /** 锁定类型，取值见 {@link com.iboot.pf.module.aps.enums.ApsTaskLockTypeEnum}。 */
    private String lockType;

    /** 锁定原因。 */
    private String reason;

    /** 锁定人员编号。 */
    private Long lockedBy;

    /** 锁定时间。 */
    private LocalDateTime lockedTime;

    /** 解锁时间。 */
    private LocalDateTime unlockTime;

    /** 状态，取值见 {@link com.iboot.pf.module.aps.enums.ApsTaskLockStatusEnum}。 */
    private String status;

    /** 备注。 */
    private String remark;

}
