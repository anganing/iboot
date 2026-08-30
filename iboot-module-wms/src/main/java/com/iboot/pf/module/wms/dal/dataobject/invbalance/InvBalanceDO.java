package com.iboot.pf.module.wms.dal.dataobject.invbalance;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * WMS-多维库存余额 DO
 *
 * @author iboot
 */
@TableName("wms_inv_balance")
@KeySequence("wms_inv_balance_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvBalanceDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
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
     * 批次编号；无批次物料挂默认批次
     */
    private Long batchId;
    /**
     * 容器编号，0表示散件
     */
    private Long containerId;
    /**
     * 项目编号，0表示无项目归属
     */
    private Long projectId;
    /**
     * 项目任务编号，0表示无项目任务归属
     */
    private Long projectTaskId;
    /**
     * 库存范围：COMMON、PROJECT_DEDICATED
     */
    private String stockScope;
    /**
     * 质量状态：QUALIFIED、QC_PENDING、REJECTED、FROZEN
     */
    private String qualityStatus;
    /**
     * 现存数量，基准单位
     */
    private BigDecimal qtyOnhand;
    /**
     * 预留数量，基准单位
     */
    private BigDecimal qtyReserved;
    /**
     * 冻结数量，基准单位
     */
    private BigDecimal qtyFrozen;
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
     * 基准单位编码快照
     */
    private String uomCode;
    /**
     * 批次号快照
     */
    private String lotNo;
    /**
     * 仓库编码快照
     */
    private String whCode;
    /**
     * 库位编码快照
     */
    private String locCode;
    /**
     * 乐观锁版本
     */
    private Integer version;
    /**
     * 备注
     */
    private String remark;


}