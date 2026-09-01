package com.iboot.pf.module.aps.solver.calculate;

import com.iboot.pf.module.aps.enums.ApsDurationTypeEnum;
import com.iboot.pf.module.aps.solver.model.ApsSolverResource;
import com.iboot.pf.module.aps.solver.model.ApsSolverTask;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 工时计算单元测试：固定工时、按件累加、双效率折算、换型叠加。 */
class ApsDurationCalculatorTest {

    private ApsSolverTask buildTask(String durationType, int setup, String perUnit, int teardown,
                                    String qty, int stdDuration) {
        ApsSolverTask task = new ApsSolverTask();
        task.setOpId(10L);
        task.setDurationType(durationType);
        task.setSetupMinutes(setup);
        task.setStdMinutesPerUnit(new BigDecimal(perUnit));
        task.setTeardownMinutes(teardown);
        task.setQty(new BigDecimal(qty));
        task.setStdDuration(stdDuration);
        return task;
    }

    private ApsSolverResource buildResource(String efficiency, String capabilityEfficiency) {
        ApsSolverResource resource = new ApsSolverResource();
        resource.setId(1L);
        resource.setCode("CNC-01");
        resource.setEfficiency(efficiency == null ? null : new BigDecimal(efficiency));
        Map<Long, BigDecimal> capabilities = new LinkedHashMap<>();
        if (capabilityEfficiency != null) {
            capabilities.put(10L, new BigDecimal(capabilityEfficiency));
        }
        resource.setCapabilityEfficiencies(capabilities);
        return resource;
    }

    @Test
    void fixedDurationShouldIgnoreQtyAndEfficiency() {
        ApsSolverTask task = buildTask(ApsDurationTypeEnum.FIXED.name(), 30, "1.5", 10, "1000", 120);
        ApsSolverResource resource = buildResource("2.0", "2.0");
        // 固定工时按标准时长取值，不随数量与效率变化
        assertEquals(120, ApsDurationCalculator.calculateWorkMinutes(task, resource));
    }

    @Test
    void perUnitShouldAccumulateSetupAndTeardown() {
        ApsSolverTask task = buildTask(ApsDurationTypeEnum.PER_UNIT.name(), 30, "1.5", 10, "100", 0);
        ApsSolverResource resource = buildResource("1.0", null);
        // 30 + 100 * 1.5 / 1.0 + 10 = 190
        assertEquals(190, ApsDurationCalculator.calculateWorkMinutes(task, resource));
    }

    @Test
    void efficiencyShouldOnlyScaleProcessingTime() {
        ApsSolverTask task = buildTask(ApsDurationTypeEnum.PER_UNIT.name(), 30, "1.5", 10, "100", 0);
        // 资源效率 1.5 × 能力效率 2.0 = 3.0，加工工时 150 / 3 = 50；准备与收尾不折算
        ApsSolverResource resource = buildResource("1.5", "2.0");
        assertEquals(30 + 50 + 10, ApsDurationCalculator.calculateWorkMinutes(task, resource));
    }

    @Test
    void nonPositiveEfficiencyShouldFallBackToOne() {
        ApsSolverTask task = buildTask(ApsDurationTypeEnum.PER_UNIT.name(), 0, "1", 0, "10", 0);
        // 效率配成 0 会除零，必须回落到 1，否则工时算成无穷
        ApsSolverResource resource = buildResource("0", "0");
        assertEquals(10, ApsDurationCalculator.calculateWorkMinutes(task, resource));
    }

    @Test
    void processingMinutesShouldCeilUp() {
        ApsSolverTask task = buildTask(ApsDurationTypeEnum.PER_UNIT.name(), 0, "0.3", 0, "10", 0);
        ApsSolverResource resource = buildResource("1.0", null);
        // 10 * 0.3 = 3 分钟整
        assertEquals(3, ApsDurationCalculator.calculateWorkMinutes(task, resource));

        ApsSolverTask fractional = buildTask(ApsDurationTypeEnum.PER_UNIT.name(), 0, "0.35", 0, "10", 0);
        // 3.5 分钟向上取整成 4：宁可多留产能，也不让计划比实际乐观
        assertEquals(4, ApsDurationCalculator.calculateWorkMinutes(fractional, resource));
    }

    @Test
    void changeoverShouldComeFromMatrixAndStackOnTotal() {
        ApsSolverTask task = buildTask(ApsDurationTypeEnum.PER_UNIT.name(), 10, "1", 0, "10", 0);
        task.setChangeoverGroup("RED");
        ApsSolverTask previous = buildTask(ApsDurationTypeEnum.PER_UNIT.name(), 0, "1", 0, "1", 0);
        previous.setChangeoverGroup("BLUE");
        ApsSolverResource resource = buildResource("1.0", null);
        Map<String, Integer> changeovers = new LinkedHashMap<>();
        changeovers.put("BLUE->RED", 25);
        resource.setChangeoverMinutes(changeovers);

        assertEquals(25, ApsDurationCalculator.calculateChangeoverMinutes(task, resource, previous));
        // 总时长 = 换型 25 + 净工时（10 + 10）
        assertEquals(45, ApsDurationCalculator.calculateTotalMinutes(task, resource, previous));
    }

    @Test
    void sameGroupOrMissingMatrixShouldNotProduceChangeover() {
        ApsSolverTask task = buildTask(ApsDurationTypeEnum.PER_UNIT.name(), 0, "1", 0, "1", 0);
        task.setChangeoverGroup("RED");
        ApsSolverTask sameGroup = buildTask(ApsDurationTypeEnum.PER_UNIT.name(), 0, "1", 0, "1", 0);
        sameGroup.setChangeoverGroup("RED");
        ApsSolverResource resource = buildResource("1.0", null);
        resource.setChangeoverMinutes(Map.of("RED->RED", 99));

        // 同分组不换型；矩阵里配了也不算
        assertEquals(0, ApsDurationCalculator.calculateChangeoverMinutes(task, resource, sameGroup));
        // 队首任务没有前道，不产生换型
        assertEquals(0, ApsDurationCalculator.calculateChangeoverMinutes(task, resource, null));

        ApsSolverTask blankGroup = buildTask(ApsDurationTypeEnum.PER_UNIT.name(), 0, "1", 0, "1", 0);
        blankGroup.setChangeoverGroup("");
        // 换型分组为空表示不参与换型计算，不能凭空造损失
        assertEquals(0, ApsDurationCalculator.calculateChangeoverMinutes(task, resource, blankGroup));
    }

}
