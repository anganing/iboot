package com.iboot.pf.module.wms.service.lock.bo;

import lombok.Data;

/** 冻结记录查询参数。为空的条件不过滤。 */
@Data
public class WmsLockQueryBO {

    /** 库存组织编号。 */
    private Long invOrgId;

    /** 仓库编号。 */
    private Long whId;

    /** 库位编号。 */
    private Long locId;

    /** 物料编号。 */
    private Long matId;

    /** 批次编号。 */
    private Long batchId;

    /** 容器编号。 */
    private Long containerId;

    /** 状态：ACTIVE、PARTIAL、RELEASED、CANCELLED。 */
    private String status;

}
