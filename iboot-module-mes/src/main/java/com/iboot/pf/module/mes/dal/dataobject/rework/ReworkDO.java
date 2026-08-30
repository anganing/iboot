package com.iboot.pf.module.mes.dal.dataobject.rework;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-返工任务 DO
 *
 * @author iboot
 */
@TableName("mes_rework")
@KeySequence("mes_rework_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReworkDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 返工任务编号
     */
    private String reworkNo;
    /**
     * 来源质检任务编号
     */
    private Long sourceQcTaskId;
    /**
     * 生产工单编号
     */
    private Long prodOrderId;
    /**
     * 作业工单编号
     */
    private Long workOrderId;
    /**
     * 返工起始工序任务编号
     */
    private Long opTaskId;
    /**
     * 返工数量
     */
    private BigDecimal qty;
    /**
     * 返工工艺路线编号
     */
    private Long routeId;
    /**
     * 返工工艺路线版本
     */
    private String routeVersion;
    /**
     * 状态：DRAFT、RELEASED、RUNNING、QC_PENDING、DONE、CANCELLED
     */
    private String status;
    /**
     * 备注
     */
    private String remark;


}