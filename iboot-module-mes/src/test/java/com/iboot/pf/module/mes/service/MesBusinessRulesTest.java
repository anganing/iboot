package com.iboot.pf.module.mes.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MesBusinessRulesTest {

    @Test
    void shouldFollowOpTaskStateMachine() {
        assertEquals("RUNNING", MesBusinessRules.nextOpTaskStatus("PENDING", "start"));
        assertEquals("PAUSED", MesBusinessRules.nextOpTaskStatus("RUNNING", "pause"));
        assertEquals("DONE", MesBusinessRules.nextOpTaskStatus("PAUSED", "finish"));
        assertThrows(RuntimeException.class, () -> MesBusinessRules.nextOpTaskStatus("DONE", "start"));
    }

    @Test
    void shouldValidateReportAndQualityQuantities() {
        MesBusinessRules.validateFinishQty(dec("10"), dec("9"), dec("1"));
        assertThrows(RuntimeException.class, () -> MesBusinessRules.validateFinishQty(dec("10"), dec("8"), dec("1")));
        MesBusinessRules.validateQualityQty(dec("5"), dec("4"), dec("1"));
        assertThrows(RuntimeException.class, () -> MesBusinessRules.validateQualityQty(dec("5"), dec("4"), dec("2")));
    }

    @Test
    void shouldCalculateRequiredQtyWithScrapRate() {
        assertEquals(dec("11.111111"), MesBusinessRules.requiredQty(dec("10"), dec("1"), dec("0.1")));
    }

    private static BigDecimal dec(String value) {
        return new BigDecimal(value);
    }
}
