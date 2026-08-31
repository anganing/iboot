package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 抽样类型。与字典 {@code mes_qc_sample_type} 一致。 */
@Getter
@AllArgsConstructor
public enum MesQcSampleTypeEnum implements ArrayValuable<String> {

    /** 全检。 */
    FULL("全检"),
    /** 固定数量抽检。 */
    FIXED_QTY("固定数量抽检"),
    /** 按比例抽检。 */
    PERCENT("按比例抽检"),
    /** AQL抽检。 */
    AQL("AQL抽检");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否需要抽样值：固定数量与比例抽检必须给出 sampleValue。 */
    public static boolean needSampleValue(String sampleType) {
        return ObjUtil.equal(FIXED_QTY.name(), sampleType) || ObjUtil.equal(PERCENT.name(), sampleType);
    }

    /** 是否 AQL 抽检：必须给出接收质量限。 */
    public static boolean isAql(String sampleType) {
        return ObjUtil.equal(AQL.name(), sampleType);
    }

}
