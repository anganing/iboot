package com.iboot.pf.module.wms.api.inorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 入库单跨模块只读事实。
 *
 * <p>给跨模块调用方按单据编号批量反查入库单的可读字段用：{@code createInOrder} 只返回单据 id，
 * 调用方落库的 {@code wms_biz_no} 要放真实的入库单号（与列注释"出入库单号"的语义对齐），
 * 就得靠这里把 id 换成 {@code inNo}，顺带取回状态、业务类型等展示与判断字段。
 */
@Schema(description = "RPC 服务 - 入库单 Response DTO")
@Data
public class InOrderRespDTO {

    @Schema(description = "入库单编号", example = "1")
    private Long id;

    @Schema(description = "入库单号", example = "IN202609090001")
    private String inNo;

    @Schema(description = "入库业务类型", example = "PO_IN")
    private String bizType;

    @Schema(description = "来源模块")
    private String sourceType;

    @Schema(description = "来源业务单号")
    private String sourceBizNo;

    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    /** 入库单状态，取值为 WmsOrderStatusEnum 的枚举名。 */
    @Schema(description = "入库单状态", example = "DONE")
    private String status;

    @Schema(description = "备注")
    private String remark;

}
