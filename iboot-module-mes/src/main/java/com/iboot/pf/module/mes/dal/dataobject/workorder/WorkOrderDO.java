package com.iboot.pf.module.mes.dal.dataobject.workorder;

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
 * MES-作业工单 DO
 *
 * @author iboot
 */
@TableName("mes_work_order")
@KeySequence("mes_work_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 作业工单号，由 infra 编码规则 MES_WORK_ORDER_NO 分配
     */
    private String workOrderNo;
    /**
     * 生产工单编号
     */
    private Long prodOrderId;
    /**
     * 生产工单号快照
     */
    private String prodOrderNo;
    /**
     * 工厂编号，来自 mdm_plant
     */
    private Long plantId;
    /**
     * 车间编号，来自 mdm_workshop
     */
    private Long workshopId;
    /**
     * 工作中心编号，来自 mdm_wc
     */
    private Long wcId;
    /**
     * 项目编号，0 表示无项目
     */
    private Long projectId;
    /**
     * 项目任务编号，0 表示无项目任务
     */
    private Long projectTaskId;
    /**
     * 计划数量
     */
    private BigDecimal plannedQty;
    /**
     * 已完成数量，由工序任务完工推进
     */
    private BigDecimal completedQty;
    /**
     * 计划开始时间
     */
    private LocalDateTime plannedStartTime;
    /**
     * 计划结束时间
     */
    private LocalDateTime plannedEndTime;
    /**
     * 实际开始时间，由首道工序开工写入
     */
    private LocalDateTime actualStartTime;
    /**
     * 实际结束时间，由末道工序完工写入
     */
    private LocalDateTime actualEndTime;
    /**
     * 齐套状态，由齐套检查写入
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesKitStatusEnum}
     */
    private String kitStatus;
    /**
     * 状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum}
     */
    private String status;
    /**
     * 乐观锁版本
     */
    private Integer version;
    /**
     * 备注
     */
    private String remark;

}
