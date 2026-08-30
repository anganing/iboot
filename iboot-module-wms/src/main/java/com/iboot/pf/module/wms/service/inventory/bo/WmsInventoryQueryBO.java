package com.iboot.pf.module.wms.service.inventory.bo;

import lombok.Data;

import java.util.Collection;

/** 库存余额查询参数。为空的条件不过滤。 */
@Data
public class WmsInventoryQueryBO {

    /** 库存组织编号。 */
    private Long invOrgId;

    /** 工厂编号。 */
    private Long plantId;

    /** 仓库编号。 */
    private Long whId;

    /** 库位编号。 */
    private Long locId;

    /** 库位编号集合，按库区圈选库位时使用。 */
    private Collection<Long> locIds;

    /** 物料编号。 */
    private Long matId;

    /** 物料编号集合，批量查询时使用。 */
    private Collection<Long> matIds;

    /** 批次编号。 */
    private Long batchId;

    /** 容器编号。 */
    private Long containerId;

    /** 项目编号。 */
    private Long projectId;

    /** 项目任务编号。 */
    private Long projectTaskId;

    /** 库存范围：COMMON、PROJECT_DEDICATED。 */
    private String stockScope;

    /** 质量状态：QUALIFIED、QC_PENDING、REJECTED、FROZEN。 */
    private String qualityStatus;

    /** 是否只统计合格库存，默认 true。 */
    private Boolean qualifiedOnly = true;

}
