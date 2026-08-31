package com.iboot.pf.module.wms.controller.admin.transferorder.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsOrderLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsStockScopeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - WMS-库存转移调拨明细新增/修改 Request VO")
@Data
public class TransferOrderLineSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "转移调拨单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "转移调拨单编号不能为空")
    private Long orderId;

    @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "行号不能为空")
    @Min(value = 1, message = "行号必须大于 0")
    private Integer lineNo;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    @Schema(description = "容器编号，散件留空", example = "1")
    private Long containerId;

    @Schema(description = "来源库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源库位编号不能为空")
    private Long fromLocId;

    @Schema(description = "目标库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标库位编号不能为空")
    private Long toLocId;

    @Schema(description = "来源库存范围", example = "COMMON")
    @InEnum(WmsStockScopeEnum.class)
    private String fromStockScope;

    @Schema(description = "目标库存范围", example = "COMMON")
    @InEnum(WmsStockScopeEnum.class)
    private String toStockScope;

    @Schema(description = "来源项目编号，公共库存留空", example = "1")
    private Long fromProjectId;

    @Schema(description = "目标项目编号，公共库存留空", example = "1")
    private Long toProjectId;

    @Schema(description = "来源项目任务编号", example = "1")
    private Long fromProjectTaskId;

    @Schema(description = "目标项目任务编号", example = "1")
    private Long toProjectTaskId;

    @Schema(description = "来源质量状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "QUALIFIED")
    @NotEmpty(message = "来源质量状态不能为空")
    @InEnum(WmsQualityStatusEnum.class)
    private String qualityStatusFrom;

    @Schema(description = "目标质量状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "QUALIFIED")
    @NotEmpty(message = "目标质量状态不能为空")
    @InEnum(WmsQualityStatusEnum.class)
    private String qualityStatusTo;

    @Schema(description = "基准计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "基准计量单位编号不能为空")
    private Long uomId;

    @Schema(description = "计划转移数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "计划转移数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "计划转移数量必须大于 0")
    private BigDecimal planQty;

    @Schema(description = "明细状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @NotEmpty(message = "明细状态不能为空")
    @InEnum(WmsOrderLineStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
