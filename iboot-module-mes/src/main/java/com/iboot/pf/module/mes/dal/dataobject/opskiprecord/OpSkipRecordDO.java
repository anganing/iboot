package com.iboot.pf.module.mes.dal.dataobject.opskiprecord;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-工序跳过记录 DO
 *
 * @author iboot
 */
@TableName("mes_op_skip_record")
@KeySequence("mes_op_skip_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
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
     */
    private String skipReasonType;
    /**
     * 跳过原因
     */
    private String skipReason;
    /**
     * 操作人员编号
     */
    private Long operatorId;
    /**
     * 审批人员编号
     */
    private Long approvedBy;
    /**
     * 审批状态：NOT_REQUIRED、PENDING、APPROVED、REJECTED
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
     * 状态：PENDING、EFFECTIVE、REVOKED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;


}