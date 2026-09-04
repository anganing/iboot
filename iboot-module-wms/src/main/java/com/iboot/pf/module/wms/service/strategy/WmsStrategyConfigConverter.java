package com.iboot.pf.module.wms.service.strategy;

import cn.hutool.core.util.StrUtil;
import com.iboot.pf.framework.common.exception.ServiceException;
import com.iboot.pf.framework.common.util.json.JsonUtils;
import com.iboot.pf.module.wms.controller.admin.strategy.vo.StrategyConfigVO;
import com.iboot.pf.module.wms.controller.admin.strategy.vo.StrategyRespVO;
import com.iboot.pf.module.wms.enums.WmsStrategyTypeEnum;
import com.iboot.pf.module.wms.strategy.config.WmsStrategyCondition;
import com.iboot.pf.module.wms.strategy.config.WmsStrategyConfig;
import com.iboot.pf.module.wms.strategy.config.WmsStrategyConfigParser;
import com.iboot.pf.module.wms.strategy.config.WmsStrategySort;
import com.iboot.pf.module.wms.strategy.field.WmsStrategyFieldCatalog;

import java.util.ArrayList;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_TYPE_UNSUPPORTED;

/**
 * 策略配置在"接口 VO"与"{@code config_json}"之间的双向转换。
 *
 * <p>写路径严格、读路径宽松，这是两条路径的目标不同决定的：保存时必须把非法配置挡在库外，
 * 否则错误会一直潜伏到货真的上错架；打开时如果同样严格，库里那些历史废配置就永远打不开，
 * 只能靠 DML 去修。读路径回落默认值并带上原因，仓管保存一次就自动修正了。
 */
public final class WmsStrategyConfigConverter {

    private WmsStrategyConfigConverter() {
    }

    /**
     * 结构化配置转 {@code config_json}。
     *
     * <p>序列化完再过一遍 {@link WmsStrategyConfigParser}：Bean Validation 只能管到"字段非空、
     * 算子能识别"，管不了"这个字段属不属于这个策略类型""值类型对不对得上"。用引擎将来要用的
     * 同一个解析器做终检，才能保证存进去的一定能拿去执行。
     */
    public static String toConfigJson(String strategyType, StrategyConfigVO configVO) {
        WmsStrategyTypeEnum type = WmsStrategyTypeEnum.of(strategyType);
        if (type == null) {
            throw exception(STRATEGY_TYPE_UNSUPPORTED);
        }
        String configJson = JsonUtils.toJsonString(toDomainConfig(type, configVO));
        WmsStrategyConfigParser.parse(type, configJson);
        return configJson;
    }

    /**
     * 把库里的 {@code config_json} 填进响应 VO。
     *
     * <p>解析失败不抛异常：置 {@code configInvalid} 让前端弹告警，配置本身回落成该类型的默认值，
     * 这样页面能打开、能改、能存。
     */
    public static void fillConfig(StrategyRespVO respVO) {
        if (respVO == null) {
            return;
        }
        try {
            respVO.setConfig(toConfigVO(
                    WmsStrategyConfigParser.parse(respVO.getStrategyType(), respVO.getConfigJson())));
            respVO.setConfigInvalid(Boolean.FALSE);
        } catch (ServiceException ex) {
            respVO.setConfig(defaultConfigVO(respVO.getStrategyType()));
            respVO.setConfigInvalid(Boolean.TRUE);
            respVO.setConfigInvalidReason(ex.getMessage());
        }
    }

    /** 某类型的默认配置：新建策略、页面上切换类型、读路径回落都取自它。 */
    public static StrategyConfigVO defaultConfigVO(String strategyType) {
        return defaultConfigVO(WmsStrategyTypeEnum.of(strategyType));
    }

    /**
     * 某类型的默认配置。
     *
     * <p>默认是"不筛不排"而不是给一套推荐规则：筛什么、按什么排完全取决于现场的库位规划，
     * 预填一套只会让人以为那是必须的。类型为 {@code null} 时给空配置，让读路径还能把页面打开。
     */
    public static StrategyConfigVO defaultConfigVO(WmsStrategyTypeEnum strategyType) {
        WmsStrategyConfig config = new WmsStrategyConfig();
        if (strategyType != WmsStrategyTypeEnum.PUTAWAY) {
            // 混批开关只对上架有意义，其它类型留空，免得页面上多出一个不该有的选项
            config.setAllowMixedLot(null);
        }
        return toConfigVO(config);
    }

    /**
     * 接口 VO 转领域配置，顺带按类型清掉不属于该类型的字段。
     *
     * <p>清理是必要的一步而不是洁癖：前端切换策略类型时，上一个类型填过的专属字段还留在表单值里，
     * 原样落库会让库里出现"拣选策略带着补货量"这种自相矛盾的配置。在这里按类型只取本类型字段，
     * 保存一次就干净了——这也是解析器读路径可以放过多余字段的前提。
     */
    private static WmsStrategyConfig toDomainConfig(WmsStrategyTypeEnum strategyType,
                                                    StrategyConfigVO configVO) {
        WmsStrategyConfig config = new WmsStrategyConfig();
        config.setAllowMixedLot(null);
        if (configVO == null) {
            return config;
        }
        config.setConditions(toDomainConditions(configVO.getConditions()));
        // 该类型没有可排序字段就不落 sorts，让库里的配置跟页面上看到的一致
        if (!WmsStrategyFieldCatalog.sortFields(strategyType).isEmpty()) {
            config.setSorts(toDomainSorts(configVO.getSorts()));
        }
        switch (strategyType) {
            case PUTAWAY -> {
                config.setDimensionCheck(StrUtil.blankToDefault(configVO.getDimensionCheck(), null));
                config.setAllowMixedLot(Boolean.TRUE.equals(configVO.getAllowMixedLot()));
            }
            case REPLENISH -> {
                config.setFromWhCode(StrUtil.blankToDefault(configVO.getFromWhCode(), null));
                config.setReplenishQty(configVO.getReplenishQty());
            }
            case PICK -> { }
        }
        return config;
    }

    private static List<WmsStrategyCondition> toDomainConditions(List<StrategyConfigVO.Condition> conditions) {
        List<WmsStrategyCondition> domainConditions = new ArrayList<>();
        if (conditions == null) {
            return domainConditions;
        }
        conditions.forEach(condition -> domainConditions.add(
                new WmsStrategyCondition(condition.getField(), condition.getOp(), condition.getValue())));
        return domainConditions;
    }

    private static List<WmsStrategySort> toDomainSorts(List<StrategyConfigVO.Sort> sorts) {
        List<WmsStrategySort> domainSorts = new ArrayList<>();
        if (sorts == null) {
            return domainSorts;
        }
        sorts.forEach(sort -> domainSorts.add(new WmsStrategySort(sort.getField(), sort.getOrder())));
        return domainSorts;
    }

    private static StrategyConfigVO toConfigVO(WmsStrategyConfig config) {
        StrategyConfigVO configVO = new StrategyConfigVO();
        List<StrategyConfigVO.Condition> conditions = new ArrayList<>();
        if (config.getConditions() != null) {
            config.getConditions().forEach(condition -> {
                StrategyConfigVO.Condition conditionVO = new StrategyConfigVO.Condition();
                conditionVO.setField(condition.getField());
                conditionVO.setOp(condition.getOp());
                conditionVO.setValue(condition.getValue());
                conditions.add(conditionVO);
            });
        }
        configVO.setConditions(conditions);
        List<StrategyConfigVO.Sort> sorts = new ArrayList<>();
        if (config.getSorts() != null) {
            config.getSorts().forEach(sort -> {
                StrategyConfigVO.Sort sortVO = new StrategyConfigVO.Sort();
                sortVO.setField(sort.getField());
                sortVO.setOrder(sort.getOrder());
                sorts.add(sortVO);
            });
        }
        configVO.setSorts(sorts);
        configVO.setDimensionCheck(config.getDimensionCheck());
        configVO.setAllowMixedLot(config.getAllowMixedLot());
        configVO.setFromWhCode(config.getFromWhCode());
        configVO.setReplenishQty(config.getReplenishQty());
        return configVO;
    }

}
