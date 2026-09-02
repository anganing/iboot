package com.iboot.pf.module.aps.solver.constraint;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 约束类别。
 *
 * <p>硬约束是"不许违反"，违反即不可行；软约束是"尽量优化"，违反只是扣分。
 * 这个类别只描述约束的设计意图，实际生效仍由权重决定：权重全为 0 的约束等于不参与求解。
 */
@Getter
@AllArgsConstructor
public enum ApsConstraintCategoryEnum {

    /** 硬约束。 */
    HARD("硬约束"),
    /** 软约束。 */
    SOFT("软约束");

    private final String name;

}
