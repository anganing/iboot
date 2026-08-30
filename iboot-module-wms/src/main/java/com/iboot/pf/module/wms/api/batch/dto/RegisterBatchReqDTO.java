package com.iboot.pf.module.wms.api.batch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 注册批次命令 DTO。 */
@Data
public class RegisterBatchReqDTO {

    /** 物料编号。 */
    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    /** 物料编码快照。 */
    @Schema(description = "物料编码快照", example = "M001")
    private String matCode;

    /** 物料名称快照。 */
    @Schema(description = "物料名称快照", example = "钢板")
    private String matName;

    /** 批次号；无批次物料传默认批次号。 */
    @Schema(description = "批次号", requiredMode = Schema.RequiredMode.REQUIRED, example = "LOT20260801")
    @NotBlank(message = "批次号不能为空")
    private String lotNo;

    /** 序列号，可为空。 */
    @Schema(description = "序列号")
    private String snNo;

    /** 供应商编号。 */
    @Schema(description = "供应商编号")
    private Long supplierId;

    /** 供应商编码快照。 */
    @Schema(description = "供应商编码快照")
    private String supplierCode;

    /** 供应商批次号。 */
    @Schema(description = "供应商批次号")
    private String supplierLotNo;

    /** 炉批号。 */
    @Schema(description = "炉批号")
    private String furnaceLotNo;

    /** 生产日期。 */
    @Schema(description = "生产日期")
    private java.time.LocalDate produceDate;

    /** 失效日期。 */
    @Schema(description = "失效日期")
    private java.time.LocalDate expiryDate;

}
