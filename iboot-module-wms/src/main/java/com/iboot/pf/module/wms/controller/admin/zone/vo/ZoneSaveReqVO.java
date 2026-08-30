package com.iboot.pf.module.wms.controller.admin.zone.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - WMS-库区新增/修改 Request VO")
@Data
public class ZoneSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "14009")
    private Long id;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32765")
    @NotNull(message = "仓库编号不能为空")
    private Long whId;

    @Schema(description = "库区编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "库区编码不能为空")
    private String code;

    @Schema(description = "库区名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "库区名称不能为空")
    private String name;

    @Schema(description = "库区类型：RAW、WIP、FG、QC、RETURN、SCRAP、RECEIVING、SHIPPING", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "库区类型：RAW、WIP、FG、QC、RETURN、SCRAP、RECEIVING、SHIPPING不能为空")
    private String zoneType;

    @Schema(description = "是否允许混放物料", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否允许混放物料不能为空")
    private Boolean allowMixedMat;

    @Schema(description = "是否允许混放批次", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否允许混放批次不能为空")
    private Boolean allowMixedLot;

    @Schema(description = "温区要求")
    private String tempRange;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态：0-启用，1-停用不能为空")
    private Integer status;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}