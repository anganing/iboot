package com.iboot.pf.module.wms.controller.admin.arrival.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 按到货明细生成采购入库单的请求。
 *
 * <p>{@code lineIds} 留空表示整单生成，取全部待入库（{@code READY}）明细，这是界面上最常用的操作；
 * 指定明细时逐个校验必须处于待入库状态——待质检的明细要等 IQC 结论，未确认到货的明细还没有批次。
 *
 * <p>这个动作**没有** {@code idempotencyKey} 字段，是有意为之：领域侧
 * {@code createInOrderFromArrival} 在内部按 {@code WMS:{到货单号}:PO_IN:{本次选中的行号串}} 派生幂等键，
 * 落在入库单表 {@code idempotency_key} 唯一索引上判重。这个键比 Controller 能派生的更精确——它带上了
 * 本次选中的行号，同一批明细重复提交命中原单，不同批明细各生成一张入库单。
 * 方法签名不接受外部键，若在这里放一个字段，前端传了会被静默丢弃，那才是真正的幂等失效。
 */
@Schema(description = "管理后台 - WMS-到货登记生成入库单 Request VO")
@Data
public class ArrivalInOrderCreateReqVO {

    @Schema(description = "到货登记编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "到货登记编号不能为空")
    private Long id;

    @Schema(description = "要生成入库单的到货明细编号，留空表示整单生成")
    private List<Long> lineIds;

}
