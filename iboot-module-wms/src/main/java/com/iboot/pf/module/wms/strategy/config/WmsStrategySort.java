package com.iboot.pf.module.wms.strategy.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一条排序规则。
 *
 * <p>多条排序按列表顺序构成排序链：前一条比不出高下才轮到后一条。顺序即优先级，
 * 所以前端必须让人能上下移动，不能只给增删。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WmsStrategySort {

    /** 字段名，取值范围见 {@code WmsStrategyFieldCatalog}。 */
    private String field;

    /** 排序方向。 */
    private WmsStrategySortOrderEnum order;

}
