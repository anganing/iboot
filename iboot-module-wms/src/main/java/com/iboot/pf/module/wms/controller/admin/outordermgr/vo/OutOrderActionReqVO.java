package com.iboot.pf.module.wms.controller.admin.outordermgr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 出库单动作请求，提交与过账共用。
 *
 * <p>{@code idempotencyKey} 留空时由 Controller 按 {@code UI:{单据编号}:{命令类型}:0} 派生。
 * 这是确定性派生而不是随机键：同一张单据的同一个动作永远得到同一个键，界面连点两次、
 * 浏览器重发都会被 {@code wms_command_record} 的唯一索引判重。若服务端生成随机键，
 * 两次点击拿到两个不同的键，幂等就完全失效了。
 *
 * <p>过账动作对幂等键的依赖更强：{@code confirmOutOrder} 用它派生每行的库存流水键
 * {@code {幂等键}:POST:{行号}} 与预留核销键 {@code {幂等键}:{行号}:CONSUME}，
 * 键不稳定会让同一行被过账两次、余额被扣两遍。
 */
@Schema(description = "管理后台 - WMS-出库单动作 Request VO")
@Data
public class OutOrderActionReqVO {

    @Schema(description = "出库单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "出库单编号不能为空")
    private Long id;

    @Schema(description = "幂等键，留空由服务端按单据编号与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
