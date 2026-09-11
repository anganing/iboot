package com.iboot.pf.module.mes.dal.dataobject.disposition;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MES-不合格品处置单 DO
 *
 * @author iboot
 */
@TableName("mes_disposition")
@KeySequence("mes_disposition_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispositionDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 不合格处置单号，由 infra 编码规则 MES_DISPOSITION_NO 分配
     */
    private String dispositionNo;
    /**
     * 质检任务编号
     */
    private Long taskId;
    /**
     * 处置类型
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesDispositionTypeEnum}
     */
    private String dispositionType;
    /**
     * 处置数量，不能超过质检任务的不合格数量
     */
    private BigDecimal qty;
    /**
     * 联动目标业务类型
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesBizTypeEnum}
     */
    private String targetBizType;
    /**
     * 联动目标业务单号
     */
    private String targetBizNo;
    /**
     * 审批人员编号，0 表示尚未审批，来自 system_users
     */
    private Long approvedBy;
    /**
     * 审批时间
     */
    private LocalDateTime approveTime;
    /**
     * 状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesDispositionStatusEnum}
     */
    private String status;
    /**
     * 处置幂等键，唯一索引防止重复处置
     */
    private String idempotencyKey;
    /**
     * 处置说明
     */
    private String remark;

    /** 审批状态，取值见 BpmProcessInstanceStatusEnum。 */
    private Integer approvalStatus;
    /** 流程实例编号。 */
    private String processInstanceId;
    /** 审批完成时间。 */
    private LocalDateTime approvalTime;
    /** 审批意见或驳回原因。 */
    private String approvalReason;

}
