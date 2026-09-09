package com.iboot.pf.module.mes.service.qcresult;

import cn.hutool.core.util.StrUtil;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.enums.MesQcItemConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcItemTypeEnum;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 检验项单项判定器。一条实测值对照一个检验项的标准，给出 PASS / FAIL。
 *
 * <p>判定链条见 {@code dev-docs/业务流程/05-MES质量子域流程.md} 的"抽样与判定"：逐项录实测值后
 * 按 {@code item_type} 走三条分支——NUMERIC 判上下限区间（单侧为空该侧不限）、TEXT 与 BOOLEAN
 * 判与标准值相等。判定只回答"这一格合不合格"；任务级的结论汇总（QUALIFIED / REJECTED /
 * CONCESSION）是人的决策，由 {@code submitConclusion} 落定，本类不越权。
 *
 * <p>解析失败（数值格式不对、布尔写法不认识）视为 FAIL 而不是抛异常：检验录入是作业动作，
 * 一个格子格式不对就把整批提交打回去，检验员得对着几十个格子找错；宁可让这一格落 FAIL
 * （FAIL 不放行下游，出错方向是安全的），由人在结果页看到红色不合格后改数重交。
 *
 * <p>独立成类而不内联在领域 Service 里：判定口径是纯函数（不碰库、不看登录人），抽出来才能
 * 不起 Spring 上下文直接测全分支，五个检验点将来复用同一把尺子。
 *
 * @author iboot
 */
public final class MesQcItemJudge {

    /** 布尔型"真"的归一值，{@code true} / {@code TRUE} / {@code 1} 都归到它。 */
    private static final String BOOLEAN_TRUE = "true";
    /** 布尔型"假"的归一值，{@code false} / {@code FALSE} / {@code 0} 都归到它。 */
    private static final String BOOLEAN_FALSE = "false";

    private MesQcItemJudge() {
    }

    /**
     * 判定单项实测值。
     *
     * @param item        检验项，判定口径（类型、标准值、上下限）从它取
     * @param actualValue 实测值原始字符串，首尾空格不影响判定
     * @return {@link MesQcItemConclusionEnum#PASS} 或 {@link MesQcItemConclusionEnum#FAIL} 的枚举名
     */
    public static String judge(QcItemDO item, String actualValue) {
        String itemType = item.getItemType();
        if (MesQcItemTypeEnum.NUMERIC.name().equals(itemType)) {
            return judgeNumeric(item, actualValue);
        }
        if (MesQcItemTypeEnum.BOOLEAN.name().equals(itemType)) {
            return judgeBoolean(item, actualValue);
        }
        // TEXT 与未登记的类型一律按文本比对，检验项类型由字典约束，走不到未知分支
        return judgeText(item, actualValue);
    }

    /**
     * 数值型：{@code lower_limit <= 实测值 <= upper_limit}，单侧为空该侧不限。
     *
     * <p>上下限用 {@code compareTo} 比较，只论大小不比精度，{@code 10.0} 与 {@code 10.00} 等价。
     */
    private static String judgeNumeric(QcItemDO item, String actualValue) {
        BigDecimal value = parseNumeric(actualValue);
        if (value == null) {
            return MesQcItemConclusionEnum.FAIL.name();
        }
        if (item.getLowerLimit() != null && value.compareTo(item.getLowerLimit()) < 0) {
            return MesQcItemConclusionEnum.FAIL.name();
        }
        if (item.getUpperLimit() != null && value.compareTo(item.getUpperLimit()) > 0) {
            return MesQcItemConclusionEnum.FAIL.name();
        }
        return MesQcItemConclusionEnum.PASS.name();
    }

    /** 文本型：与标准值去掉首尾空格后相等。 */
    private static String judgeText(QcItemDO item, String actualValue) {
        String std = item.getStdValue() == null ? null : item.getStdValue().trim();
        String actual = actualValue == null ? null : actualValue.trim();
        return Objects.equals(actual, std)
                ? MesQcItemConclusionEnum.PASS.name()
                : MesQcItemConclusionEnum.FAIL.name();
    }

    /**
     * 布尔型：与标准值相等，{@code true}/{@code false} 与 {@code 1}/{@code 0} 两种写法等价，
     * 大小写不敏感。任何一侧写法不认识都判 FAIL：标准值配置错了宁可全判不合格等人工介入，
     * 也不能拿一个没对齐过的口径放行。
     */
    private static String judgeBoolean(QcItemDO item, String actualValue) {
        String actual = normalizeBoolean(actualValue);
        String std = normalizeBoolean(item.getStdValue());
        if (actual == null || std == null) {
            return MesQcItemConclusionEnum.FAIL.name();
        }
        return actual.equals(std)
                ? MesQcItemConclusionEnum.PASS.name()
                : MesQcItemConclusionEnum.FAIL.name();
    }

    /**
     * 解析数值，格式不合法返回 null 由调用方按 FAIL 处理。
     *
     * <p>用 {@link BigDecimal} 的原生解析而非正则预校验：{@code 1e3}、{@code +5} 这类合法写法
     * 不必自己枚举，解析器认不出的就是该判 FAIL 的。
     */
    private static BigDecimal parseNumeric(String raw) {
        if (StrUtil.isBlank(raw)) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 布尔值归一：true/false 与 1/0 等价、大小写不敏感，不认识的写法返回 null。 */
    private static String normalizeBoolean(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (BOOLEAN_TRUE.equalsIgnoreCase(value) || "1".equals(value)) {
            return BOOLEAN_TRUE;
        }
        if (BOOLEAN_FALSE.equalsIgnoreCase(value) || "0".equals(value)) {
            return BOOLEAN_FALSE;
        }
        return null;
    }

}
