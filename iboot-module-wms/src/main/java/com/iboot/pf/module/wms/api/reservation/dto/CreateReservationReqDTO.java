package com.iboot.pf.module.wms.api.reservation.dto;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 预留库存命令 DTO。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateReservationReqDTO extends WmsCommandReqDTO {

    /** 来源业务类型，写入预留与流水。 */
    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "PROD_ISSUE")
    @NotNull(message = "来源业务类型不能为空")
    private String bizType;

    /** 库存组织编号。 */
    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    /** 仓库编号。 */
    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "仓库编号不能为空")
    private Long whId;

    /** 库位编号，为空表示不限库位。 */
    @Schema(description = "库位编号")
    private Long locId;

    /** 物料编号。 */
    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    /** 批次编号。 */
    @Schema(description = "批次编号")
    private Long batchId;

    /** 容器编号。 */
    @Schema(description = "容器编号")
    private Long containerId;

    /** 项目编号。 */
    @Schema(description = "项目编号")
    private Long projectId;

    /** 项目任务编号。 */
    @Schema(description = "项目任务编号")
    private Long projectTaskId;

    /** 质量状态，为空按合格处理：QUALIFIED、QC_PENDING、REJECTED、FROZEN。 */
    @Schema(description = "质量状态", example = "QUALIFIED")
    private String qualityStatus;

    /** 预留数量，基准单位。 */
    @Schema(description = "预留数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.000000")
    @NotNull(message = "预留数量不能为空")
    private BigDecimal qty;

    /** 预留失效时间，为空表示不失效。 */
    @Schema(description = "预留失效时间")
    private LocalDateTime expireTime;

    /** 备注。 */
    @Schema(description = "备注")
    private String remark;

}
