package com.iboot.pf.module.wms.api.lock.dto;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 冻结库存命令 DTO。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateLockReqDTO extends WmsCommandReqDTO {

    /** 来源业务类型，写入冻结记录与流水。 */
    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "QC_HOLD")
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

    /** 库位编号。 */
    @Schema(description = "库位编号", example = "1")
    private Long locId;

    /** 物料编号。 */
    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    /** 批次编号。 */
    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    /** 容器编号。 */
    @Schema(description = "容器编号", example = "0")
    private Long containerId;

    /** 项目编号。 */
    @Schema(description = "项目编号", example = "0")
    private Long projectId;

    /** 项目任务编号。 */
    @Schema(description = "项目任务编号", example = "0")
    private Long projectTaskId;

    /** 质量状态，为空按合格处理：QUALIFIED、QC_PENDING、REJECTED、FROZEN。 */
    @Schema(description = "质量状态", example = "QUALIFIED")
    private String qualityStatus;

    /** 冻结数量，基准单位。 */
    @Schema(description = "冻结数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.000000")
    @NotNull(message = "冻结数量不能为空")
    private BigDecimal qty;

    /** 冻结原因。 */
    @Schema(description = "冻结原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "质检发现异常")
    @NotBlank(message = "冻结原因不能为空")
    private String reason;

    /** 备注。 */
    @Schema(description = "备注")
    private String remark;

}
