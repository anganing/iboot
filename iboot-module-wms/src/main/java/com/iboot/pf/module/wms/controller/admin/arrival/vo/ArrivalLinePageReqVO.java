package com.iboot.pf.module.wms.controller.admin.arrival.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - WMS-到货登记明细分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ArrivalLinePageReqVO extends PageParam {

    @Schema(description = "到货登记编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "到货登记编号不能为空")
    private Long arrivalId;

    @Schema(description = "物料编号", example = "1")
    private Long matId;

    @Schema(description = "批次号")
    private String lotNo;

    @Schema(description = "明细状态")
    private String status;

}
