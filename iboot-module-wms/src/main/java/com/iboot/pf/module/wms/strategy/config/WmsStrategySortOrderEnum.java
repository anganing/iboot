package com.iboot.pf.module.wms.strategy.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 排序方向。
 *
 * <p>{@link #of} 大小写都认，理由同 {@code WmsStrategyOperatorEnum}：历史 {@code config_json}
 * 里存的是小写（{@code "order": "asc"}）。
 */
@Getter
@AllArgsConstructor
public enum WmsStrategySortOrderEnum {

    /** 升序。 */
    ASC("升序"),
    /** 降序。 */
    DESC("降序");

    private final String name;

    /** 按名称解析，大小写不敏感；无法识别时返回 {@code null}，由解析器给出可读提示。 */
    @JsonCreator
    public static WmsStrategySortOrderEnum of(String order) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(order))
                .findFirst().orElse(null);
    }

}
