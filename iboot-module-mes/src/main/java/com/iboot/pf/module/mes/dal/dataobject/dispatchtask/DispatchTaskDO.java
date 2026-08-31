package com.iboot.pf.module.mes.dal.dataobject.dispatchtask;

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
 * MES-派工任务 DO
 *
 * @author iboot
 */
@TableName("mes_dispatch_task")
@KeySequence("mes_dispatch_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchTaskDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 派工单号，由 infra 编码规则 MES_DISPATCH_NO 分配
     */
    private String dispatchNo;
    /**
     * 工序任务编号
     */
    private Long opTaskId;
    /**
     * 作业人员编号，0 表示派给班组，来自 system_users
     */
    private Long operatorId;
    /**
     * 作业班组编号，0 表示派给个人，来自 mdm_team
     */
    private Long teamId;
    /**
     * 生产资源编号，来自 mdm_res
     */
    private Long resId;
    /**
     * 班次编号，0 表示不区分班次，来自 mdm_shift
     */
    private Long shiftId;
    /**
     * 计划开始时间
     */
    private LocalDateTime plannedStartTime;
    /**
     * 计划结束时间
     */
    private LocalDateTime plannedEndTime;
    /**
     * 状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesDispatchTaskStatusEnum}
     */
    private String status;
    /**
     * 备注
     */
    private String remark;

}
