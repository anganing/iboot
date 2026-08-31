package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 质检触发点。与字典 {@code mes_qc_trigger_point} 一致。 */
@Getter
@AllArgsConstructor
public enum MesQcTriggerPointEnum implements ArrayValuable<String> {

    /** 来料检验。 */
    IQC("来料检验"),
    /** 过程检验。 */
    IPQC("过程检验"),
    /** 完工检验。 */
    FQC("完工检验"),
    /** 出货检验。 */
    OQC("出货检验"),
    /** 首件检验。 */
    FAI("首件检验");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
