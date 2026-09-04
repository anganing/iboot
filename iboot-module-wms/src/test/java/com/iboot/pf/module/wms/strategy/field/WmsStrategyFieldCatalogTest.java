package com.iboot.pf.module.wms.strategy.field;

import com.iboot.pf.module.wms.enums.WmsStrategyTypeEnum;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 字段目录的自洽性。
 *
 * <p>目录同时喂三个消费方：前端的字段下拉、前端的值控件、后端保存时的校验。任一条登记不全都不会
 * 让编译失败，只会在页面上表现成"下拉里有个没名字的字段"或者"枚举字段渲染成了输入框"，
 * 所以要靠这个测试挡住。
 */
class WmsStrategyFieldCatalogTest {

    @Test
    void everyTypeShouldHaveConditionFields() {
        for (WmsStrategyTypeEnum type : WmsStrategyTypeEnum.values()) {
            assertFalse(WmsStrategyFieldCatalog.conditionFields(type).isEmpty(),
                    type.getName() + " 没有可筛字段，页面打开会是一片空白");
            assertFalse(WmsStrategyFieldCatalog.candidateTarget(type).isBlank(),
                    type.getName() + " 没说清条件作用在哪批数据上");
        }
    }

    @Test
    void everyConditionFieldShouldBeReadableAndUsable() {
        for (WmsStrategyTypeEnum type : WmsStrategyTypeEnum.values()) {
            for (WmsStrategyConditionField field : WmsStrategyFieldCatalog.conditionFields(type)) {
                String where = type.getName() + " 的 " + field.field();
                assertFalse(field.displayName().isBlank(), where + " 缺中文名");
                // 说明是仓管配策略时唯一的决策依据，不许留空
                assertFalse(field.description().isBlank(), where + " 缺业务说明");
                assertNotNull(field.valueType(), where + " 缺值类型");
                assertFalse(field.operators().isEmpty(), where + " 没有可用算子，选出来也没法填条件");
            }
        }
    }

    @Test
    void enumFieldShouldCarryDictTypeAndOthersShouldNot() {
        for (WmsStrategyTypeEnum type : WmsStrategyTypeEnum.values()) {
            for (WmsStrategyConditionField field : WmsStrategyFieldCatalog.conditionFields(type)) {
                String where = type.getName() + " 的 " + field.field();
                if (field.valueType() == WmsStrategyValueTypeEnum.ENUM) {
                    // 枚举字段没有字典编码，前端只能退化成让人手打枚举值
                    assertNotNull(field.dictType(), where + " 是枚举字段却没给字典编码");
                } else {
                    assertNull(field.dictType(), where + " 不是枚举字段却给了字典编码");
                }
            }
        }
    }

    @Test
    void multiValueOperatorShouldOnlyAppearOnEnumField() {
        for (WmsStrategyTypeEnum type : WmsStrategyTypeEnum.values()) {
            for (WmsStrategyConditionField field : WmsStrategyFieldCatalog.conditionFields(type)) {
                if (field.valueType() == WmsStrategyValueTypeEnum.ENUM) {
                    continue;
                }
                // 非枚举字段配上 IN，前端得为它额外做一套多值输入，值域又是无限的，没有实际用法
                assertTrue(field.operators().stream().noneMatch(WmsStrategyOperatorEnum::isMultiValue),
                        type.getName() + " 的 " + field.field() + " 不是枚举字段，不该支持多值算子");
            }
        }
    }

    @Test
    void fieldNamesShouldNotRepeatWithinOneType() {
        for (WmsStrategyTypeEnum type : WmsStrategyTypeEnum.values()) {
            Set<String> conditionNames = new HashSet<>();
            WmsStrategyFieldCatalog.conditionFields(type).forEach(field ->
                    assertTrue(conditionNames.add(field.field()),
                            type.getName() + " 的可筛字段 " + field.field() + " 登记了两次"));
            Set<String> sortNames = new HashSet<>();
            WmsStrategyFieldCatalog.sortFields(type).forEach(field ->
                    assertTrue(sortNames.add(field.field()),
                            type.getName() + " 的可排序字段 " + field.field() + " 登记了两次"));
        }
    }

    @Test
    void everySortFieldShouldBeReadable() {
        for (WmsStrategyTypeEnum type : WmsStrategyTypeEnum.values()) {
            for (WmsStrategySortField field : WmsStrategyFieldCatalog.sortFields(type)) {
                String where = type.getName() + " 的排序字段 " + field.field();
                assertFalse(field.displayName().isBlank(), where + " 缺中文名");
                assertFalse(field.description().isBlank(), where + " 缺业务说明");
            }
        }
    }

    @Test
    void replenishShouldHaveNoSortFieldByDesign() {
        // 补货只判要不要补，没有按什么顺序补这回事。这条断言把这个决定钉住，
        // 免得后来有人以为是漏登记了顺手补上一堆
        assertTrue(WmsStrategyFieldCatalog.sortFields(WmsStrategyTypeEnum.REPLENISH).isEmpty());
    }

    @Test
    void fieldsUsedByExistingStrategiesShouldStayInCatalog() {
        // 库里已配好的策略用到的就是这些字段，删字段或改名等于把它们配废。
        // 真要改，改的同时得把这份清单和库里的数据一起处理，不能只改目录
        assertConditionFields(WmsStrategyTypeEnum.PUTAWAY, List.of("matType", "zoneType"));
        assertSortFields(WmsStrategyTypeEnum.PUTAWAY, List.of("aisleNo", "levelNo", "locCode"));
        assertConditionFields(WmsStrategyTypeEnum.PICK, List.of("qualityStatus"));
        assertSortFields(WmsStrategyTypeEnum.PICK, List.of("expiryDate", "produceDate", "createTime"));
        assertConditionFields(WmsStrategyTypeEnum.REPLENISH, List.of("qtyAvailable"));
    }

    private static void assertConditionFields(WmsStrategyTypeEnum type, List<String> fields) {
        fields.forEach(field -> assertNotNull(WmsStrategyFieldCatalog.conditionField(type, field),
                type.getName() + " 丢了存量策略在用的可筛字段 " + field));
    }

    private static void assertSortFields(WmsStrategyTypeEnum type, List<String> fields) {
        fields.forEach(field -> assertTrue(WmsStrategyFieldCatalog.supportsSortField(type, field),
                type.getName() + " 丢了存量策略在用的可排序字段 " + field));
    }

}
