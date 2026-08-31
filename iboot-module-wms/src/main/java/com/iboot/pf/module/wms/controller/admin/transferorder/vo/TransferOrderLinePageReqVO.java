package com.iboot.pf.module.wms.controller.admin.transferorder.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - WMS-库存转移调拨明细分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TransferOrderLinePageReqVO extends PageParam {

    @Schema(description = "转移调拨单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "转移调拨单编号不能为空")
    private Long orderId;

    @Schema(description = "物料编号", example = "1")
    private Long matId;

    @Schema(description = "批次号")
    private String lotNo;

    @Schema(description = "明细状态")
    private String status;

}
