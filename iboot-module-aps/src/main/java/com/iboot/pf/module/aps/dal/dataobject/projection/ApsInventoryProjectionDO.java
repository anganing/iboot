package com.iboot.pf.module.aps.dal.dataobject.projection;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * APS-WMS 库存投影 DO。
 *
 * <p>场景冻结的一份库存余额副本（uk_aps_projection_inv_deleted：场景 + 来源余额）。
 * 缺料计算的可用量来源，只由“刷新投影”整体重建。
 */
@TableName("aps_projection_inv")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApsInventoryProjectionDO extends BaseDO {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 排程场景编号。 */
    private Long scenarioId;

    /** WMS 库存余额编号。 */
    private Long sourceBalanceId;

    /** 库存组织编号。 */
    private Long invOrgId;

    /** 工厂编号。 */
    private Long plantId;

    /** 仓库编号。 */
    private Long whId;

    /** 库位编号。 */
    private Long locId;

    /** 物料编号。 */
    private Long matId;

    /** 物料编码快照。 */
    private String matCode;

    /** 批次编号，0 表示无批次。 */
    private Long batchId;

    /** 批次号快照。 */
    private String lotNo;

    /** 项目编号，0 表示无项目。 */
    private Long projectId;

    /** 项目任务编号，0 表示无项目任务。 */
    private Long projectTaskId;

    /** 库存范围快照，取值见字典 wms_stock_scope。 */
    private String stockScope;

    /** 质量状态快照，取值见字典 wms_quality_status。 */
    private String qualityStatus;

    /** 现存数量快照。 */
    private BigDecimal qtyOnhand;

    /** 预留数量快照。 */
    private BigDecimal qtyReserved;

    /** 冻结数量快照。 */
    private BigDecimal qtyFrozen;

    /** 可用数量快照。 */
    private BigDecimal qtyAvailable;

    /** 来源数据更新时间。 */
    private LocalDateTime sourceUpdateTime;

    /** 投影快照时间。 */
    private LocalDateTime snapshotTime;

    /** 备注。 */
    private String remark;

}
