package com.iboot.pf.module.wms.strategy.field;

import java.util.List;

/**
 * 可用于策略条件的字段。
 *
 * @param field 字段名，与策略引擎取候选值时用的键一致
 * @param displayName 中文名，页面展示用
 * @param valueType 值类型，决定前端控件与后端校验
 * @param dictType 枚举字段的字典编码，让前端拿字典渲染下拉；非枚举字段为 {@code null}
 * @param operators 该字段支持的算子，顺序即页面下拉顺序
 * @param description 业务说明：这个条件筛掉的是什么
 */
public record WmsStrategyConditionField(String field, String displayName,
                                        WmsStrategyValueTypeEnum valueType, String dictType,
                                        List<WmsStrategyOperatorEnum> operators, String description) {
}
