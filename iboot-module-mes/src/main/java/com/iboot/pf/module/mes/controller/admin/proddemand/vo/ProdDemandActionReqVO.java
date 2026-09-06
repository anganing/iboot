package com.iboot.pf.module.mes.controller.admin.proddemand.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 生产需求动作请求，确认与取消共用。
 *
 * <p>两个动作的入参本就相同（定位一条需求 + 一句可读说明），领域侧的
 * {@code MesConfirmDemandBO} 也是一个类同时服务两个动作，这里不再拆成两个 VO。
 * 动作语义由端点路径与命令类型区分，不靠参数类型区分。
 *
 * <p>{@code idempotencyKey} 允许为空，留空时由领域 Service 按
 * {@code UI:{需求编号}:{命令类型}:0} 确定性派生。<b>前端不要自己造键</b>：
 * 派生键对同一张单的同一个动作恒为同一个值，界面连点两次、浏览器重发都会被
 * {@code mes_command_record} 的唯一索引判重；前端填 uuid 或时间戳等于每次都是新键，幂等直接失效。
 */
@Schema(description = "管理后台 - MES-生产需求动作 Request VO")
@Data
public class ProdDemandActionReqVO {

    @Schema(description = "生产需求编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产需求编号不能为空")
    private Long id;

    @Schema(description = "幂等键，留空由服务端按需求编号与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "可读说明：确认时记确认说明，取消时记取消原因")
    @Size(max = 500, message = "说明长度不能超过 500")
    private String remark;

}
