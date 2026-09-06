package com.iboot.pf.module.mes.controller.admin.proddemand.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 生产需求转生产工单请求。
 *
 * <p>支持部分转单：{@code qty} 是本次转多少，可多次调用直到累计转满需求量，转满时需求才置已下达。
 * 转多少是计划决策，因此必填、不做"留空即转剩余全部"。
 *
 * <p>{@code idempotencyKey} 允许为空，留空时由领域 Service 按
 * {@code UI:{需求编号}:DEMAND_TO_ORDER:{已生成工单数}} 派生。<b>这一个动作的派生键只能保证不同批次之间
 * 不撞键，拦不住同一批次的重复点击</b>——服务端无法凭需求编号区分"第二次转单"与"第一次转单的重发"。
 * 界面上必须在提交后禁用按钮，重复点击会真的产生两张工单。
 */
@Schema(description = "管理后台 - MES-生产需求转生产工单 Request VO")
@Data
public class ProdDemandToOrderReqVO {

    @Schema(description = "生产需求编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产需求编号不能为空")
    private Long id;

    @Schema(description = "本次转单数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "本次转单数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "本次转单数量必须大于零")
    private BigDecimal qty;

    @Schema(description = "车间编号，留空落 0 表示待指派车间", example = "1")
    private Long workshopId;

    @Schema(description = "幂等键，留空由服务端按需求编号与已生成工单数派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "工单备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
