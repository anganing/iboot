package com.iboot.pf.module.wms.strategy.config;

import com.iboot.pf.module.wms.enums.WmsStrategyTypeEnum;
import com.iboot.pf.module.wms.strategy.field.WmsStrategyOperatorEnum;
import org.junit.jupiter.api.Test;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_CONFIG_INVALID;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_TYPE_UNSUPPORTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 策略配置解析单元测试。
 *
 * <p>重点覆盖两类：一是历史数据能不能读回来（小写算子、缺字段），二是非法配置能不能挡住
 * （字段不属于该类型、算子不支持、值类型对不上）。前者决定升级后老策略打不打得开，
 * 后者决定错配置会不会一路潜伏到货上错架。
 */
class WmsStrategyConfigParserTest {

    @Test
    void blankConfigShouldFallBackToEmptyRules() {
        WmsStrategyConfig config = WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK, null);

        // 没配等于不筛不排，是合法状态：策略骨架先建起来、配置慢慢补是常见节奏
        assertTrue(config.getConditions().isEmpty());
        assertTrue(config.getSorts().isEmpty());
    }

    @Test
    void lowerCaseOperatorAndOrderShouldStillParse() {
        // 库里存量数据就是这个形态，严格匹配大小写会让这些策略再也打不开
        String json = """
                {
                  "sorts": [{"field": "expiryDate", "order": "asc"}],
                  "conditions": [{"op": "eq", "field": "qualityStatus", "value": "QUALIFIED"}]
                }
                """;

        WmsStrategyConfig config = WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK, json);

        assertEquals(WmsStrategyOperatorEnum.EQ, config.getConditions().get(0).getOp());
        assertEquals(WmsStrategySortOrderEnum.ASC, config.getSorts().get(0).getOrder());
    }

    @Test
    void putawayConfigWithTypeOptionsShouldParse() {
        String json = """
                {
                  "sorts": [{"field": "aisleNo", "order": "asc"}, {"field": "levelNo", "order": "asc"}],
                  "conditions": [{"op": "in", "field": "matType", "value": ["RAW"]}],
                  "dimensionCheck": "FULL_DIM",
                  "allowMixedLot": true
                }
                """;

        WmsStrategyConfig config = WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PUTAWAY, json);

        assertEquals("FULL_DIM", config.getDimensionCheck());
        assertTrue(config.allowMixedLotOrFalse());
        assertEquals(2, config.getSorts().size());
    }

    @Test
    void unknownStrategyTypeShouldBeRejected() {
        assertServiceException(() -> WmsStrategyConfigParser.parse("NO_SUCH_TYPE", "{}"),
                STRATEGY_TYPE_UNSUPPORTED);
    }

    @Test
    void nonJsonShouldSayFormatIllegal() {
        // 提示语要分清不是 JSON 与结构对不上，否则会让人去检查根本没问题的括号
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK, "not json"),
                STRATEGY_CONFIG_INVALID, "JSON 格式非法");
    }

    @Test
    void jsonWithWrongShapeShouldSayStructureMismatch() {
        assertServiceException(
                () -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK, "{\"conditions\": \"oops\"}"),
                STRATEGY_CONFIG_INVALID, "配置结构与当前版本不匹配");
    }

    @Test
    void conditionFieldOutsideCatalogShouldBeRejected() {
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK,
                        "{\"conditions\":[{\"field\":\"noSuchField\",\"op\":\"EQ\",\"value\":\"X\"}]}"),
                STRATEGY_CONFIG_INVALID, "第 1 条筛选条件的字段 noSuchField 不在拣选策略的可筛字段里");
    }

    @Test
    void conditionFieldFromAnotherTypeShouldBeRejected() {
        // 拣选里出现库位承重，说明配的人搞错了策略类型，不能当成无害多余字段放过
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK,
                        "{\"conditions\":[{\"field\":\"maxWeight\",\"op\":\"GE\",\"value\":100}]}"),
                STRATEGY_CONFIG_INVALID, "第 1 条筛选条件的字段 maxWeight 不在拣选策略的可筛字段里");
    }

    @Test
    void sortFieldUsedAsConditionShouldBeRejected() {
        // aisleNo 在拣选里只能排序不能筛：能排不等于能筛，两份清单是分开登记的
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK,
                        "{\"conditions\":[{\"field\":\"aisleNo\",\"op\":\"EQ\",\"value\":\"A1\"}]}"),
                STRATEGY_CONFIG_INVALID, "第 1 条筛选条件的字段 aisleNo 不在拣选策略的可筛字段里");
    }

    @Test
    void unsupportedOperatorForFieldShouldBeRejected() {
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK,
                        "{\"conditions\":[{\"field\":\"qualityStatus\",\"op\":\"GT\",\"value\":\"QUALIFIED\"}]}"),
                STRATEGY_CONFIG_INVALID, "第 1 条筛选条件（质量状态）不支持大于这个算子");
    }

    @Test
    void unknownOperatorShouldBeRejected() {
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK,
                        "{\"conditions\":[{\"field\":\"qualityStatus\",\"op\":\"LIKE\",\"value\":\"Q\"}]}"),
                STRATEGY_CONFIG_INVALID, "第 1 条筛选条件的算子无法识别");
    }

    @Test
    void multiValueOperatorWithSingleValueShouldBeRejected() {
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK,
                        "{\"conditions\":[{\"field\":\"qualityStatus\",\"op\":\"IN\",\"value\":\"QUALIFIED\"}]}"),
                STRATEGY_CONFIG_INVALID, "第 1 条筛选条件用了属于，值要给一组而不是单个");
    }

    @Test
    void multiValueOperatorWithEmptyListShouldBeRejected() {
        // 空集合等于筛不出任何候选，跟没配完全是两个结果，不能当成没配放过
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK,
                        "{\"conditions\":[{\"field\":\"qualityStatus\",\"op\":\"IN\",\"value\":[]}]}"),
                STRATEGY_CONFIG_INVALID, "第 1 条筛选条件（质量状态）至少要选一个值");
    }

    @Test
    void singleValueOperatorWithListShouldBeRejected() {
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK,
                        "{\"conditions\":[{\"field\":\"qtyAvailable\",\"op\":\"GE\",\"value\":[1,2]}]}"),
                STRATEGY_CONFIG_INVALID, "第 1 条筛选条件用了大于等于，值只能给一个");
    }

    @Test
    void numberFieldWithStringValueShouldBeRejected() {
        // 放过字符串形式的数字，引擎按字符串比大小会得出 9 大于 10 这种结果
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK,
                        "{\"conditions\":[{\"field\":\"qtyAvailable\",\"op\":\"GE\",\"value\":\"50\"}]}"),
                STRATEGY_CONFIG_INVALID, "第 1 条筛选条件（可用量）的值应该是数字类型");
    }

    @Test
    void blankValueShouldBeRejected() {
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK,
                        "{\"conditions\":[{\"field\":\"qualityStatus\",\"op\":\"EQ\",\"value\":\"  \"}]}"),
                STRATEGY_CONFIG_INVALID, "第 1 条筛选条件（质量状态）没有填值");
    }

    @Test
    void sortFieldOutsideCatalogShouldBeRejected() {
        // expiryDate 是拣选的排序字段，上架按它排没有意义
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PUTAWAY,
                        "{\"sorts\":[{\"field\":\"expiryDate\",\"order\":\"ASC\"}]}"),
                STRATEGY_CONFIG_INVALID, "第 1 条排序规则的字段 expiryDate 不在上架策略的可排序字段里");
    }

    @Test
    void duplicateSortFieldShouldBeRejected() {
        // 同一字段排两次，第二次永远不生效，是明确的配置错误而不是无害冗余
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK,
                        "{\"sorts\":[{\"field\":\"expiryDate\",\"order\":\"ASC\"},"
                                + "{\"field\":\"expiryDate\",\"order\":\"DESC\"}]}"),
                STRATEGY_CONFIG_INVALID, "第 2 条排序规则的字段与前面的重复了");
    }

    @Test
    void unknownSortOrderShouldBeRejected() {
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PICK,
                        "{\"sorts\":[{\"field\":\"expiryDate\",\"order\":\"random\"}]}"),
                STRATEGY_CONFIG_INVALID, "第 1 条排序规则的排序方向无法识别");
    }

    @Test
    void replenishSortShouldBeRejectedBecauseTypeHasNoSortField() {
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.REPLENISH,
                        "{\"sorts\":[{\"field\":\"qtyAvailable\",\"order\":\"ASC\"}]}"),
                STRATEGY_CONFIG_INVALID, "第 1 条排序规则的字段 qtyAvailable 不在补货策略的可排序字段里");
    }

    @Test
    void unknownDimensionCheckModeShouldBeRejected() {
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PUTAWAY,
                        "{\"dimensionCheck\":\"WHATEVER\"}"),
                STRATEGY_CONFIG_INVALID, "尺寸校验模式 WHATEVER 无法识别");
    }

    @Test
    void blankDimensionCheckShouldBeNormalizedToNull() {
        WmsStrategyConfig config = WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.PUTAWAY,
                "{\"dimensionCheck\":\"  \"}");

        // 留空合法，表示按库位自己的尺寸校验模式判；归一成 null，免得库里同时出现 null 和空串
        assertNull(config.getDimensionCheck());
    }

    @Test
    void nonPositiveReplenishQtyShouldBeRejected() {
        assertServiceException(() -> WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.REPLENISH,
                        "{\"replenishQty\":0}"),
                STRATEGY_CONFIG_INVALID, "单次补货量要大于 0");
    }

    @Test
    void incompleteReplenishConfigShouldStillParse() {
        // 结构合法性与配齐了没是两件事：草稿必须存得下来，齐不齐在启用时才校验
        WmsStrategyConfig config = WmsStrategyConfigParser.parse(WmsStrategyTypeEnum.REPLENISH, "{}");

        assertTrue(config.getConditions().isEmpty());
        assertNull(config.getFromWhCode());
        assertNull(config.getReplenishQty());
    }

}
