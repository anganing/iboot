package com.iboot.pf.module.mes.dal.dataobject.disposition;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-不合格品处置单 DO
 *
 * @author iboot
 */
@TableName("mes_disposition")
@KeySequence("mes_disposition_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
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
     * 不合格处置单号
     */
    private String dispositionNo;
    /**
     * 质检任务编号
     */
    private Long taskId;
    /**
     * 处置类型：REWORK、SCRAP、CONCESSION、FREEZE、RETURN_SUPPLIER、ADJUST_PROCESS
     */
    private String dispositionType;
    /**
     * 处置数量
     */
    private BigDecimal qty;
    /**
     * 联动目标业务类型
     */
    private String targetBizType;
    /**
     * 联动目标业务单号
     */
    private String targetBizNo;
    /**
     * 审批人员编号
     */
    private Long approvedBy;
    /**
     * 审批时间
     */
    private LocalDateTime approveTime;
    /**
     * 状态：DRAFT、SUBMITTED、APPROVED、EXECUTING、DONE、REJECTED、CANCELLED
     */
    private String status;
    /**
     * 处置幂等键
     */
    private String idempotencyKey;
    /**
     * 处置说明
     */
    private String remark;


}