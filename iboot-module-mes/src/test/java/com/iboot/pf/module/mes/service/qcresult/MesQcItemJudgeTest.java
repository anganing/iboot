package com.iboot.pf.module.mes.service.qcresult;

import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.enums.MesQcItemConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcItemTypeEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 检验项单项判定测试。
 *
 * <p>覆盖任务书 T2-05 的判定分支：三种 {@code item_type} 各 1 个通过 + 1 个超差、
 * NUMERIC 只配上限 / 只配下限 / 都配三种限位、数值解析失败视为 FAIL。
 * 判定是纯函数不碰库，不起 Spring 上下文直接测，全分支跑得快。
 */
class MesQcItemJudgeTest {

    private static final String PASS = MesQcItemConclusionEnum.PASS.name();
    private static final String FAIL = MesQcItemConclusionEnum.FAIL.name();

    // ========== NUMERIC：上下限判定 ==========

    @Test
    void testJudgeNumeric_bothLimits_pass() {
        QcItemDO item = item(MesQcItemTypeEnum.NUMERIC.name(), new BigDecimal("0.05"), new BigDecimal("0.10"), null);

        assertEquals(PASS, MesQcItemJudge.judge(item, "0.08"));
        // 边界值闭合在区间内
        assertEquals(PASS, MesQcItemJudge.judge(item, "0.05"));
        assertEquals(PASS, MesQcItemJudge.judge(item, "0.10"));
    }

    @Test
    void testJudgeNumeric_bothLimits_fail() {
        QcItemDO item = item(MesQcItemTypeEnum.NUMERIC.name(), new BigDecimal("0.05"), new BigDecimal("0.10"), null);

        // 低于下限与高于上限都算超差
        assertEquals(FAIL, MesQcItemJudge.judge(item, "0.04"));
        assertEquals(FAIL, MesQcItemJudge.judge(item, "0.11"));
    }

    @Test
    void testJudgeNumeric_lowerOnly() {
        // 只配下限 0.05：上限侧不限
        QcItemDO item = item(MesQcItemTypeEnum.NUMERIC.name(), new BigDecimal("0.05"), null, null);

        assertEquals(FAIL, MesQcItemJudge.judge(item, "0.04"));
        assertEquals(PASS, MesQcItemJudge.judge(item, "100"));
    }

    @Test
    void testJudgeNumeric_upperOnly() {
        // 只配上限 0.10：下限侧不限
        QcItemDO item = item(MesQcItemTypeEnum.NUMERIC.name(), null, new BigDecimal("0.10"), null);

        assertEquals(PASS, MesQcItemJudge.judge(item, "-5"));
        assertEquals(FAIL, MesQcItemJudge.judge(item, "0.11"));
    }

    @Test
    void testJudgeNumeric_noLimits_pass() {
        // 上下限都没配：数值解析得开就合格
        QcItemDO item = item(MesQcItemTypeEnum.NUMERIC.name(), null, null, null);

        assertEquals(PASS, MesQcItemJudge.judge(item, "0.08"));
    }

    @Test
    void testJudgeNumeric_parseFailAsFail() {
        QcItemDO item = item(MesQcItemTypeEnum.NUMERIC.name(), new BigDecimal("0.05"), new BigDecimal("0.10"), null);

        // 解析失败视为 FAIL 不抛异常：空白、非数字、科学计数写错都一样
        assertEquals(FAIL, MesQcItemJudge.judge(item, "abc"));
        assertEquals(FAIL, MesQcItemJudge.judge(item, " "));
        assertEquals(FAIL, MesQcItemJudge.judge(item, ""));
    }

    @Test
    void testJudgeNumeric_scaleIrrelevant() {
        QcItemDO item = item(MesQcItemTypeEnum.NUMERIC.name(), new BigDecimal("0.05"), new BigDecimal("0.10"), null);

        // compareTo 只论大小不比精度：10.0 与 10.00 等价
        assertEquals(PASS, MesQcItemJudge.judge(item, "0.0500"));
    }

    // ========== TEXT：与标准值相等 ==========

    @Test
    void testJudgeText_pass() {
        QcItemDO item = item(MesQcItemTypeEnum.TEXT.name(), null, null, "无击穿");

        assertEquals(PASS, MesQcItemJudge.judge(item, "无击穿"));
        // 去首尾空格后相等
        assertEquals(PASS, MesQcItemJudge.judge(item, " 无击穿 "));
    }

    @Test
    void testJudgeText_fail() {
        QcItemDO item = item(MesQcItemTypeEnum.TEXT.name(), null, null, "无击穿");

        assertEquals(FAIL, MesQcItemJudge.judge(item, "击穿"));
        // 空实测值与任何非空标准值都不等
        assertEquals(FAIL, MesQcItemJudge.judge(item, ""));
    }

    // ========== BOOLEAN：与标准值相等，true/false 与 1/0 等价 ==========

    @Test
    void testJudgeBoolean_pass() {
        QcItemDO item = item(MesQcItemTypeEnum.BOOLEAN.name(), null, null, "true");

        // 四种等价写法：true / TRUE / 1 / 带空格
        assertEquals(PASS, MesQcItemJudge.judge(item, "true"));
        assertEquals(PASS, MesQcItemJudge.judge(item, "TRUE"));
        assertEquals(PASS, MesQcItemJudge.judge(item, "1"));
        assertEquals(PASS, MesQcItemJudge.judge(item, " true "));
    }

    @Test
    void testJudgeBoolean_fail() {
        QcItemDO item = item(MesQcItemTypeEnum.BOOLEAN.name(), null, null, "true");

        assertEquals(FAIL, MesQcItemJudge.judge(item, "false"));
        // 0 与 true 标准不等价
        assertEquals(FAIL, MesQcItemJudge.judge(item, "0"));
        // 写法不认识判 FAIL
        assertEquals(FAIL, MesQcItemJudge.judge(item, "yes"));
    }

    @Test
    void testJudgeBoolean_stdFalse_oneMeansFail() {
        // 标准值 false：实测 0 与它等价
        QcItemDO item = item(MesQcItemTypeEnum.BOOLEAN.name(), null, null, "0");

        assertEquals(PASS, MesQcItemJudge.judge(item, "false"));
        assertEquals(PASS, MesQcItemJudge.judge(item, "0"));
        assertEquals(FAIL, MesQcItemJudge.judge(item, "1"));
    }

    @Test
    void testJudgeBoolean_stdInvalid_fails() {
        // 标准值配置错误（不认识的写法）：宁可全判不合格等人工介入，不拿没对齐的口径放行
        QcItemDO item = item(MesQcItemTypeEnum.BOOLEAN.name(), null, null, "是");

        assertEquals(FAIL, MesQcItemJudge.judge(item, "true"));
        assertEquals(FAIL, MesQcItemJudge.judge(item, "是"));
    }

    // ========== 桩数据 ==========

    /** 检验项桩：类型、上下限、标准值由用例指定，其余字段判定不关心。 */
    private static QcItemDO item(String itemType, BigDecimal lowerLimit,
                                 BigDecimal upperLimit, String stdValue) {
        return QcItemDO.builder()
                .id(1L)
                .itemCode("IT-01")
                .itemName("耐压测试")
                .itemType(itemType)
                .stdValue(stdValue)
                .lowerLimit(lowerLimit)
                .upperLimit(upperLimit)
                .requiredFlag(true)
                .build();
    }

}
