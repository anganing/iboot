package com.iboot.pf.module.aps.solver.config;

import cn.hutool.core.util.StrUtil;
import com.iboot.pf.framework.common.util.json.JsonUtils;
import com.iboot.pf.module.aps.solver.constraint.ApsConstraintNames;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SOLVE_DIRECTION_UNSUPPORTED;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SOLVE_STRATEGY_CONFIG_INVALID;

/**
 * 解析 {@code aps_strategy.config_json}。
 *
 * <p>三件事：容错解析、补默认值、校验约束名。校验放在解析这一步而不是等 Timefold 报错，
 * 是为了让计划员在点“生成排程”时就拿到“约束名写错了”这样的中文提示，
 * 而不是一句英文的 IllegalArgumentException。
 */
public final class ApsStrategyConfigParser {

    private ApsStrategyConfigParser() {
    }

    /**
     * 解析策略配置。
     *
     * @param configJson {@code aps_strategy.config_json} 的内容，允许为空
     * @return 策略配置；为空时返回全默认配置
     */
    public static ApsStrategyConfig parse(String configJson) {
        if (StrUtil.isBlank(configJson)) {
            // 策略没配就用默认权重跑，不阻塞排程：默认值本身是一套可用的偏好
            return new ApsStrategyConfig();
        }
        ApsStrategyConfig config;
        try {
            config = JsonUtils.parseObject(configJson, ApsStrategyConfig.class);
        } catch (Exception ex) {
            throw exception(SOLVE_STRATEGY_CONFIG_INVALID, "JSON 格式非法");
        }
        if (config == null) {
            throw exception(SOLVE_STRATEGY_CONFIG_INVALID, "内容为空");
        }
        validateDirection(config);
        validateWeights(config);
        return config;
    }

    /** 倒排还没实现，配了它就直接拒绝，而不是悄悄按正排跑出一份对不上预期的计划。 */
    private static void validateDirection(ApsStrategyConfig config) {
        if (config.getDirection() == null) {
            config.setDirection(ApsPlanningDirectionEnum.FORWARD);
            return;
        }
        if (config.getDirection() != ApsPlanningDirectionEnum.FORWARD) {
            throw exception(SOLVE_DIRECTION_UNSUPPORTED);
        }
    }

    /** 未知约束名一律报错：静默忽略会让人以为权重生效了，实际排程结果完全不受影响。 */
    private static void validateWeights(ApsStrategyConfig config) {
        if (config.getWeights() == null || config.getWeights().isEmpty()) {
            return;
        }
        Set<String> unknown = new LinkedHashSet<>();
        config.getWeights().keySet().forEach(name -> {
            if (!ApsConstraintNames.ALL.contains(name)) {
                unknown.add(name);
            }
        });
        if (!unknown.isEmpty()) {
            throw exception(SOLVE_STRATEGY_CONFIG_INVALID, "存在未知约束名 " + unknown);
        }
    }

}
