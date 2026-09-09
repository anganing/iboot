package com.iboot.pf.module.mes.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 在制流转事务类型。与字典 {@code mes_wip_txn_type} 一致。 */
@Getter
@AllArgsConstructor
public enum MesWipTxnTypeEnum implements ArrayValuable<String> {

    /** 开工。 */
    START("开工"),
    /** 暂停。 */
    PAUSE("暂停"),
    /** 恢复。 */
    RESUME("恢复"),
    /** 报工。 */
    REPORT("报工"),
    /** 工序流转。 */
    MOVE("工序流转"),
    /** 跳序。 */
    SKIP("跳序"),
    /** 完工。 */
    COMPLETE("完工"),
    /** 完工入库：成品入库房过账，qty 记本次入库量，来源业务是 WMS 入库单。 */
    FINISH_INBOUND("完工入库"),
    /** 返工。 */
    REWORK("返工"),
    /** 报废。 */
    SCRAP("报废");

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
