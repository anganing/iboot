package com.iboot.pf.module.wms.controller.admin.inordermgr.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsOrderLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - WMS-入库单明细新增/修改 Request VO")
@Data
public class InOrderLineSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "入库单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "入库单编号不能为空")
    private Long orderId;

    @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "行号不能为空")
    @Min(value = 1, message = "行号必须大于 0")
    private Integer lineNo;

    @Schema(description = "来源明细行号")
    @Size(max = 64, message = "来源明细行号长度不能超过 64")
    private String sourceLineNo;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    @Schema(description = "批次编号，过账前可留空", example = "1")
    private Long batchId;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计量单位编号不能为空")
    private Long uomId;

    @Schema(description = "目标库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标库位编号不能为空")
    private Long locId;

    @Schema(description = "容器编号，散件留空", example = "1")
    private Long containerId;

    @Schema(description = "入库质量状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "QUALIFIED")
    @NotEmpty(message = "入库质量状态不能为空")
    @InEnum(WmsQualityStatusEnum.class)
    private String qualityStatus;

    @Schema(description = "计划入库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "计划入库数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "计划入库数量必须大于 0")
    private BigDecimal planQty;

    @Schema(description = "明细状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @NotEmpty(message = "明细状态不能为空")
    @InEnum(WmsOrderLineStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
