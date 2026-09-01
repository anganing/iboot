package com.iboot.pf.module.aps.dal.dataobject.projection;

import java.math.BigDecimal;

/**
 * {@link ApsResourceProjectionDO#getCapabilityJson()} 的元素结构。
 *
 * <p>刷新资源投影时由 mdm 资源能力序列化写入，求解时反序列化判定“这台资源能不能做这道工序”
 * 以及按能力效率折算工时。求解只读投影，不回查 mdm。
 *
 * @param opId       标准工序编号
 * @param efficiency 该资源做该工序的专属效率系数；为空按 1 处理
 * @param preferred  是否首选资源
 */
public record ApsResourceCapability(Long opId, BigDecimal efficiency, Boolean preferred) {

    /** 默认效率：能力上没配就按不折算处理，避免除零。 */
    public BigDecimal efficiencyOrDefault() {
        return efficiency == null || efficiency.signum() <= 0 ? BigDecimal.ONE : efficiency;
    }

}
