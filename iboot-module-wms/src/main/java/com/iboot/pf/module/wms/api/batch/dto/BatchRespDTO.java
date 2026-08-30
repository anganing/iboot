package com.iboot.pf.module.wms.api.batch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/** 批次响应 DTO。 */
@Data
public class BatchRespDTO {

    /** 批次编号。 */
    @Schema(description = "批次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    /** 物料编号。 */
    @Schema(description = "物料编号", example = "1")
    private Long matId;

    /** 物料编码快照。 */
    @Schema(description = "物料编码快照", example = "M001")
    private String matCode;

    /** 物料名称快照。 */
    @Schema(description = "物料名称快照", example = "钢板")
    private String matName;

    /** 批次号；无批次物料使用默认批次。 */
    @Schema(description = "批次号", example = "LOT20260801")
    private String lotNo;

    /** 序列号；SN 物料以数量为 1 的批次承载。 */
    @Schema(description = "序列号", example = "SN001")
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
    private LocalDate produceDate;

    /** 失效日期。 */
    @Schema(description = "失效日期")
    private LocalDate expiryDate;

    /** 状态：0-启用，1-停用。 */
    @Schema(description = "状态", example = "0")
    private Integer status;

}
