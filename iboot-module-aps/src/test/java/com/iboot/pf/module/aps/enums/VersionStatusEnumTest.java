package com.iboot.pf.module.aps.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VersionStatusEnumTest {

    @Test
    void shouldAllowDocumentedTransitions() {
        assertTrue(VersionStatusEnum.SOLVING.canTransitTo(VersionStatusEnum.CONFIRMED));
        assertTrue(VersionStatusEnum.CONFIRMED.canTransitTo(VersionStatusEnum.PUBLISHED));
        assertTrue(VersionStatusEnum.PUBLISHED.canTransitTo(VersionStatusEnum.REPLACED));
    }

    @Test
    void shouldRejectReverseAndSkippedTransitions() {
        assertFalse(VersionStatusEnum.SOLVING.canTransitTo(VersionStatusEnum.PUBLISHED));
        assertFalse(VersionStatusEnum.PUBLISHED.canTransitTo(VersionStatusEnum.CONFIRMED));
        assertFalse(VersionStatusEnum.REPLACED.canTransitTo(VersionStatusEnum.PUBLISHED));
    }

    @Test
    void publishedAndReplacedShouldBeImmutable() {
        assertTrue(VersionStatusEnum.PUBLISHED.isImmutable());
        assertTrue(VersionStatusEnum.REPLACED.isImmutable());
        assertFalse(VersionStatusEnum.CONFIRMED.isImmutable());
    }
}
