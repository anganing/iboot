package com.iboot.pf.module.aps.controller.admin.solversnapshot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 约束命中 Response VO：把 {@code CONSTRAINT_MATCH} 快照翻成人能读的对照表。
 *
 * <p>不让前端直接解析 {@code snapshot_json}：那份 JSON 里的权重与得分是 Timefold 的
 * {@code HardSoftLongScore} 字符串（形如 {@code 0hard/-1soft}），把这个格式的知识散到前端，
 * 将来换分数类型就要改两处。这里统一拆成数字，并补上约束类别与业务说明。
 *
 * <p>约束顺序与策略配置页的权重编辑器一致（都以 {@code ApsConstraintNames#ALL} 为准），
 * 计划员才能一行行对着看"这条权重配了多少、实际扣了多少分"。
 */
@Schema(description = "管理后台 - APS-求解约束命中 Response VO")
@Data
public class SolverSnapshotConstraintMatchRespVO {

    @Schema(description = "快照编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long snapshotId;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long versionId;

    @Schema(description = "排程版本号", example = "3")
    private Integer versionNo;

    @Schema(description = "排程场景编码", example = "SC202609001")
    private String scenarioCode;

    @Schema(description = "排程场景名称", example = "9 月主排程")
    private String scenarioName;

    @Schema(description = "快照生成时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime snapshotTime;

    @Schema(description = "硬约束总得分", example = "0")
    private Long hardScore;

    @Schema(description = "软约束总得分", example = "-299555670")
    private Long softScore;

    @Schema(description = "各约束的权重、命中次数与扣分，顺序与策略配置页一致")
    private List<Constraint> constraints = new ArrayList<>();

    /** 单条约束的命中情况。 */
    @Schema(description = "管理后台 - APS-约束命中明细")
    @Data
    public static class Constraint {

        @Schema(description = "约束名", requiredMode = Schema.RequiredMode.REQUIRED, example = "dueDateDelay")
        private String constraintName;

        @Schema(description = "约束中文名", requiredMode = Schema.RequiredMode.REQUIRED, example = "交期延误")
        private String displayName;

        @Schema(description = "约束类别：HARD 硬约束、SOFT 软约束", example = "SOFT")
        private String category;

        @Schema(description = "约束类别中文名", example = "软约束")
        private String categoryName;

        @Schema(description = "业务说明：罚什么、权重调大调小会怎样")
        private String description;

        /**
         * 本次求解实际生效的硬权重。
         *
         * <p>取绝对值：Timefold 报的是罚分后的权重（负数），而策略配置页上填的是正数，
         * 两处显示同一个值才能对着看。
         */
        @Schema(description = "本次求解生效的硬约束权重", example = "1")
        private Long hardWeight;

        @Schema(description = "本次求解生效的软约束权重", example = "100")
        private Long softWeight;

        @Schema(description = "命中次数", example = "7")
        private Integer matchCount;

        @Schema(description = "该约束扣掉的硬分", example = "0")
        private Long hardScore;

        @Schema(description = "该约束扣掉的软分", example = "-23975")
        private Long softScore;

    }

}
