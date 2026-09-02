package com.iboot.pf.module.aps.service.strategy;

import com.iboot.pf.framework.common.exception.ServiceException;
import com.iboot.pf.framework.common.util.json.JsonUtils;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategyConfigVO;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategyRespVO;
import com.iboot.pf.module.aps.solver.ApsConstraintWeightResolver;
import com.iboot.pf.module.aps.solver.config.ApsConstraintWeight;
import com.iboot.pf.module.aps.solver.config.ApsPlanningDirectionEnum;
import com.iboot.pf.module.aps.solver.config.ApsStrategyConfig;
import com.iboot.pf.module.aps.solver.config.ApsStrategyConfigParser;
import com.iboot.pf.module.aps.solver.constraint.ApsConstraintNames;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 策略配置在"接口 VO"与"{@code config_json}"之间的双向转换。
 *
 * <p>写路径严格、读路径宽松，这是两条路径的目标不同决定的：保存时必须把非法配置挡在库外，
 * 否则错误会一直潜伏到点"生成排程"才暴露；打开时如果同样严格，库里那些历史废配置就永远打不开，
 * 只能靠 DML 去修。读路径回落默认值并带上原因，计划员保存一次就自动修正了。
 */
public final class ApsStrategyConfigConverter {

    private ApsStrategyConfigConverter() {
    }

    /**
     * 结构化配置转 {@code config_json}。
     *
     * <p>序列化完再过一遍 {@link ApsStrategyConfigParser}：Bean Validation 只管数值范围，
     * 管不了"约束名是否存在"。用求解链路的同一个解析器做终检，能保证"能存进去的一定能拿去求解"。
     */
    public static String toConfigJson(StrategyConfigVO configVO) {
        ApsStrategyConfig config = new ApsStrategyConfig();
        // 只支持正排，方向不由前端决定，直接写死
        config.setDirection(ApsPlanningDirectionEnum.FORWARD);
        config.setSpentLimitSeconds(configVO.getSpentLimitSeconds());
        config.setUnimprovedSpentLimitSeconds(configVO.getUnimprovedSpentLimitSeconds());
        config.setMaterialConstrained(configVO.getMaterialConstrained());
        config.setWeights(toDomainWeights(configVO.getWeights()));
        String configJson = JsonUtils.toJsonString(config);
        ApsStrategyConfigParser.parse(configJson);
        return configJson;
    }

    /**
     * 把库里的 {@code config_json} 填进响应 VO。
     *
     * <p>解析失败不抛异常：置 {@code configInvalid} 让前端弹告警，配置本身回落成全默认值，
     * 这样页面能打开、能改、能存。
     */
    public static void fillConfig(StrategyRespVO respVO) {
        if (respVO == null) {
            return;
        }
        try {
            respVO.setConfig(toConfigVO(ApsStrategyConfigParser.parse(respVO.getConfigJson())));
            respVO.setConfigInvalid(Boolean.FALSE);
        } catch (ServiceException ex) {
            respVO.setConfig(defaultConfigVO());
            respVO.setConfigInvalid(Boolean.TRUE);
            respVO.setConfigInvalidReason(ex.getMessage());
        }
    }

    /** 全默认配置，供新建策略、恢复默认与读路径回落使用。 */
    public static StrategyConfigVO defaultConfigVO() {
        return toConfigVO(new ApsStrategyConfig());
    }

    /**
     * 求解域配置转接口 VO。
     *
     * <p>权重按 {@code ApsConstraintNames.ALL} 补全：策略里没配的约束用代码默认权重填上，
     * 让页面上 11 条约束的实际生效权重都是明的，而不是"没显示就是走默认"这种要靠猜的状态。
     */
    private static StrategyConfigVO toConfigVO(ApsStrategyConfig config) {
        StrategyConfigVO configVO = new StrategyConfigVO();
        configVO.setSpentLimitSeconds(config.spentLimitSecondsOrDefault());
        configVO.setUnimprovedSpentLimitSeconds(config.unimprovedSpentLimitSecondsOrDefault());
        configVO.setMaterialConstrained(Boolean.TRUE.equals(config.getMaterialConstrained()));
        Map<String, ApsConstraintWeight> configured = config.getWeights() == null
                ? Map.of() : config.getWeights();
        Map<String, StrategyConfigVO.ConstraintWeight> weights = new LinkedHashMap<>();
        for (String constraintName : ApsConstraintNames.ALL) {
            ApsConstraintWeight weight = configured.getOrDefault(constraintName,
                    ApsConstraintWeightResolver.defaultWeights().get(constraintName));
            weights.put(constraintName, toWeightVO(weight));
        }
        configVO.setWeights(weights);
        return configVO;
    }

    private static StrategyConfigVO.ConstraintWeight toWeightVO(ApsConstraintWeight weight) {
        StrategyConfigVO.ConstraintWeight weightVO = new StrategyConfigVO.ConstraintWeight();
        weightVO.setHard(weight == null ? 0L : weight.hardOrZero());
        weightVO.setSoft(weight == null ? 0L : weight.softOrZero());
        return weightVO;
    }

    private static Map<String, ApsConstraintWeight> toDomainWeights(
            Map<String, StrategyConfigVO.ConstraintWeight> weights) {
        Map<String, ApsConstraintWeight> domainWeights = new LinkedHashMap<>();
        if (weights == null) {
            return domainWeights;
        }
        weights.forEach((constraintName, weight) -> domainWeights.put(constraintName,
                new ApsConstraintWeight(weight.getHard(), weight.getSoft())));
        return domainWeights;
    }

}
