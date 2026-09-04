package com.iboot.pf.module.wms.strategy.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code wms_strategy.config_json} 的结构。
 *
 * <p>三种策略类型共用一个类，而不是按类型拆三个子类做多态反序列化：三类的骨架本来就一样——
 * 先用 {@code conditions} 筛候选，再用 {@code sorts} 决定先用哪个，剩下的才是各类专属选项。
 * 拆成三个类要配 {@code @JsonTypeInfo}，还得往 JSON 里塞一个类型判别字段，
 * 而类型本来就在 {@code wms_strategy.strategy_type} 上，没必要存两遍。
 *
 * <p>代价是"字段属于哪个类型"这件事编译期管不了：PUTAWAY 的配置里出现
 * {@code replenishQty} 语法上完全合法。这一层由 {@link WmsStrategyConfigParser} 按类型校验兜住，
 * 配错类型的字段会被拒绝保存，不会悄悄留在库里等引擎去踩。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WmsStrategyConfig {

    /** 筛选条件，多条之间是"且"的关系。为空表示不过滤，全部候选都参与。 */
    private List<WmsStrategyCondition> conditions = new ArrayList<>();

    /** 排序链，顺序即优先级。为空表示不排序，由引擎按稳定顺序取第一个。 */
    private List<WmsStrategySort> sorts = new ArrayList<>();

    /**
     * 上架专属：尺寸校验模式，取值见 {@code WmsDimensionCheckModeEnum}。
     *
     * <p>留空表示按库位自己的 {@code wms_loc.dimension_check_mode} 判。策略级给一个覆盖值是为了
     * 应对"这批货必须全尺寸校验"这类临时要求，不用去改一批库位主数据。
     */
    private String dimensionCheck;

    /**
     * 上架专属：是否允许混批上架，即把新批次放到已有其它批次的库位上。
     *
     * <p>缺省 {@code false}：混批之后先进先出就无从谈起了，要开得是明确的选择。
     * 它与库区自身的 {@code allow_mixed_lot} 是两件事——库区说"这里允不允许混"，
     * 策略说"这次上架要不要混"，两个都放行才会混。
     */
    private Boolean allowMixedLot = Boolean.FALSE;

    /** 补货专属：来源仓库编码。存编码而不是主键，策略配置才能在环境之间搬。 */
    private String fromWhCode;

    /** 补货专属：单次补货量。 */
    private BigDecimal replenishQty;

    /** 是否允许混批上架，未配置按不允许。 */
    public boolean allowMixedLotOrFalse() {
        return Boolean.TRUE.equals(allowMixedLot);
    }

}
