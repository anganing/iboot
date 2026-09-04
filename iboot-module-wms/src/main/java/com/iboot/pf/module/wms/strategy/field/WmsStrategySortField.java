package com.iboot.pf.module.wms.strategy.field;

/**
 * 可用于策略排序的字段。
 *
 * <p>不带值类型与算子：排序只需要"按哪个字段、升还是降"，多带的元数据只会让前端多问一句
 * "排序字段为什么有算子"。
 *
 * @param field 字段名
 * @param displayName 中文名
 * @param description 业务说明：按它排会得到什么效果
 */
public record WmsStrategySortField(String field, String displayName, String description) {
}
