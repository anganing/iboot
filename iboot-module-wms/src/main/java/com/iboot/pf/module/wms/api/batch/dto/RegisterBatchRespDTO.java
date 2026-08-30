package com.iboot.pf.module.wms.api.batch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 批次注册结果 DTO。 */
@Data
public class RegisterBatchRespDTO {

    /** 批次编号。 */
    @Schema(description = "批次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    /** 批次号。 */
    @Schema(description = "批次号", example = "LOT20260801")
    private String lotNo;

    /** 序列号，可为空。 */
    @Schema(description = "序列号")
    private String snNo;

}
