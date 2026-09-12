package com.iboot.pf.module.wms.api.outorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 出库单跨模块只读事实。
 *
 * <p>给跨模块调用方按单据编号批量反查出库单的可读字段用：{@code createOutOrder} 只返回单据 id，
 * 调用方落库的 {@code wms_biz_no} 要放真实的出库单号（与列注释"出入库单号"的语义对齐），
 * 就得靠这里把 id 换成 {@code outNo}，顺带取回状态、业务类型等展示与判断字段。
 */
@Schema(description = "RPC 服务 - 出库单 Response DTO")
@Data
public class OutOrderRespDTO {

    @Schema(description = "出库单编号", example = "1")
    private Long id;

    @Schema(description = "出库单号", example = "OUT202609090001")
    private String outNo;

    @Schema(description = "出库业务类型", example = "PROD_ISSUE_OUT")
    private String bizType;

    @Schema(description = "来源模块")
    private String sourceType;

    @Schema(description = "来源业务单号")
    private String sourceBizNo;

    /**
     * 客户编号，销售发货出库（{@code SALES_DELIVERY_OUT}）才有值。
     *
     * <p>追溯链路"成品批次 → 销售出库单 → 客户"的唯一权威来源就是这一列，跨模块取客户名称
     * 走 {@code PartnerApi.getCustomerMap} 批量补全，不在门面里跨库 JOIN mdm。
     */
    @Schema(description = "客户编号", example = "1")
    private Long customerId;

    /**
     * 生产工单号快照，领料出库（{@code PROD_ISSUE_OUT}）才有值。
     *
     * <p>调用方只持有出库单号（例如库存流水的 {@code biz_no}）时，靠这一列直接桥到 MES 工单，
     * 不必再绕领料申请。
     */
    @Schema(description = "生产工单号快照", example = "MO20260909001")
    private String prodOrderNo;

    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    /** 出库单状态，取值为 WmsOrderStatusEnum 的枚举名。 */
    @Schema(description = "出库单状态", example = "DONE")
    private String status;

    @Schema(description = "备注")
    private String remark;

}
