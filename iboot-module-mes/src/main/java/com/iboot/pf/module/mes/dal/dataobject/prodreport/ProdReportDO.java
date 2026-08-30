package com.iboot.pf.module.mes.dal.dataobject.prodreport;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-生产报工记录 DO
 *
 * @author iboot
 */
@TableName("mes_prod_report")
@KeySequence("mes_prod_report_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
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
     * 报工单号
     */
    private String reportNo;
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
     * 报工作业人员编号
     */
    private Long workerId;
    /**
     * 生产资源编号
     */
    private Long resId;
    /**
     * 班次编号
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
     * 状态：DRAFT、SUBMITTED、QC_PENDING、CONFIRMED、CANCELLED
     */
    private String status;
    /**
     * 报工幂等键
     */
    private String idempotencyKey;
    /**
     * 备注
     */
    private String remark;


}