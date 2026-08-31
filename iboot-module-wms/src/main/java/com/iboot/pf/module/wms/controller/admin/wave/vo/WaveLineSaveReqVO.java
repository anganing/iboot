package com.iboot.pf.module.wms.controller.admin.wave.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsWaveStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - WMS-波次明细新增/修改 Request VO")
@Data
public class WaveLineSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "波次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "波次编号不能为空")
    private Long waveId;

    @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "行号不能为空")
    @Min(value = 1, message = "行号必须大于 0")
    private Integer lineNo;

    @Schema(description = "出库单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "出库单编号不能为空")
    private Long outOrderId;

    @Schema(description = "出库单明细编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "出库单明细编号不能为空")
    private Long outOrderLineId;

    @Schema(description = "波次内优先级，数值越大越优先", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "优先级不能为空")
    @Min(value = 0, message = "优先级不能为负数")
    private Integer priority;

    @Schema(description = "明细状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @NotEmpty(message = "明细状态不能为空")
    @InEnum(WmsWaveStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
