package com.iboot.pf.module.wms.controller.admin.wave.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - WMS-波次明细分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class WaveLinePageReqVO extends PageParam {

    @Schema(description = "波次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "波次编号不能为空")
    private Long waveId;

    @Schema(description = "出库单编号", example = "1")
    private Long outOrderId;

    @Schema(description = "明细状态")
    private String status;

}
