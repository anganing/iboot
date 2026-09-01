package com.iboot.pf.module.aps.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 排程版本状态机的单元测试：只允许文档里写明的迁移。 */
class ApsVersionStatusEnumTest {

    @Test
    void shouldAllowDocumentedTransitions() {
        assertTrue(ApsVersionStatusEnum.SOLVING.canTransitTo(ApsVersionStatusEnum.CONFIRMED));
        assertTrue(ApsVersionStatusEnum.SOLVED.canTransitTo(ApsVersionStatusEnum.CONFIRMED));
        assertTrue(ApsVersionStatusEnum.PENDING_CONFIRM.canTransitTo(ApsVersionStatusEnum.CONFIRMED));
        assertTrue(ApsVersionStatusEnum.CONFIRMED.canTransitTo(ApsVersionStatusEnum.PUBLISHED));
        assertTrue(ApsVersionStatusEnum.PUBLISHED.canTransitTo(ApsVersionStatusEnum.REPLACED));
    }

    @Test
    void solvingShouldAllowSolveOutcomes() {
        // 求解结束有两种结局，都由 SOLVING 直接迁出
        assertTrue(ApsVersionStatusEnum.SOLVING.canTransitTo(ApsVersionStatusEnum.SOLVED));
        assertTrue(ApsVersionStatusEnum.SOLVING.canTransitTo(ApsVersionStatusEnum.FAILED));
        // 求解失败是终态：重排要生成新版本，不允许把失败的版本救回来确认发布
        assertFalse(ApsVersionStatusEnum.FAILED.canTransitTo(ApsVersionStatusEnum.SOLVED));
        assertFalse(ApsVersionStatusEnum.SOLVED.canTransitTo(ApsVersionStatusEnum.FAILED));
    }

    @Test
    void shouldRejectReverseAndSkippedTransitions() {
        // 跳过确认直接发布会让未经确认的计划流到车间
        assertFalse(ApsVersionStatusEnum.SOLVING.canTransitTo(ApsVersionStatusEnum.PUBLISHED));
        assertFalse(ApsVersionStatusEnum.PUBLISHED.canTransitTo(ApsVersionStatusEnum.CONFIRMED));
        assertFalse(ApsVersionStatusEnum.REPLACED.canTransitTo(ApsVersionStatusEnum.PUBLISHED));
        assertFalse(ApsVersionStatusEnum.FAILED.canTransitTo(ApsVersionStatusEnum.CONFIRMED));
    }

    @Test
    void publishedAndReplacedShouldBeImmutable() {
        assertTrue(ApsVersionStatusEnum.PUBLISHED.isImmutable());
        assertTrue(ApsVersionStatusEnum.REPLACED.isImmutable());
        assertFalse(ApsVersionStatusEnum.CONFIRMED.isImmutable());
        assertFalse(ApsVersionStatusEnum.SOLVING.isImmutable());
    }

    @Test
    void statusHelpersShouldMatchNames() {
        assertTrue(ApsVersionStatusEnum.isPublished(ApsVersionStatusEnum.PUBLISHED.name()));
        assertTrue(ApsVersionStatusEnum.isConfirmed(ApsVersionStatusEnum.CONFIRMED.name()));
        assertFalse(ApsVersionStatusEnum.isPublished(ApsVersionStatusEnum.CONFIRMED.name()));
        assertFalse(ApsVersionStatusEnum.isConfirmed(null));
    }

}
