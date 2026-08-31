package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 检验模式。与字典 {@code mes_qc_mode} 一致。
 *
 * <p>工序任务只用 NONE、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE；
 * 质检规则额外用 PRE_INBOUND、POST_INBOUND_PENDING 描述入库前后检验。
 */
@Getter
@AllArgsConstructor
public enum MesQcModeEnum implements ArrayValuable<String> {

    /** 免检。 */
    NONE("免检"),
    /** 开工前检验。 */
    BEFORE_START("开工前检验"),
    /** 首件检验。 */
    FIRST_ARTICLE("首件检验"),
    /** 过程检验。 */
    PROCESS("过程检验"),
    /** 完工前检验。 */
    BEFORE_COMPLETE("完工前检验"),
    /** 入库前检验。 */
    PRE_INBOUND("入库前检验"),
    /** 先入库待检。 */
    POST_INBOUND_PENDING("先入库待检");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
