package com.iboot.pf.module.wms.controller.admin.countorder.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsCountLineStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - WMS-盘点明细新增/修改 Request VO")
@Data
public class CountOrderLineSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "盘点单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "盘点单编号不能为空")
    private Long orderId;

    @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "行号不能为空")
    @Min(value = 1, message = "行号必须大于 0")
    private Integer lineNo;

    @Schema(description = "库存余额编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "库存余额编号不能为空")
    private Long balanceId;

    @Schema(description = "实盘数量")
    @DecimalMin(value = "0", message = "实盘数量不能为负数")
    private BigDecimal actualQty;

    @Schema(description = "质量状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "QUALIFIED")
    @NotEmpty(message = "质量状态不能为空")
    @InEnum(WmsQualityStatusEnum.class)
    private String qualityStatus;

    @Schema(description = "明细状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    @NotEmpty(message = "明细状态不能为空")
    @InEnum(WmsCountLineStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
