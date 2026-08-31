package com.iboot.pf.module.wms.controller.admin.arrival.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsArrivalLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsIqcModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - WMS-到货登记明细新增/修改 Request VO")
@Data
public class ArrivalLineSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "到货登记编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "到货登记编号不能为空")
    private Long arrivalId;

    @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "行号不能为空")
    @Min(value = 1, message = "行号必须大于 0")
    private Integer lineNo;

    @Schema(description = "来源单据行号")
    @Size(max = 64, message = "来源单据行号长度不能超过 64")
    private String sourceLineNo;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    @Schema(description = "到货批次号")
    @Size(max = 128, message = "到货批次号长度不能超过 128")
    private String lotNo;

    @Schema(description = "供应商批次号")
    @Size(max = 128, message = "供应商批次号长度不能超过 128")
    private String supplierLotNo;

    @Schema(description = "到货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "到货数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "到货数量必须大于 0")
    private BigDecimal arrivalQty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计量单位编号不能为空")
    private Long uomId;

    @Schema(description = "是否需要质检", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否需要质检不能为空")
    private Boolean needQc;

    @Schema(description = "来料检验模式", example = "PRE_INBOUND")
    @InEnum(WmsIqcModeEnum.class)
    private String iqcMode;

    @Schema(description = "合格数量")
    @DecimalMin(value = "0", message = "合格数量不能为负数")
    private BigDecimal qualifiedQty;

    @Schema(description = "不合格数量")
    @DecimalMin(value = "0", message = "不合格数量不能为负数")
    private BigDecimal rejectedQty;

    @Schema(description = "明细状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    @NotEmpty(message = "明细状态不能为空")
    @InEnum(WmsArrivalLineStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
