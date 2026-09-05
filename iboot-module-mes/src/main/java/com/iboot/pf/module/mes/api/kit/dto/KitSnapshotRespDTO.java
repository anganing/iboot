package com.iboot.pf.module.mes.api.kit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 齐套快照一行，对应 {@code mes_kit_snapshot} 的一条需求行。
 *
 * <p>快照只记录检查那一刻的库存口径，不占用库存：预留是领料提交阶段的事。
 */
@Schema(description = "RPC 服务 - 齐套快照 Response DTO")
@Data
public class KitSnapshotRespDTO {

    @Schema(description = "齐套快照编号", example = "1")
    private Long id;

    @Schema(description = "齐套检查编号", example = "KC202609040001")
    private String checkNo;

    /** 检查范围，取值为 MesKitCheckScopeEnum 的枚举名。 */
    @Schema(description = "检查范围", example = "PROD_ORDER")
    private String checkScope;

    @Schema(description = "生产工单编号", example = "1")
    private Long prodOrderId;

    @Schema(description = "作业工单编号，0 表示整单检查", example = "1")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0 表示非工序检查", example = "1")
    private Long opTaskId;

    @Schema(description = "检查明细行号", example = "1")
    private Integer lineNo;

    @Schema(description = "需求物料编号", example = "1")
    private Long matId;

    @Schema(description = "物料编码快照", example = "M-0001")
    private String matCode;

    @Schema(description = "物料名称快照")
    private String matName;

    @Schema(description = "需求数量", example = "210.526316")
    private BigDecimal reqQty;

    @Schema(description = "检查时可用数量", example = "300")
    private BigDecimal availQty;

    @Schema(description = "检查时本需求已预留数量", example = "0")
    private BigDecimal reservedQty;

    @Schema(description = "检查时已领用数量", example = "0")
    private BigDecimal issuedQty;

    @Schema(description = "短缺数量", example = "0")
    private BigDecimal shortQty;

    @Schema(description = "实际采用替代料编号，0 表示未采用", example = "0")
    private Long altMatId;

    /** 行检查结果，取值为 MesKitStatusEnum 的枚举名。 */
    @Schema(description = "行检查结果", example = "FULL_KIT")
    private String result;

    @Schema(description = "检查时间")
    private LocalDateTime checkTime;

}
