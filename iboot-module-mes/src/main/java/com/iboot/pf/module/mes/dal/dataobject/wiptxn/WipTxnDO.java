package com.iboot.pf.module.mes.dal.dataobject.wiptxn;

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
 * MES-在制流转流水 DO
 *
 * <p>账表：只由业务动作（开工、报工、流转、跳序、完工）写入留痕，不接受人工新增修改删除。
 *
 * @author iboot
 */
@TableName("mes_wip_txn")
@KeySequence("mes_wip_txn_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WipTxnDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 在制事务编号，由 infra 编码规则 MES_WIP_TXN_NO 分配
     */
    private String txnNo;
    /**
     * 生产工单编号
     */
    private Long prodOrderId;
    /**
     * 作业工单编号
     */
    private Long workOrderId;
    /**
     * 工序任务编号
     */
    private Long opTaskId;
    /**
     * 事务类型
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum}
     */
    private String txnType;
    /**
     * 事务数量
     */
    private BigDecimal qty;
    /**
     * 变更前状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum}
     */
    private String fromStatus;
    /**
     * 变更后状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum}
     */
    private String toStatus;
    /**
     * 来源业务类型
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesBizTypeEnum}
     */
    private String bizType;
    /**
     * 来源业务单号
     */
    private String bizNo;
    /**
     * 来源工序任务编号，0 表示无
     */
    private Long fromOpTaskId;
    /**
     * 目标工序任务编号，0 表示无
     */
    private Long toOpTaskId;
    /**
     * 事务发生时间
     */
    private LocalDateTime txnTime;
    /**
     * 事务幂等键，唯一索引防止同一动作重复留痕
     */
    private String idempotencyKey;
    /**
     * 备注
     */
    private String remark;

}
