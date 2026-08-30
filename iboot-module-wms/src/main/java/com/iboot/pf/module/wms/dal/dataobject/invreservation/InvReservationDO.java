package com.iboot.pf.module.wms.dal.dataobject.invreservation;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-库存预留 DO
 *
 * @author iboot
 */
@TableName("wms_inv_reservation")
@KeySequence("wms_inv_reservation_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvReservationDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 预留编号
     */
    private String reservationNo;
    /**
     * 来源业务类型
     */
    private String bizType;
    /**
     * 来源业务单号
     */
    private String bizNo;
    /**
     * 来源业务行号
     */
    private String bizLineNo;
    /**
     * 库存组织编号
     */
    private Long invOrgId;
    /**
     * 仓库编号
     */
    private Long whId;
    /**
     * 库位编号
     */
    private Long locId;
    /**
     * 物料编号
     */
    private Long matId;
    /**
     * 批次编号
     */
    private Long batchId;
    /**
     * 容器编号，0表示散件
     */
    private Long containerId;
    /**
     * 项目编号，0表示无项目
     */
    private Long projectId;
    /**
     * 项目任务编号，0表示无项目任务
     */
    private Long projectTaskId;
    /**
     * 质量状态
     */
    private String qualityStatus;
    /**
     * 预留数量，基准单位
     */
    private BigDecimal qty;
    /**
     * 已释放或已核销数量
     */
    private BigDecimal releasedQty;
    /**
     * 状态：ACTIVE、PARTIAL、RELEASED、CONSUMED、CANCELLED
     */
    private String status;
    /**
     * 预留失效时间
     */
    private LocalDateTime expireTime;
    /**
     * 预留幂等键
     */
    private String idempotencyKey;
    /**
     * 备注
     */
    private String remark;


}