package com.iboot.pf.module.mes.dal.dataobject.opskiprecord;

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
 * MES-工序跳过记录 DO
 *
 * @author iboot
 */
@TableName("mes_op_skip_record")
@KeySequence("mes_op_skip_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpSkipRecordDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 被跳过工序任务编号
     */
    private Long opTaskId;
    /**
     * 跳过原因类型
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesSkipReasonTypeEnum}
     */
    private String skipReasonType;
    /**
     * 跳过原因
     */
    private String skipReason;
    /**
     * 操作人员编号，来自 system_users
     */
    private Long operatorId;
    /**
     * 审批人员编号，0 表示尚未审批，来自 system_users
     */
    private Long approvedBy;
    /**
     * 审批状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesSkipApproveStatusEnum}
     */
    private String approveStatus;
    /**
     * 实际跳过时间
     */
    private LocalDateTime skipTime;
    /**
     * 对后续工序、质检及排程的影响说明
     */
    private String effectRemark;
    /**
     * 状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesSkipRecordStatusEnum}
     */
    private String status;
    /**
     * 备注
     */
    private String remark;

}
