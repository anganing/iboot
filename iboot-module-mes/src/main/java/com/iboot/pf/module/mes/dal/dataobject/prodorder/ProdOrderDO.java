package com.iboot.pf.module.mes.dal.dataobject.prodorder;

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
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * MES-生产工单 DO
 *
 * @author iboot
 */
@TableName("mes_prod_order")
@KeySequence("mes_prod_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProdOrderDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 生产工单号
     */
    private String orderNo;
    /**
     * 生产需求编号
     */
    private Long demandId;
    /**
     * 工厂编号
     */
    private Long plantId;
    /**
     * 车间编号
     */
    private Long workshopId;
    /**
     * 项目编号，0表示无项目
     */
    private Long projectId;
    /**
     * 项目任务编号，0表示无项目任务
     */
    private Long projectTaskId;
    /**
     * 生产物料编号
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
     * 计划生产数量
     */
    private BigDecimal qty;
    /**
     * 已完成数量
     */
    private BigDecimal completedQty;
    /**
     * 累计报废数量
     */
    private BigDecimal scrapQty;
    /**
     * 计量单位编号
     */
    private Long uomId;
    /**
     * 计量单位编码快照
     */
    private String uomCode;
    /**
     * 锁定的BOM编号
     */
    private Long bomId;
    /**
     * 锁定的BOM版本
     */
    private String bomVersion;
    /**
     * 锁定的工艺路线编号
     */
    private Long routeId;
    /**
     * 锁定的工艺路线版本
     */
    private String routeVersion;
    /**
     * 工单交期
     */
    private LocalDateTime dueDate;
    /**
     * 优先级
     */
    private Integer priority;
    /**
     * 齐套状态：UNCHECKED、FULL_KIT、PARTIAL_KIT、SHORTAGE
     */
    private String kitStatus;
    /**
     * 状态：DRAFT、RELEASED、READY、RUNNING、HOLD、DONE、CLOSED、CANCELLED
     */
    private String status;
    /**
     * 下达时间
     */
    private LocalDateTime releaseTime;
    /**
     * 实际开始时间
     */
    private LocalDateTime actualStartTime;
    /**
     * 实际结束时间
     */
    private LocalDateTime actualEndTime;
    /**
     * 乐观锁版本
     */
    private Integer version;
    /**
     * 备注
     */
    private String remark;


}