package com.iboot.pf.module.wms.strategy.field;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 条件字段的值类型。
 *
 * <p>它决定前端拿什么控件收值：枚举给字典下拉、数字给 InputNumber、布尔给是否、文本给输入框。
 * 后端也用它校验条件里的 value，类型对不上直接拒绝保存——策略引擎拿到字符串去比大小，
 * 不会报错，只会静默筛出一批错的库位。
 */
@Getter
@AllArgsConstructor
public enum WmsStrategyValueTypeEnum {

    /** 枚举值，取值范围由字段的 dictType 指定。 */
    ENUM("枚举"),
    /** 数字。 */
    NUMBER("数字"),
    /** 布尔。 */
    BOOLEAN("布尔"),
    /** 文本。 */
    TEXT("文本");

    private final String name;

}
