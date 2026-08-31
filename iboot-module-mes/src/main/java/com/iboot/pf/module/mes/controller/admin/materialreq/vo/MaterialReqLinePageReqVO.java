package com.iboot.pf.module.mes.controller.admin.materialreq.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 领退补料申请明细分页 Request VO。
 *
 * <p>明细必须限定在某一张申请单下，父键用 {@code @NotNull} 约束，避免全表扫明细。
 */
@Schema(description = "管理后台 - MES-领退补料申请明细分页 Request VO")
@Data
public class MaterialReqLinePageReqVO extends PageParam {

    @Schema(description = "领退补料申请编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "领退补料申请编号不能为空")
    private Long reqId;

    @Schema(description = "物料编号", example = "1")
    private Long matId;

    @Schema(description = "批次号")
    private String lotNo;

    @Schema(description = "明细状态", example = "DRAFT")
    private String status;

}
