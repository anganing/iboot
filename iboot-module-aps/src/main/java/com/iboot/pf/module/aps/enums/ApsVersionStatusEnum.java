package com.iboot.pf.module.aps.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

/**
 * 排程版本状态机。与字典 {@code aps_version_status} 一致。
 *
 * <p>允许的迁移：求解中 -> 已求解 / 求解失败，求解中/已求解/待确认 -> 已确认，
 * 已确认 -> 已发布，已发布 -> 已被替换。
 * 已发布与已被替换是终态，排程结果不允许再改，否则车间已领到的计划会和库里的数据对不上。
 *
 * <p>求解失败是终态：失败的版本不允许确认发布，重排要生成新版本。保留失败记录而不是删掉，
 * 是为了让“这个场景什么时候排不出来过”可追溯。
 */
@Getter
@AllArgsConstructor
public enum ApsVersionStatusEnum implements ArrayValuable<String> {

    /** 求解中。 */
    SOLVING("求解中"),
    /** 已求解。 */
    SOLVED("已求解"),
    /** 待确认。 */
    PENDING_CONFIRM("待确认"),
    /** 已确认。 */
    CONFIRMED("已确认"),
    /** 已发布。 */
    PUBLISHED("已发布"),
    /** 已被替换。 */
    REPLACED("已被替换"),
    /** 求解失败。 */
    FAILED("求解失败");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private static final Set<ApsVersionStatusEnum> IMMUTABLE_STATUSES = Set.of(PUBLISHED, REPLACED);

    /**
     * 曾经生效过的状态，最新在前。
     *
     * <p>车间与 PMC 的"排程结果"页只能查这两种：已发布是当前生效的计划，已被替换是上一版下过车间的计划。
     * 求解中、求解失败、待确认、已确认从没到过车间，把它们混进去会让人误以为那也是一份计划。
     */
    private static final Set<String> EFFECTIVE_STATUSES = Set.of(PUBLISHED.name(), REPLACED.name());

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 判断当前状态是否允许迁移到目标状态。 */
    public boolean canTransitTo(ApsVersionStatusEnum target) {
        return switch (this) {
            // 求解完成后既可能有解（SOLVED）也可能无解（FAILED），人工确认前允许直接确认已求解的版本
            case SOLVING -> target == SOLVED || target == FAILED || target == CONFIRMED;
            case SOLVED, PENDING_CONFIRM -> target == CONFIRMED;
            case CONFIRMED -> target == PUBLISHED;
            case PUBLISHED -> target == REPLACED;
            default -> false;
        };
    }

    /** 排程结果是否已冻结：已发布与已被替换的版本，任务、依赖、缺料、负荷都不允许再改。 */
    public boolean isImmutable() {
        return IMMUTABLE_STATUSES.contains(this);
    }

    /** 是否已发布。 */
    public static boolean isPublished(String status) {
        return ObjUtil.equal(PUBLISHED.name(), status);
    }

    /** 是否已确认：只有已确认的版本才允许发布或作为替换的新版本。 */
    public static boolean isConfirmed(String status) {
        return ObjUtil.equal(CONFIRMED.name(), status);
    }

    /**
     * 判断状态是否属于曾经生效过。
     *
     * <p>空值与白名单外的取值一律返回 {@code false}，由调用方决定回落策略。
     * 先挡空值是必须的：{@code Set.of} 是不可变集合，{@code contains(null)} 会直接抛 NPE。
     */
    public static boolean isEffective(String status) {
        return status != null && EFFECTIVE_STATUSES.contains(status);
    }

}
