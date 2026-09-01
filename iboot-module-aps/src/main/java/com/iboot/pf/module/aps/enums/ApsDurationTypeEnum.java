package com.iboot.pf.module.aps.enums;

import cn.hutool.core.util.ObjUtil;

/**
 * 工序工时类型。取值与 mdm {@code mdm_op.duration_type} 保持一致。
 *
 * <p>不做成 {@code ArrayValuable}：这是从 mdm 投影过来的快照值，APS 侧不对外提供按它筛选的查询。
 */
public enum ApsDurationTypeEnum {

    /** 固定工时：不随数量变化，直接取 {@code stdDuration}。 */
    FIXED,
    /** 按件累加：{@code setup + qty * perUnit + teardown}。 */
    PER_UNIT;

    /** 是否固定工时；投影值为空时按按件累加处理，与 mdm 默认值一致。 */
    public static boolean isFixed(String durationType) {
        return ObjUtil.equal(FIXED.name(), durationType);
    }

}
