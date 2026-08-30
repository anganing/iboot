package com.iboot.pf.module.wms.api.container.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 容器响应 DTO。 */
@Data
public class ContainerRespDTO {

    /** 容器编号。 */
    @Schema(description = "容器编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    /** 容器编号或条码。 */
    @Schema(description = "容器编号或条码", example = "CTN001")
    private String containerNo;

    /** 容器类型编号。 */
    @Schema(description = "容器类型编号")
    private Long typeId;

    /** 库存组织编号。 */
    @Schema(description = "库存组织编号")
    private Long invOrgId;

    /** 当前仓库编号。 */
    @Schema(description = "当前仓库编号")
    private Long whId;

    /** 当前库位编号，0 表示未落位。 */
    @Schema(description = "当前库位编号")
    private Long locId;

    /** 状态：EMPTY、LOADED、IN_TRANSIT、STORED、CLOSED。 */
    @Schema(description = "状态", example = "STORED")
    private String status;

    /** 乐观锁版本。 */
    @Schema(description = "乐观锁版本")
    private Integer version;

}
