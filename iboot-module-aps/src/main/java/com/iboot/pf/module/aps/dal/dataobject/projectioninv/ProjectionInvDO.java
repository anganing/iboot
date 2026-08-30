package com.iboot.pf.module.aps.dal.dataobject.projectioninv;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 库存投影 DO
 *
 * @author iboot
 */
@TableName("aps_projection_inv")
@KeySequence("aps_projection_inv_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectionInvDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 排程场景编号
     */
    private Long scenarioId;
    /**
     * WMS库存余额编号
     */
    private Long sourceBalanceId;
    /**
     * 库存组织编号
     */
    private Long invOrgId;
    /**
     * 工厂编号
     */
    private Long plantId;
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
     * 物料编码快照
     */
    private String matCode;
    /**
     * 批次编号
     */
    private Long batchId;
    /**
     * 批次号快照
     */
    private String lotNo;
    /**
     * 项目编号，0表示无项目
     */
    private Long projectId;
    /**
     * 项目任务编号，0表示无项目任务
     */
    private Long projectTaskId;
    /**
     * 库存范围：COMMON、PROJECT_DEDICATED
     */
    private String stockScope;
    /**
     * 质量状态
     */
    private String qualityStatus;
    /**
     * 现存数量快照
     */
    private BigDecimal qtyOnhand;
    /**
     * 预留数量快照
     */
    private BigDecimal qtyReserved;
    /**
     * 冻结数量快照
     */
    private BigDecimal qtyFrozen;
    /**
     * 可用数量快照
     */
    private BigDecimal qtyAvailable;
    /**
     * 来源数据更新时间
     */
    private LocalDateTime sourceUpdateTime;
    /**
     * 投影快照时间
     */
    private LocalDateTime snapshotTime;
    /**
     * 备注
     */
    private String remark;


}