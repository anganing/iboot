package com.iboot.pf.module.wms.api.container.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 容器类型响应 DTO。 */
@Data
public class ContainerTypeRespDTO {

    /** 容器类型编号。 */
    @Schema(description = "容器类型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    /** 容器类型编码。 */
    @Schema(description = "容器类型编码", example = "TRAY")
    private String code;

    /** 容器类型名称。 */
    @Schema(description = "容器类型名称", example = "托盘")
    private String name;

    /** 标准长度，单位毫米。 */
    @Schema(description = "标准长度，单位毫米")
    private java.math.BigDecimal stdLength;

    /** 标准宽度，单位毫米。 */
    @Schema(description = "标准宽度，单位毫米")
    private java.math.BigDecimal stdWidth;

    /** 标准高度，单位毫米。 */
    @Schema(description = "标准高度，单位毫米")
    private java.math.BigDecimal stdHeight;

    /** 最大承重。 */
    @Schema(description = "最大承重")
    private java.math.BigDecimal maxWeight;

    /** 最大容积。 */
    @Schema(description = "最大容积")
    private java.math.BigDecimal maxVolume;

    /** 状态：0-启用，1-停用。 */
    @Schema(description = "状态", example = "0")
    private Integer status;

}
