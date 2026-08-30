package com.iboot.pf.module.mes.dal.dataobject.proddemand;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-生产需求 DO
 *
 * @author iboot
 */
@TableName("mes_prod_demand")
@KeySequence("mes_prod_demand_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdDemandDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 生产需求编号
     */
    private String demandNo;
    /**
     * 来源类型：MANUAL、EXCEL、ERP、APS
     */
    private String sourceType;
    /**
     * 来源单号
     */
    private String sourceNo;
    /**
     * 工厂编号
     */
    private Long plantId;
    /**
     * 项目编号，0表示无项目
     */
    private Long projectId;
    /**
     * 项目任务编号，0表示无项目任务
     */
    private Long projectTaskId;
    /**
     * 需求物料编号
     */
    private Long matId;
    /**
     * 物料编码快照
     */
    private String matCode;
    /**
     * 物料名称快照
     */
    private String matName;
    /**
     * 物料规格快照
     */
    private String matSpec;
    /**
     * 需求数量
     */
    private BigDecimal qty;
    /**
     * 计量单位编号
     */
    private Long uomId;
    /**
     * 计量单位编码快照
     */
    private String uomCode;
    /**
     * 需求交期
     */
    private LocalDateTime dueDate;
    /**
     * 优先级，数值越大越优先
     */
    private Integer priority;
    /**
     * 状态：DRAFT、CONFIRMED、PLANNED、RELEASED、DONE、CANCELLED
     */
    private String status;
    /**
     * 创建幂等键
     */
    private String idempotencyKey;
    /**
     * 备注
     */
    private String remark;


}