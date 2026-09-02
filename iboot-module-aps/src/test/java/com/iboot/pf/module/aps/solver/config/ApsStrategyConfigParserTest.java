package com.iboot.pf.module.aps.solver.config;

import com.iboot.pf.framework.common.util.json.JsonUtils;
import com.iboot.pf.module.aps.solver.constraint.ApsConstraintNames;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SOLVE_DIRECTION_UNSUPPORTED;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SOLVE_STRATEGY_CONFIG_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** 策略配置解析单元测试：默认值、方向限制、未知约束名。 */
class ApsStrategyConfigParserTest {

    @Test
    void blankConfigShouldFallBackToDefaults() {
        ApsStrategyConfig config = ApsStrategyConfigParser.parse(null);
        assertEquals(ApsPlanningDirectionEnum.FORWARD, config.getDirection());
        assertEquals(ApsStrategyConfig.DEFAULT_SPENT_LIMIT_SECONDS, config.spentLimitSecondsOrDefault());
        assertEquals(ApsStrategyConfig.DEFAULT_UNIMPROVED_LIMIT_SECONDS,
                config.unimprovedSpentLimitSecondsOrDefault());
    }

    @Test
    void shouldParseWeightsAndLimits() {
        String json = """
                {
                  "direction": "FORWARD",
                  "spentLimitSeconds": 120,
                  "unimprovedSpentLimitSeconds": 30,
                  "weights": {
                    "dueDateDelay": {"hard": 0, "soft": 200}
                  }
                }
                """;
        ApsStrategyConfig config = ApsStrategyConfigParser.parse(json);
        assertEquals(120L, config.spentLimitSecondsOrDefault());
        assertEquals(30L, config.unimprovedSpentLimitSecondsOrDefault());
        assertEquals(200L, config.getWeights().get(ApsConstraintNames.DUE_DATE_DELAY).softOrZero());
    }

    @Test
    void nonPositiveLimitsShouldFallBackToDefaults() {
        String json = "{\"spentLimitSeconds\": 0, \"unimprovedSpentLimitSeconds\": -5}";
        ApsStrategyConfig config = ApsStrategyConfigParser.parse(json);
        // 配 0 或负数等于没配，回落默认值，不能让求解器拿到 0 秒上限直接返回空解
        assertEquals(ApsStrategyConfig.DEFAULT_SPENT_LIMIT_SECONDS, config.spentLimitSecondsOrDefault());
        assertEquals(ApsStrategyConfig.DEFAULT_UNIMPROVED_LIMIT_SECONDS,
                config.unimprovedSpentLimitSecondsOrDefault());
    }

    @Test
    void unknownConstraintNameShouldFail() {
        String json = "{\"weights\": {\"notExistConstraint\": {\"soft\": 1}}}";
        // 静默忽略会让人以为权重生效了，实际排程结果完全不受影响
        assertServiceException(() -> ApsStrategyConfigParser.parse(json),
                SOLVE_STRATEGY_CONFIG_INVALID, "存在未知约束名 [notExistConstraint]");
    }

    @Test
    void backwardDirectionShouldBeRejected() {
        String json = "{\"direction\": \"BACKWARD\"}";
        assertServiceException(() -> ApsStrategyConfigParser.parse(json), SOLVE_DIRECTION_UNSUPPORTED);
    }

    @Test
    void invalidJsonShouldFail() {
        assertServiceException(() -> ApsStrategyConfigParser.parse("{not-json"),
                SOLVE_STRATEGY_CONFIG_INVALID, "JSON 格式非法");
    }

    @Test
    void structurallyMismatchedConfigShouldReportStructureError() {
        // 库里有一批早于结构化配置的历史数据：是合法 JSON，但权重值不是 {hard, soft} 结构。
        // 提示要说"结构不匹配"，说成格式非法会让人去检查根本没问题的括号
        assertServiceException(() -> ApsStrategyConfigParser.parse("{\"weights\": {\"dueDateDelay\": 100}}"),
                SOLVE_STRATEGY_CONFIG_INVALID, "配置结构与当前版本不匹配");
    }

    @Test
    void unknownTopLevelFieldShouldBeIgnored() {
        // 策略配置会随版本演进，多出来的字段不该让老策略排不了
        ApsStrategyConfig config = ApsStrategyConfigParser.parse("{\"futureField\": 1}");
        assertEquals(ApsPlanningDirectionEnum.FORWARD, config.getDirection());
    }

    @Test
    void fullWeightsShouldSurviveRoundTrip() {
        // 策略保存走的是"结构化配置 -> JSON -> 解析回来"，全量 11 条权重必须一条不丢、一分不差
        Map<String, ApsConstraintWeight> weights = new LinkedHashMap<>();
        long soft = 1L;
        for (String constraintName : ApsConstraintNames.ALL) {
            weights.put(constraintName, new ApsConstraintWeight(soft, soft * 2));
            soft++;
        }
        ApsStrategyConfig source = new ApsStrategyConfig();
        source.setSpentLimitSeconds(600L);
        source.setUnimprovedSpentLimitSeconds(60L);
        source.setMaterialConstrained(Boolean.TRUE);
        source.setWeights(weights);

        ApsStrategyConfig parsed = ApsStrategyConfigParser.parse(JsonUtils.toJsonString(source));

        assertEquals(600L, parsed.spentLimitSecondsOrDefault());
        assertEquals(60L, parsed.unimprovedSpentLimitSecondsOrDefault());
        assertEquals(Boolean.TRUE, parsed.getMaterialConstrained());
        assertEquals(ApsConstraintNames.ALL.size(), parsed.getWeights().size());
        weights.forEach((constraintName, weight) -> {
            assertEquals(weight.hardOrZero(), parsed.getWeights().get(constraintName).hardOrZero());
            assertEquals(weight.softOrZero(), parsed.getWeights().get(constraintName).softOrZero());
        });
    }

}
