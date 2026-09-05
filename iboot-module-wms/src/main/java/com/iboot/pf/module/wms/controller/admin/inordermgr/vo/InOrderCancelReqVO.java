package com.iboot.pf.module.wms.controller.admin.inordermgr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 入库单取消请求。
 *
 * <p>与 {@link InOrderActionReqVO} 分开是因为取消要落的是原因而不是备注，
 * 领域侧 {@code WmsCancelOrderBO} 用的字段名也是 {@code reason}。
 */
@Schema(description = "管理后台 - WMS-入库单取消 Request VO")
@Data
public class InOrderCancelReqVO {

    @Schema(description = "入库单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "入库单编号不能为空")
    private Long id;

    @Schema(description = "幂等键，留空由服务端按单据编号与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "取消原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "取消原因不能为空")
    @Size(max = 500, message = "取消原因长度不能超过 500")
    private String reason;

}
