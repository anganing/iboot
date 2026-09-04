package com.iboot.pf.module.wms.strategy.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 条件算子。
 *
 * <p>{@code multiValue} 标出哪些算子收列表：前端据此决定值控件是单选还是多选，后端据此校验
 * value 的形态。配了 {@code IN} 却只给一个单值会被拒，反过来也一样。
 *
 * <p>{@link #of} 大小写都认，是为了兼容历史 {@code config_json}——里面存的是小写
 * （{@code "op": "eq"}）。严格匹配会让那些策略再也打不开，只能靠 DML 去修。
 * 序列化统一输出大写，打开保存一次就统一了。
 */
@Getter
@AllArgsConstructor
public enum WmsStrategyOperatorEnum {

    /** 等于。 */
    EQ("等于", false),
    /** 不等于。 */
    NE("不等于", false),
    /** 属于。 */
    IN("属于", true),
    /** 不属于。 */
    NOT_IN("不属于", true),
    /** 大于。 */
    GT("大于", false),
    /** 大于等于。 */
    GE("大于等于", false),
    /** 小于。 */
    LT("小于", false),
    /** 小于等于。 */
    LE("小于等于", false);

    private final String name;

    /** 值是否为列表。 */
    private final boolean multiValue;

    /**
     * 按名称解析，大小写不敏感。
     *
     * <p>无法识别时返回 {@code null} 而不是抛异常：Jackson 抛出来的是英文的
     * InvalidFormatException，交给 {@code WmsStrategyConfigParser} 报"未知算子"更有用。
     */
    @JsonCreator
    public static WmsStrategyOperatorEnum of(String op) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(op))
                .findFirst().orElse(null);
    }

}
