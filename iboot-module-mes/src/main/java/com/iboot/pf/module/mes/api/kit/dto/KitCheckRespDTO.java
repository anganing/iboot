package com.iboot.pf.module.mes.api.kit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 齐套检查结果。
 *
 * <p>整体结论与明细行一起返回：全部行齐套为 {@code FULL_KIT}，有齐有缺为 {@code PARTIAL_KIT}，
 * 全缺为 {@code SHORTAGE}。汇总规则留在服务端，不让调用方按行自己拼结论。
 */
@Schema(description = "RPC 服务 - 齐套检查结果 Response DTO")
@Data
public class KitCheckRespDTO {

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

    /** 整体齐套结论，取值为 MesKitStatusEnum 的枚举名。 */
    @Schema(description = "整体齐套结论", example = "PARTIAL_KIT")
    private String result;

    @Schema(description = "检查时间")
    private LocalDateTime checkTime;

    @Schema(description = "齐套快照明细行")
    private List<KitSnapshotRespDTO> lines;

}
