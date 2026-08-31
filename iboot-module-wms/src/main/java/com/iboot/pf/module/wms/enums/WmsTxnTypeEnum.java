package com.iboot.pf.module.wms.enums;

import com.iboot.pf.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 库存事务类型。 */
@Getter
@AllArgsConstructor
public enum WmsTxnTypeEnum implements ArrayValuable<String> {

    /** 入库收货。 */
    RECEIPT("入库收货", WmsIoTypeEnum.IN),
    /** 出库发料。 */
    ISSUE("出库发料", WmsIoTypeEnum.OUT),
    /** 转移调拨。 */
    TRANSFER("转移调拨", WmsIoTypeEnum.MOVE),
    /** 预留占用。 */
    RESERVE("预留占用", WmsIoTypeEnum.STATUS),
    /** 预留释放。 */
    RELEASE("预留释放", WmsIoTypeEnum.STATUS),
    /** 冻结。 */
    FREEZE("冻结", WmsIoTypeEnum.STATUS),
    /** 解冻。 */
    UNFREEZE("解冻", WmsIoTypeEnum.STATUS),
    /** 质量状态变更：按来源与目标质量状态在两条余额之间搬运，语义等同 MOVE。 */
    QUALITY_CHANGE("质量状态变更", WmsIoTypeEnum.MOVE),
    /** 盘点调整：默认按盘盈处理，盘亏必须显式传 ioType=OUT。 */
    ADJUST("盘点调整", WmsIoTypeEnum.IN);

    public static final String[] ARRAYS = Arrays.stream(values()).map(Enum::name).toArray(String[]::new);

    private final String name;

    /** 默认收发方向，过账时按事务类型推导，避免调用方传错。 */
    private final WmsIoTypeEnum ioType;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
