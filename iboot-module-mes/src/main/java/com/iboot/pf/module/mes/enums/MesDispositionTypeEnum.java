package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 不合格品处置类型。与字典 {@code mes_disposition_type} 一致。 */
@Getter
@AllArgsConstructor
public enum MesDispositionTypeEnum implements ArrayValuable<String> {

    /** 返工。 */
    REWORK("返工"),
    /** 报废。 */
    SCRAP("报废"),
    /** 让步接收。 */
    CONCESSION("让步接收"),
    /** 冻结库存。 */
    FREEZE("冻结库存"),
    /** 退供应商。 */
    RETURN_SUPPLIER("退供应商"),
    /** 调整工艺。 */
    ADJUST_PROCESS("调整工艺");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
