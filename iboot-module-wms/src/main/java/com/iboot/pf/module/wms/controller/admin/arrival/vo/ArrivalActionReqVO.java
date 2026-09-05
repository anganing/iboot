package com.iboot.pf.module.wms.controller.admin.arrival.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 到货登记动作请求，当前用于到货确认。
 *
 * <p>{@code idempotencyKey} 留空时由 Controller 按 {@code UI:{单据编号}:{命令类型}:0} 派生。
 * 这是确定性派生而不是随机键：同一张单据的同一个动作永远得到同一个键，界面连点两次、
 * 浏览器重发都会被 {@code wms_command_record} 的唯一索引判重。若服务端生成随机键，
 * 两次点击拿到两个不同的键，会重复注册批次、重复分流明细，幂等就完全失效了。
 */
@Schema(description = "管理后台 - WMS-到货登记动作 Request VO")
@Data
public class ArrivalActionReqVO {

    @Schema(description = "到货登记编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "到货登记编号不能为空")
    private Long id;

    @Schema(description = "幂等键，留空由服务端按单据编号与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "实际到货时间，留空取当前时间")
    private LocalDateTime arrivalTime;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
