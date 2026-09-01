package com.iboot.pf.module.aps.solver.config;

/**
 * 单条约束的权重配置。
 *
 * <p>硬软分开而不是只给一个数：同一条约束在不同策略下可能既想当硬约束（绝对不许违反），
 * 也想降级成软约束（尽量满足）。给 hard 一个非零值就是硬约束，给 soft 就是软约束。
 *
 * @param hard 硬约束权重，通常为负或零
 * @param soft 软约束权重
 */
public record ApsConstraintWeight(Long hard, Long soft) {

    /** 硬权重，缺省 0。 */
    public long hardOrZero() {
        return hard == null ? 0L : hard;
    }

    /** 软权重，缺省 0。 */
    public long softOrZero() {
        return soft == null ? 0L : soft;
    }

    /** 纯硬约束权重。 */
    public static ApsConstraintWeight ofHard(long hard) {
        return new ApsConstraintWeight(hard, 0L);
    }

    /** 纯软约束权重。 */
    public static ApsConstraintWeight ofSoft(long soft) {
        return new ApsConstraintWeight(0L, soft);
    }

}
