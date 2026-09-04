package com.iboot.pf.module.wms.strategy.field;

import com.iboot.pf.module.wms.enums.WmsDictTypeConstants;
import com.iboot.pf.module.wms.enums.WmsStrategyTypeEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.module.wms.strategy.field.WmsStrategyOperatorEnum.EQ;
import static com.iboot.pf.module.wms.strategy.field.WmsStrategyOperatorEnum.GE;
import static com.iboot.pf.module.wms.strategy.field.WmsStrategyOperatorEnum.GT;
import static com.iboot.pf.module.wms.strategy.field.WmsStrategyOperatorEnum.IN;
import static com.iboot.pf.module.wms.strategy.field.WmsStrategyOperatorEnum.LE;
import static com.iboot.pf.module.wms.strategy.field.WmsStrategyOperatorEnum.LT;
import static com.iboot.pf.module.wms.strategy.field.WmsStrategyOperatorEnum.NE;
import static com.iboot.pf.module.wms.strategy.field.WmsStrategyOperatorEnum.NOT_IN;
import static com.iboot.pf.module.wms.strategy.field.WmsStrategyValueTypeEnum.BOOLEAN;
import static com.iboot.pf.module.wms.strategy.field.WmsStrategyValueTypeEnum.ENUM;
import static com.iboot.pf.module.wms.strategy.field.WmsStrategyValueTypeEnum.NUMBER;
import static com.iboot.pf.module.wms.strategy.field.WmsStrategyValueTypeEnum.TEXT;

/**
 * 策略字段目录：每种策略类型能筛哪些字段、能按哪些字段排序。
 *
 * <p>它是"策略配置里允许出现哪些字段"的唯一事实源。前端的字段下拉、算子下拉、值控件全部由它
 * 驱动，保存时 {@code WmsStrategyConfigParser} 也拿它校验——同一份清单同时管渲染和校验，
 * 页面上能选出来的就一定存得进去，存得进去的将来引擎就一定认。前端另写一份的话，加字段要改两处，
 * 迟早出现"页面上能选、后端不认"或者反过来。
 *
 * <p>字段说明刻意写"配了它会筛掉什么、按它排会得到什么效果"，而不是"这个字段是什么"：
 * 配策略的人是仓管，他要的是决策依据，不是数据字典。
 *
 * <p>字段名沿用库里已有 {@code config_json} 的 camelCase 写法，新加的字段也照这个来：
 * 改名等于把已配好的策略全废掉。
 */
public final class WmsStrategyFieldCatalog {

    /** 枚举字段的算子：值域是有限集合，只有"是不是这几个"有意义。 */
    private static final List<WmsStrategyOperatorEnum> ENUM_OPS = List.of(EQ, IN, NOT_IN);

    /** 数字字段的算子：只给比较不给相等，按承重、可用量卡阈值才是实际用法。 */
    private static final List<WmsStrategyOperatorEnum> NUMBER_OPS = List.of(GE, GT, LE, LT);

    /** 布尔字段的算子。 */
    private static final List<WmsStrategyOperatorEnum> BOOLEAN_OPS = List.of(EQ);

    /** 文本字段的算子。 */
    private static final List<WmsStrategyOperatorEnum> TEXT_OPS = List.of(EQ, NE);

    private static final Map<WmsStrategyTypeEnum, List<WmsStrategyConditionField>> CONDITION_FIELDS =
            buildConditionFields();

    private static final Map<WmsStrategyTypeEnum, List<WmsStrategySortField>> SORT_FIELDS = buildSortFields();

    private static final Map<WmsStrategyTypeEnum, String> CANDIDATE_TARGETS = buildCandidateTargets();

    private WmsStrategyFieldCatalog() {
    }

    /** 某类型的全部条件字段，顺序即页面展示顺序；未登记的类型返回空列表。 */
    public static List<WmsStrategyConditionField> conditionFields(WmsStrategyTypeEnum strategyType) {
        return CONDITION_FIELDS.getOrDefault(strategyType, List.of());
    }

    /**
     * 某类型的全部排序字段，顺序即页面展示顺序。
     *
     * <p>补货策略返回空列表：它只判"要不要补"，没有"按什么顺序补"这回事。前端拿到空列表就不渲染
     * 排序区，而不是给出一个选不出东西的下拉。
     */
    public static List<WmsStrategySortField> sortFields(WmsStrategyTypeEnum strategyType) {
        return SORT_FIELDS.getOrDefault(strategyType, List.of());
    }

    /** 该类型的条件作用在哪批数据上，用于页面上把话说清。 */
    public static String candidateTarget(WmsStrategyTypeEnum strategyType) {
        return CANDIDATE_TARGETS.getOrDefault(strategyType, "");
    }

    /** 单个条件字段的定义；该类型不支持这个字段时返回 {@code null}。 */
    public static WmsStrategyConditionField conditionField(WmsStrategyTypeEnum strategyType, String field) {
        return conditionFields(strategyType).stream()
                .filter(item -> item.field().equals(field))
                .findFirst().orElse(null);
    }

    /** 该类型是否支持按这个字段排序。 */
    public static boolean supportsSortField(WmsStrategyTypeEnum strategyType, String field) {
        return sortFields(strategyType).stream().anyMatch(item -> item.field().equals(field));
    }

    private static Map<WmsStrategyTypeEnum, String> buildCandidateTargets() {
        Map<WmsStrategyTypeEnum, String> targets = new EnumMap<>(WmsStrategyTypeEnum.class);
        targets.put(WmsStrategyTypeEnum.PUTAWAY, "库位（含所属库区）");
        targets.put(WmsStrategyTypeEnum.PICK, "库存余额（含批次与库位）");
        targets.put(WmsStrategyTypeEnum.REPLENISH, "线边库存余额");
        return Collections.unmodifiableMap(targets);
    }

    private static Map<WmsStrategyTypeEnum, List<WmsStrategyConditionField>> buildConditionFields() {
        Map<WmsStrategyTypeEnum, List<WmsStrategyConditionField>> fields =
                new EnumMap<>(WmsStrategyTypeEnum.class);
        fields.put(WmsStrategyTypeEnum.PUTAWAY, putawayConditionFields());
        fields.put(WmsStrategyTypeEnum.PICK, pickConditionFields());
        fields.put(WmsStrategyTypeEnum.REPLENISH, replenishConditionFields());
        return Collections.unmodifiableMap(fields);
    }

    private static Map<WmsStrategyTypeEnum, List<WmsStrategySortField>> buildSortFields() {
        Map<WmsStrategyTypeEnum, List<WmsStrategySortField>> fields = new EnumMap<>(WmsStrategyTypeEnum.class);
        fields.put(WmsStrategyTypeEnum.PUTAWAY, putawaySortFields());
        fields.put(WmsStrategyTypeEnum.PICK, pickSortFields());
        // 显式登记空列表而不是不登记，表明补货没有排序维度是想清楚的结果
        fields.put(WmsStrategyTypeEnum.REPLENISH, List.of());
        return Collections.unmodifiableMap(fields);
    }

    private static List<WmsStrategyConditionField> putawayConditionFields() {
        List<WmsStrategyConditionField> fields = new ArrayList<>();
        fields.add(new WmsStrategyConditionField("matType", "物料类型", ENUM,
                WmsDictTypeConstants.MAT_TYPE, ENUM_OPS,
                "按待上架物料的类型决定这条策略管不管。配「原料只走原料区」这类规则时，先用它把策略限定到原料。"));
        fields.add(new WmsStrategyConditionField("zoneType", "库区类型", ENUM,
                WmsDictTypeConstants.ZONE_TYPE, ENUM_OPS,
                "限定候选库位所在库区的类型，最常用的一条。成品上到原料区、货上到发货区，靠它挡住。"));
        fields.add(new WmsStrategyConditionField("locType", "库位类型", ENUM,
                WmsDictTypeConstants.LOC_TYPE, ENUM_OPS,
                "限定库位形态。立库货格靠堆垛机、平库区域靠叉车，两者的上架动作不同，"
                        + "混在一条策略里会排出设备到不了的位置。"));
        fields.add(new WmsStrategyConditionField("locStatus", "库位状态", ENUM,
                WmsDictTypeConstants.LOC_STATUS, ENUM_OPS,
                "限定库位当前状态。只上空库位就配等于空闲；允许并板时把占用也放进来。"
                        + "不配这条，锁定和停用的库位也会进候选。"));
        fields.add(new WmsStrategyConditionField("tempRange", "温区要求", TEXT, null, TEXT_OPS,
                "按库区温区筛。冷链物料上到常温区，等到货损才发现就晚了。"));
        fields.add(new WmsStrategyConditionField("allowMixedMat", "库区允许混放物料", BOOLEAN, null,
                BOOLEAN_OPS,
                "库区自身的混料开关。配等于是，候选只剩允许混料的库区；配等于否，则只往一位一料的库区上架。"));
        fields.add(new WmsStrategyConditionField("maxWeight", "库位最大承重", NUMBER, null, NUMBER_OPS,
                "按库位承重筛，单位与库位维护时一致。配大于等于能把重货挡在轻型货架之外。"));
        fields.add(new WmsStrategyConditionField("maxVolume", "库位最大容积", NUMBER, null, NUMBER_OPS,
                "按库位容积粗筛。跟尺寸校验分工不同：这条比容积，尺寸校验按长宽高逐项比。"));
        return List.copyOf(fields);
    }

    private static List<WmsStrategySortField> putawaySortFields() {
        return List.of(
                new WmsStrategySortField("aisleNo", "巷道号",
                        "配升序从靠入口的巷道开始放，缩短搬运距离。"),
                new WmsStrategySortField("rowNo", "排号", "同巷道内按排号排，配合巷道号一起用。"),
                new WmsStrategySortField("colNo", "列号", "同排内按列号排，配合排号一起用。"),
                new WmsStrategySortField("levelNo", "层号",
                        "配升序优先低层，人工存取省力；配降序优先高层，把低层留给高频物料。"),
                new WmsStrategySortField("depthNo", "纵深号",
                        "双深货架专用。配升序先放外侧，取货不用倒货；配降序先填里侧，库容利用率高。"),
                new WmsStrategySortField("qtyOnhand", "库位现存量",
                        "配升序优先空库位；配降序优先往已有货的库位并板，库容更省。"),
                new WmsStrategySortField("maxVolume", "库位最大容积",
                        "配升序优先塞小库位，把大库位留给大件。"),
                new WmsStrategySortField("locCode", "库位编码",
                        "按编码字典序排，是没有其它偏好时的稳定兜底：保证同一批候选每次给出同一个库位，"
                                + "不会今天一个明天一个。建议放在排序链最后一条。"));
    }

    private static List<WmsStrategyConditionField> pickConditionFields() {
        List<WmsStrategyConditionField> fields = new ArrayList<>();
        fields.add(new WmsStrategyConditionField("qualityStatus", "质量状态", ENUM,
                WmsDictTypeConstants.QUALITY_STATUS, ENUM_OPS,
                "按质量状态筛库存。这条基本必配等于合格，否则待检和不合格的库存也会被拣走。"));
        fields.add(new WmsStrategyConditionField("stockScope", "库存范围", ENUM,
                WmsDictTypeConstants.STOCK_SCOPE, ENUM_OPS,
                "区分公共库存与项目专用库存。项目专用的货被普通订单拣走，项目那边就缺料了。"));
        fields.add(new WmsStrategyConditionField("zoneType", "库区类型", ENUM,
                WmsDictTypeConstants.ZONE_TYPE, ENUM_OPS,
                "限定从哪些库区拣。发货区的货已经在等车，退货区的货还没判定，都不该被拣回去。"));
        fields.add(new WmsStrategyConditionField("locType", "库位类型", ENUM,
                WmsDictTypeConstants.LOC_TYPE, ENUM_OPS,
                "限定从哪种库位拣。线边储位的货是给生产线备的，一般不参与整单出库。"));
        fields.add(new WmsStrategyConditionField("qtyAvailable", "可用量", NUMBER, null, NUMBER_OPS,
                "现存量扣掉预留与冻结之后的可拣量。配大于等于某个值，避免为了几个零头再开一个库位。"));
        fields.add(new WmsStrategyConditionField("remainShelfLifeDays", "剩余保质期天数", NUMBER, null,
                NUMBER_OPS,
                "距失效日期还剩多少天，由批次失效日期推算。配大于等于可以把快到期的批次留给内部消耗，"
                        + "不发给客户。物料没维护保质期时这条不生效。"));
        return List.copyOf(fields);
    }

    private static List<WmsStrategySortField> pickSortFields() {
        return List.of(
                new WmsStrategySortField("expiryDate", "失效日期",
                        "配升序就是 FEFO，先到期先出，有保质期的物料默认选它。"),
                new WmsStrategySortField("produceDate", "生产日期",
                        "配升序优先老货。失效日期没维护时用它近似 FEFO。"),
                new WmsStrategySortField("createTime", "入库时间",
                        "配升序就是 FIFO，先进先出。批属性都没维护时的兜底选择。"),
                new WmsStrategySortField("qtyAvailable", "可用量",
                        "配降序优先拣大库存，少拆几个库位；配升序优先清零头，把库位腾出来。"),
                new WmsStrategySortField("lotNo", "批次号",
                        "按批次号字典序排，适用于批号本身带日期规律的场景。"),
                new WmsStrategySortField("aisleNo", "巷道号",
                        "配升序从靠出口的巷道开始拣，缩短行走距离。"),
                new WmsStrategySortField("locCode", "库位编码",
                        "稳定兜底，保证同一批候选每次给出同一条余额。建议放在排序链最后一条。"));
    }

    private static List<WmsStrategyConditionField> replenishConditionFields() {
        List<WmsStrategyConditionField> fields = new ArrayList<>();
        fields.add(new WmsStrategyConditionField("qtyAvailable", "可用量", NUMBER, null, NUMBER_OPS,
                "线边库存扣掉预留与冻结之后的可用量，补货判断的主力条件：配小于安全水位即触发补货。"));
        fields.add(new WmsStrategyConditionField("qtyOnhand", "现存量", NUMBER, null, NUMBER_OPS,
                "不扣预留与冻结的账面数量。跟可用量的差别在于已被预留的货算不算还在线边，"
                        + "线边货随时会用，按现存量判更保守。"));
        fields.add(new WmsStrategyConditionField("matType", "物料类型", ENUM,
                WmsDictTypeConstants.MAT_TYPE, ENUM_OPS,
                "限定这条策略管哪类物料。不同物料的补货水位差别很大，一条策略通吃会补多或补少。"));
        fields.add(new WmsStrategyConditionField("locType", "库位类型", ENUM,
                WmsDictTypeConstants.LOC_TYPE, ENUM_OPS,
                "限定扫描哪种库位，一般配等于线边储位。"));
        return List.copyOf(fields);
    }

}
