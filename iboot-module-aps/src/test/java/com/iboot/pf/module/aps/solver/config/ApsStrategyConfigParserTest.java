package com.iboot.pf.module.aps.solver.config;

import com.iboot.pf.module.aps.solver.constraint.ApsConstraintNames;
import org.junit.jupiter.api.Test;

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
    void unknownTopLevelFieldShouldBeIgnored() {
        // 策略配置会随版本演进，多出来的字段不该让老策略排不了
        ApsStrategyConfig config = ApsStrategyConfigParser.parse("{\"futureField\": 1}");
        assertEquals(ApsPlanningDirectionEnum.FORWARD, config.getDirection());
    }

}
