package com.iboot.pf.module.aps.enums;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 缺料状态推导的单元测试。 */
class ApsShortageStatusEnumTest {

    @Test
    void shouldBeResolvedWhenNoShortage() {
        assertEquals(ApsShortageStatusEnum.RESOLVED,
                ApsShortageStatusEnum.of(BigDecimal.ZERO, new BigDecimal("100")));
    }

    @Test
    void shouldBePartialWhenSomeStockAvailable() {
        assertEquals(ApsShortageStatusEnum.PARTIAL,
                ApsShortageStatusEnum.of(new BigDecimal("40"), new BigDecimal("60")));
    }

    @Test
    void shouldBeOpenWhenNothingAvailable() {
        assertEquals(ApsShortageStatusEnum.OPEN,
                ApsShortageStatusEnum.of(new BigDecimal("100"), BigDecimal.ZERO));
    }

}
