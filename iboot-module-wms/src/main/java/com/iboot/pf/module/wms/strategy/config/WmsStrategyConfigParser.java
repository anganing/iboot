package com.iboot.pf.module.wms.strategy.config;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.iboot.pf.framework.common.util.json.JsonUtils;
import com.iboot.pf.module.wms.enums.WmsDimensionCheckModeEnum;
import com.iboot.pf.module.wms.enums.WmsStrategyTypeEnum;
import com.iboot.pf.module.wms.strategy.field.WmsStrategyConditionField;
import com.iboot.pf.module.wms.strategy.field.WmsStrategyFieldCatalog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_CONFIG_INVALID;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_TYPE_UNSUPPORTED;

/**
 * 解析并校验 {@code wms_strategy.config_json}。
 *
 * <p>校验必须带策略类型：同一个字段名在不同类型下的含义和合法性都不同（{@code qtyAvailable}
 * 在拣选里是"这条余额能拣多少"，在补货里是"线边还剩多少"，而在上架里根本不该出现）。
 *
 * <p>所有提示语都写成中文并带上位置（第几条条件、哪个字段），因为它们会原样显示给配策略的仓管。
 * 一句 {@code IllegalArgumentException} 只能让人回来问开发。
 */
public final class WmsStrategyConfigParser {

    private WmsStrategyConfigParser() {
    }

    /** 按类型名解析；类型名无法识别时直接拒绝，不猜。 */
    public static WmsStrategyConfig parse(String strategyType, String configJson) {
        WmsStrategyTypeEnum type = WmsStrategyTypeEnum.of(strategyType);
        if (type == null) {
            throw exception(STRATEGY_TYPE_UNSUPPORTED);
        }
        return parse(type, configJson);
    }

    /**
     * 解析策略配置。
     *
     * @param strategyType 策略类型，决定哪些字段合法
     * @param configJson {@code wms_strategy.config_json} 的内容，允许为空
     * @return 校验通过的配置；配置为空时返回全默认配置
     */
    public static WmsStrategyConfig parse(WmsStrategyTypeEnum strategyType, String configJson) {
        WmsStrategyConfig config = readConfig(configJson);
        // null 归一成空列表，后面的校验和将来的引擎都不用再判 null
        if (config.getConditions() == null) {
            config.setConditions(new ArrayList<>());
        }
        if (config.getSorts() == null) {
            config.setSorts(new ArrayList<>());
        }
        validateConditions(strategyType, config);
        validateSorts(strategyType, config);
        validateTypeOptions(strategyType, config);
        return config;
    }

    private static WmsStrategyConfig readConfig(String configJson) {
        if (StrUtil.isBlank(configJson)) {
            // 没配等于不筛不排，是合法状态：策略骨架先建起来、配置慢慢补是常见节奏
            return new WmsStrategyConfig();
        }
        WmsStrategyConfig config;
        try {
            config = JsonUtils.parseObject(configJson, WmsStrategyConfig.class);
        } catch (Exception ex) {
            // 区分"不是 JSON"与"是 JSON 但结构对不上"：这句会直接显示给仓管，
            // 把结构不匹配说成格式非法，只会让人去检查根本没问题的括号
            throw exception(STRATEGY_CONFIG_INVALID,
                    JsonUtils.isJsonObject(configJson) ? "配置结构与当前版本不匹配" : "JSON 格式非法");
        }
        if (config == null) {
            throw exception(STRATEGY_CONFIG_INVALID, "内容为空");
        }
        return config;
    }

    /** 条件的字段、算子、值形态三件事都要跟字段目录对得上，任一项错都会让引擎筛出一批错的候选。 */
    private static void validateConditions(WmsStrategyTypeEnum strategyType, WmsStrategyConfig config) {
        for (int index = 0; index < config.getConditions().size(); index++) {
            WmsStrategyCondition condition = config.getConditions().get(index);
            String position = "第 " + (index + 1) + " 条筛选条件";
            if (condition == null) {
                throw exception(STRATEGY_CONFIG_INVALID, position + "为空");
            }
            WmsStrategyConditionField field =
                    WmsStrategyFieldCatalog.conditionField(strategyType, condition.getField());
            if (field == null) {
                throw exception(STRATEGY_CONFIG_INVALID, position + "的字段 "
                        + StrUtil.blankToDefault(condition.getField(), "（空）")
                        + " 不在" + strategyType.getName() + "的可筛字段里");
            }
            if (condition.getOp() == null) {
                throw exception(STRATEGY_CONFIG_INVALID, position + "的算子无法识别");
            }
            if (!field.operators().contains(condition.getOp())) {
                throw exception(STRATEGY_CONFIG_INVALID, position + "（" + field.displayName()
                        + "）不支持" + condition.getOp().getName() + "这个算子");
            }
            validateValue(position, field, condition);
        }
    }

    /**
     * 值的形态与类型。
     *
     * <p>类型判得住是因为 JSON 自带类型：{@code "50"} 和 {@code 50} 反序列化出来一个是 String
     * 一个是 Number。放过字符串形式的数字，引擎按字符串比大小会得出 9 大于 10 这种结果。
     */
    private static void validateValue(String position, WmsStrategyConditionField field,
                                      WmsStrategyCondition condition) {
        Object value = condition.getValue();
        String operatorName = condition.getOp().getName();
        if (condition.getOp().isMultiValue()) {
            if (!(value instanceof Collection<?> values)) {
                throw exception(STRATEGY_CONFIG_INVALID,
                        position + "用了" + operatorName + "，值要给一组而不是单个");
            }
            if (values.isEmpty()) {
                // 空集合等于筛不出任何候选，配了跟没配完全是两个结果，不能当成"没配"放过
                throw exception(STRATEGY_CONFIG_INVALID,
                        position + "（" + field.displayName() + "）至少要选一个值");
            }
            values.forEach(item -> validateSingleValue(position, field, item));
            return;
        }
        if (value instanceof Collection<?>) {
            throw exception(STRATEGY_CONFIG_INVALID,
                    position + "用了" + operatorName + "，值只能给一个");
        }
        validateSingleValue(position, field, value);
    }

    private static void validateSingleValue(String position, WmsStrategyConditionField field, Object value) {
        if (value == null || (value instanceof String text && text.isBlank())) {
            throw exception(STRATEGY_CONFIG_INVALID, position + "（" + field.displayName() + "）没有填值");
        }
        boolean matched = switch (field.valueType()) {
            case NUMBER -> value instanceof Number;
            case BOOLEAN -> value instanceof Boolean;
            case ENUM, TEXT -> value instanceof String;
        };
        if (!matched) {
            throw exception(STRATEGY_CONFIG_INVALID, position + "（" + field.displayName()
                    + "）的值应该是" + field.valueType().getName() + "类型");
        }
    }

    /** 排序字段不许重复：同一字段排两次，第二次永远不生效，是明确的配置错误而不是无害冗余。 */
    private static void validateSorts(WmsStrategyTypeEnum strategyType, WmsStrategyConfig config) {
        Set<String> seenFields = new LinkedHashSet<>();
        for (int index = 0; index < config.getSorts().size(); index++) {
            WmsStrategySort sort = config.getSorts().get(index);
            String position = "第 " + (index + 1) + " 条排序规则";
            if (sort == null) {
                throw exception(STRATEGY_CONFIG_INVALID, position + "为空");
            }
            if (!WmsStrategyFieldCatalog.supportsSortField(strategyType, sort.getField())) {
                throw exception(STRATEGY_CONFIG_INVALID, position + "的字段 "
                        + StrUtil.blankToDefault(sort.getField(), "（空）")
                        + " 不在" + strategyType.getName() + "的可排序字段里");
            }
            if (sort.getOrder() == null) {
                throw exception(STRATEGY_CONFIG_INVALID, position + "的排序方向无法识别");
            }
            if (!seenFields.add(sort.getField())) {
                throw exception(STRATEGY_CONFIG_INVALID, position + "的字段与前面的重复了");
            }
        }
    }

    /**
     * 类型专属选项。
     *
     * <p>只校验该类型自己的字段，不追究"有没有多余字段"：多余字段对引擎无害（引擎按类型只读自己
     * 关心的那几个），而读路径一旦判非法，用户会连带丢掉整份还能用的配置。写路径由
     * {@code WmsStrategyConfigConverter} 按类型只取本类型字段，保存一次多余的就没了。
     */
    private static void validateTypeOptions(WmsStrategyTypeEnum strategyType, WmsStrategyConfig config) {
        switch (strategyType) {
            case PUTAWAY -> validatePutaway(config);
            case REPLENISH -> validateReplenish(config);
            // 拣选没有专属选项：筛条件加排序链已经够表达 FIFO、FEFO、就近这些规则
            case PICK -> { }
        }
    }

    private static void validatePutaway(WmsStrategyConfig config) {
        String mode = config.getDimensionCheck();
        if (StrUtil.isBlank(mode)) {
            // 留空合法，表示按库位自己的尺寸校验模式判；归一成 null，免得库里同时出现 null 和空串
            config.setDimensionCheck(null);
            return;
        }
        if (!ArrayUtil.contains(WmsDimensionCheckModeEnum.ARRAYS, mode)) {
            throw exception(STRATEGY_CONFIG_INVALID, "尺寸校验模式 " + mode + " 无法识别");
        }
    }

    /**
     * 补货专属字段的结构合法性。
     *
     * <p>这里只管"填了的值本身对不对"，不管"该填的填齐了没"：策略是先建草稿再慢慢配的，
     * 一上来就要求补货三件套齐全，人就没法把草稿存下来。齐不齐由
     * {@code WmsStrategyServiceImpl} 在启用时校验——那才是配置必须完整的时刻。
     */
    private static void validateReplenish(WmsStrategyConfig config) {
        if (config.getReplenishQty() != null && config.getReplenishQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(STRATEGY_CONFIG_INVALID, "单次补货量要大于 0");
        }
        config.setFromWhCode(StrUtil.blankToDefault(config.getFromWhCode(), null));
    }

}
