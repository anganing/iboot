package com.iboot.pf.module.mes.enums;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 质检任务结论。与字典 {@code mes_qc_conclusion} 一致。 */
@Getter
@AllArgsConstructor
public enum MesQcConclusionEnum implements ArrayValuable<String> {

    /** 合格。 */
    QUALIFIED("合格"),
    /** 不合格。 */
    REJECTED("不合格"),
    /** 让步接收。 */
    CONCESSION("让步接收");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 是否合格：让步接收不算合格，需要走处置单。 */
    public static boolean isQualified(String conclusion) {
        return ObjUtil.equal(QUALIFIED.name(), conclusion);
    }

}
