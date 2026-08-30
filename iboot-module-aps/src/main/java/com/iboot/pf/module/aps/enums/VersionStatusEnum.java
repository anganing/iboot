package com.iboot.pf.module.aps.enums;

import java.util.Set;

/** 排程版本状态机。 */
public enum VersionStatusEnum {
    SOLVING, SOLVED, PENDING_CONFIRM, CONFIRMED, PUBLISHED, REPLACED, FAILED;

    /** 判断当前状态是否允许迁移到目标状态。 */
    public boolean canTransitTo(VersionStatusEnum target) {
        return switch (this) {
            case SOLVING, SOLVED, PENDING_CONFIRM -> target == CONFIRMED;
            case CONFIRMED -> target == PUBLISHED;
            case PUBLISHED -> target == REPLACED;
            default -> false;
        };
    }

    /** 已发布后不可修改排程结果的状态。 */
    public boolean isImmutable() {
        return Set.of(PUBLISHED, REPLACED).contains(this);
    }
}
