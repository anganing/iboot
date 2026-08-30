package com.iboot.pf.module.wms.api.warehouse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 仓库响应 DTO。 */
@Data
public class WhRespDTO {

    /** 仓库编号。 */
    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    /** 库存组织编号。 */
    @Schema(description = "库存组织编号", example = "1")
    private Long invOrgId;

    /** 工厂编号。 */
    @Schema(description = "工厂编号", example = "1")
    private Long plantId;

    /** 仓库编码。 */
    @Schema(description = "仓库编码", example = "WH01")
    private String code;

    /** 仓库名称。 */
    @Schema(description = "仓库名称", example = "原材料立库")
    private String name;

    /** 仓库类型：ASRS-立库，FLAT-平库，LINE_SIDE-线边仓，VIRTUAL-虚拟仓，QC-质检仓，IN_TRANSIT-在途仓。 */
    @Schema(description = "仓库类型", example = "ASRS")
    private String whType;

    /** 管理方式：MANUAL、WCS、AGV、MIXED。 */
    @Schema(description = "管理方式", example = "WCS")
    private String managedBy;

    /** 外部设备系统编码。 */
    @Schema(description = "外部设备系统编码")
    private String externalSystemCode;

    /** 状态：0-启用，1-停用。 */
    @Schema(description = "状态", example = "0")
    private Integer status;

}
