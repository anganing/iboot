package com.iboot.pf.module.wms.strategy.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.iboot.pf.module.wms.strategy.field.WmsStrategyOperatorEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一条筛选条件：把候选集合按 {@code field op value} 过滤一遍。
 *
 * <p>{@code value} 用 {@code Object} 而不是分成几个具名字段：条件的值形态由字段的值类型决定
 * （枚举字段可能是单值也可能是列表、数字字段是数字、布尔字段是布尔），拆成
 * {@code stringValue}/{@code numberValue}/{@code listValue} 只会让 JSON 里多出一堆
 * null，也让前端要判断该往哪个字段塞。合法性由 {@link WmsStrategyConfigParser} 按字段目录校验。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WmsStrategyCondition {

    /** 字段名，取值范围见 {@code WmsStrategyFieldCatalog}。 */
    private String field;

    /** 算子。 */
    private WmsStrategyOperatorEnum op;

    /** 比较值：算子是 IN/NOT_IN 时为列表，否则为单值。 */
    private Object value;

}
