package com.iboot.pf.module.mes.controller.admin.prodorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 齐套检查结果：整体结论 + 逐物料的明细行。
 *
 * <p><b>明细行不能丢</b>。整体结论只回答"齐没齐"，而车间要的是"缺什么、缺多少、能不能用替代料"——
 * 缺料时只回一个 {@code SHORTAGE} 等于让人再去翻齐套快照列表，齐套检查这个动作就白做了。
 *
 * <p>结论怎么汇总由服务端定（全齐 {@code FULL_KIT}、有齐有缺 {@code PARTIAL_KIT}、全缺 {@code SHORTAGE}），
 * 界面不要按行自己拼：两个调用方拼法不同，同一份快照就会有两种说法。
 *
 * <p>行上四个数量的口径（齐套判准是 {@code 可用量 + 已预留 + 已领用 >= 需求量}，不是只看可用量）：
 * 本工单提交领料后自己占的预留会把可用量压下去，只看可用量的话同一张工单在领料后再查齐套会从"齐套"
 * 倒退成"缺料"。这也正是快照要单独存 {@code reservedQty} 与 {@code issuedQty} 两列的原因。
 */
@Schema(description = "管理后台 - MES-生产工单齐套检查 Response VO")
@Data
public class ProdOrderKitCheckRespVO {

    @Schema(description = "齐套检查编号，一次检查一个", example = "KC-0001")
    private String checkNo;

    @Schema(description = "检查范围", example = "PROD_ORDER")
    private String checkScope;

    @Schema(description = "生产工单编号", example = "1")
    private Long prodOrderId;

    @Schema(description = "作业工单编号，0 表示整单检查", example = "0")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0 表示非工序检查", example = "0")
    private Long opTaskId;

    @Schema(description = "整体齐套结论：FULL_KIT 全齐、PARTIAL_KIT 部分齐、SHORTAGE 全缺", example = "FULL_KIT")
    private String result;

    @Schema(description = "检查时间")
    private LocalDateTime checkTime;

    @Schema(description = "逐物料的齐套明细，按行号升序；无需求行时为空表")
    private List<Line> lines;

    /** 一个物料的齐套结论。 */
    @Schema(description = "齐套检查明细")
    @Data
    public static class Line {

        @Schema(description = "行号", example = "1")
        private Integer lineNo;

        @Schema(description = "物料编号", example = "1")
        private Long matId;

        @Schema(description = "物料编码", example = "MAT-1001")
        private String matCode;

        @Schema(description = "物料名称")
        private String matName;

        @Schema(description = "需求量，按工单数量与损耗率算出", example = "210.526316")
        private BigDecimal reqQty;

        @Schema(description = "可用量：在库 − 预留 − 冻结", example = "300")
        private BigDecimal availQty;

        @Schema(description = "本工单已预留量", example = "0")
        private BigDecimal reservedQty;

        @Schema(description = "本工单已领用量", example = "0")
        private BigDecimal issuedQty;

        @Schema(description = "缺口量，齐套时为 0", example = "0")
        private BigDecimal shortQty;

        @Schema(description = "命中的替代料编号，0 表示用主料", example = "0")
        private Long altMatId;

        @Schema(description = "本行结论：FULL_KIT 或 SHORTAGE", example = "FULL_KIT")
        private String result;

        @Schema(description = "备注")
        private String remark;

    }

}
