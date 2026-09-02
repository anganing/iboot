package com.iboot.pf.module.aps.solver.constraint;

import com.iboot.pf.module.aps.solver.ApsConstraintWeightResolver;
import com.iboot.pf.module.aps.solver.config.ApsConstraintWeight;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 约束目录与约束名、默认权重的一致性。
 *
 * <p>这三处是同一件事的三种表述：{@code ApsConstraintNames} 说"有哪些约束"，
 * {@code ApsConstraintCatalog} 说"这条约束对人怎么讲"，{@code ApsConstraintWeightResolver} 说"默认多重"。
 * 加约束时漏改任意一处，页面上就会少一条可调项、或者少一句说明、或者默认权重取不到，
 * 这些都不会让编译失败，所以要靠这个测试挡住。
 */
class ApsConstraintCatalogTest {

    @Test
    void catalogShouldCoverAllConstraintNamesInSameOrder() {
        List<String> cataloged = ApsConstraintCatalog.definitions().stream()
                .map(ApsConstraintDefinition::constraintName)
                .toList();
        // 顺序也要一致：目录顺序就是权重配置页面上的展示顺序
        assertEquals(ApsConstraintNames.ALL, cataloged);
    }

    @Test
    void everyDefinitionShouldBeReadableForPlanner() {
        for (ApsConstraintDefinition definition : ApsConstraintCatalog.definitions()) {
            assertNotNull(definition.category(), definition.constraintName() + " 缺类别");
            assertFalse(definition.displayName().isBlank(), definition.constraintName() + " 缺中文名");
            // 说明是计划员调权重时唯一的决策依据，不许留空
            assertFalse(definition.description().isBlank(), definition.constraintName() + " 缺业务说明");
        }
    }

    @Test
    void declaredCategoryShouldMatchDefaultWeightSide() {
        for (ApsConstraintDefinition definition : ApsConstraintCatalog.definitions()) {
            ApsConstraintWeight weight = ApsConstraintWeightResolver.defaultWeights()
                    .get(definition.constraintName());
            assertNotNull(weight, definition.constraintName() + " 没有默认权重");
            // 声明成硬约束却给了软权重，说明两处对这条约束的理解已经不一致了
            if (definition.category() == ApsConstraintCategoryEnum.HARD) {
                assertTrue(weight.hardOrZero() > 0, definition.constraintName() + " 声明为硬约束，默认硬权重却为 0");
                assertEquals(0L, weight.softOrZero(), definition.constraintName() + " 声明为硬约束，却有默认软权重");
            } else {
                assertTrue(weight.softOrZero() > 0, definition.constraintName() + " 声明为软约束，默认软权重却为 0");
                assertEquals(0L, weight.hardOrZero(), definition.constraintName() + " 声明为软约束，却有默认硬权重");
            }
        }
    }

    @Test
    void displayNameShouldFallBackToRawKey() {
        // 历史快照里可能存在已经改过名的约束，返回原始键比返回空字符串更有追溯价值
        assertEquals("legacyConstraint", ApsConstraintCatalog.displayName("legacyConstraint"));
    }

}
