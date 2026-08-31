package com.iboot.pf.module.mes.dal.dataobject.prodreport;

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
 * MES-生产报工记录 DO
 *
 * @author iboot
 */
@TableName("mes_prod_report")
@KeySequence("mes_prod_report_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdReportDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 报工单号，由 infra 编码规则 MES_PROD_REPORT_NO 分配
     */
    private String reportNo;
    /**
     * 生产工单编号，由工序任务反查带出
     */
    private Long prodOrderId;
    /**
     * 作业工单编号，由工序任务反查带出
     */
    private Long workOrderId;
    /**
     * 工序任务编号
     */
    private Long opTaskId;
    /**
     * 报工作业人员编号，来自 system_users
     */
    private Long workerId;
    /**
     * 生产资源编号，0 表示未指定，来自 mdm_res
     */
    private Long resId;
    /**
     * 班次编号，0 表示不区分班次，来自 mdm_shift
     */
    private Long shiftId;
    /**
     * 合格数量
     */
    private BigDecimal qtyGood;
    /**
     * 报废数量
     */
    private BigDecimal qtyScrap;
    /**
     * 返工数量
     */
    private BigDecimal qtyRework;
    /**
     * 本次作业开始时间
     */
    private LocalDateTime startTime;
    /**
     * 本次作业结束时间
     */
    private LocalDateTime endTime;
    /**
     * 报工时间
     */
    private LocalDateTime reportTime;
    /**
     * 状态
     *
     * 枚举 {@link com.iboot.pf.module.mes.enums.MesProdReportStatusEnum}
     */
    private String status;
    /**
     * 报工幂等键，唯一索引防止扫码端重复提交
     */
    private String idempotencyKey;
    /**
     * 备注
     */
    private String remark;

}
