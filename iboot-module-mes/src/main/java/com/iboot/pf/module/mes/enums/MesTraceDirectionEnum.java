package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 追溯查询方向。
 *
 * <p>正向 {@code GET /mes/trace/forward}、反向 {@code GET /mes/trace/backward} 是两个端点，本来不需要这个枚举；
 * 导出只有一个 {@code GET /mes/trace/export-excel}（客户审核交材料只需要一份表），方向退化成入参，
 * 由它决定调用哪个解析器。放在 enums 而非散落字符串，是为了能挂 {@code @InEnum} 校验。
 */
@Getter
@AllArgsConstructor
public enum MesTraceDirectionEnum implements ArrayValuable<String> {

    /** 正向：从原料批次拉到出货客户。 */
    FORWARD("正向"),
    /** 反向：从成品批次反查到原料供应商。 */
    BACKWARD("反向");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    /** 按取值解析，不合法返回 null，由调用方决定报错方式。 */
    public static MesTraceDirectionEnum ofName(String name) {
        for (MesTraceDirectionEnum direction : values()) {
            if (direction.name().equals(name)) {
                return direction;
            }
        }
        return null;
    }

}
