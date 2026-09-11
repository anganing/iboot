package com.iboot.pf.module.wms.api.inventory.dto;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 库存质量状态迁移命令。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QualityChangeReqDTO extends WmsCommandReqDTO {

    private Long invOrgId;
    private Long plantId;
    private Long whId;
    private Long locId;
    private Long matId;
    private Long batchId;
    private Long containerId;
    private Long projectId;
    private Long projectTaskId;
    private String stockScope;
    private String matCode;
    private String matName;
    private String uomCode;
    private String lotNo;
    private String locCode;
    private String qualityStatusFrom;
    private String qualityStatusTo;
    private BigDecimal qty;
    private String remark;
}
