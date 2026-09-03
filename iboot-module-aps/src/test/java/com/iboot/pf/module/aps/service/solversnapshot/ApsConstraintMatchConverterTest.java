package com.iboot.pf.module.aps.service.solversnapshot;

import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.SolverSnapshotConstraintMatchRespVO;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.SolverSnapshotRespVO;
import com.iboot.pf.module.aps.solver.constraint.ApsConstraintNames;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 约束命中快照的解析。
 *
 * <p>这段解析是页面上"为什么是这个分数"的唯一数据来源，而它读的是历史留痕数据：
 * {@code aps_solver_snapshot} 里同时存在当前结构与早期版本写的扁平结构。解析必须做到
 * 认得的翻好、认不得的不抛错，否则一条老快照就能让整个详情页打不开。
 */
class ApsConstraintMatchConverterTest {

    @Test
    void shouldParseScoreAndWeightIntoNumbers() {
        SolverSnapshotRespVO snapshot = buildSnapshot("""
                {"score":"-8845hard/-299562425soft","constraints":[
                  {"constraintName":"dueDateDelay","displayName":"交期延误",
                   "weight":"0hard/-100soft","score":"0hard/-299520000soft","matchCount":3},
                  {"constraintName":"calendarCapacity","displayName":"超出日历可用产能",
                   "weight":"-1hard/0soft","score":"-8845hard/0soft","matchCount":9}]}
                """);

        SolverSnapshotConstraintMatchRespVO result = ApsConstraintMatchConverter.convert(snapshot);

        assertEquals(-8845L, result.getHardScore());
        assertEquals(-299_562_425L, result.getSoftScore());
        assertEquals(2, result.getConstraints().size());
        // 顺序按约束目录重排：日历产能是硬约束，排在交期延误之前
        assertEquals(ApsConstraintNames.CALENDAR_CAPACITY, result.getConstraints().get(0).getConstraintName());

        SolverSnapshotConstraintMatchRespVO.Constraint dueDate = result.getConstraints().get(1);
        // 权重取绝对值，和策略配置页上填的正数对得上
        assertEquals(100L, dueDate.getSoftWeight());
        assertEquals(0L, dueDate.getHardWeight());
        assertEquals(-299_520_000L, dueDate.getSoftScore());
        assertEquals(3, dueDate.getMatchCount());
        // 类别与业务说明来自约束目录，不取快照里的旧文案
        assertEquals("SOFT", dueDate.getCategory());
        assertEquals("软约束", dueDate.getCategoryName());
        assertTrue(dueDate.getDescription().contains("交期"));
    }

    @Test
    void shouldCarryVersionContextForReadability() {
        SolverSnapshotRespVO snapshot = buildSnapshot("{}");
        snapshot.setId(1001L);
        snapshot.setVersionId(2002L);
        snapshot.setVersionNo(3);
        snapshot.setScenarioCode("SC202609001");
        snapshot.setScenarioName("9 月主排程");

        SolverSnapshotConstraintMatchRespVO result = ApsConstraintMatchConverter.convert(snapshot);

        assertEquals(1001L, result.getSnapshotId());
        assertEquals(2002L, result.getVersionId());
        assertEquals(3, result.getVersionNo());
        assertEquals("SC202609001", result.getScenarioCode());
        assertEquals("9 月主排程", result.getScenarioName());
        assertTrue(result.getConstraints().isEmpty());
    }

    @Test
    void shouldTolerateLegacySnapshotStructure() {
        // 早期版本写的是 matches/constraint/count 扁平结构，认不出来时给空清单，让前端回落到原始 JSON
        SolverSnapshotRespVO snapshot = buildSnapshot(
                "{\"matches\":[{\"count\":2,\"score\":-2,\"constraint\":\"resourceCapacity\"}]}");

        SolverSnapshotConstraintMatchRespVO result = ApsConstraintMatchConverter.convert(snapshot);

        assertNotNull(result);
        assertTrue(result.getConstraints().isEmpty());
        assertNull(result.getHardScore());
    }

    @Test
    void shouldTolerateBrokenJsonAndUnknownConstraint() {
        assertTrue(ApsConstraintMatchConverter.convert(buildSnapshot("not a json")).getConstraints().isEmpty());

        SolverSnapshotRespVO snapshot = buildSnapshot("""
                {"score":null,"constraints":[
                  {"constraintName":"legacyConstraint","displayName":"历史约束","matchCount":1}]}
                """);
        List<SolverSnapshotConstraintMatchRespVO.Constraint> constraints =
                ApsConstraintMatchConverter.convert(snapshot).getConstraints();

        assertEquals(1, constraints.size());
        // 已改过名的约束在目录里查不到：保留快照里的中文名，权重与得分留空显示"-"
        assertEquals("历史约束", constraints.get(0).getDisplayName());
        assertNull(constraints.get(0).getCategory());
        assertNull(constraints.get(0).getSoftWeight());
    }

    private SolverSnapshotRespVO buildSnapshot(String json) {
        SolverSnapshotRespVO snapshot = new SolverSnapshotRespVO();
        snapshot.setSnapshotJson(json);
        return snapshot;
    }

}
