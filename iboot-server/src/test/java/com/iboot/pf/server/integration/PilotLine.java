package com.iboot.pf.server.integration;

import java.math.BigDecimal;
import java.util.List;

/**
 * 试点产线主数据集合——fixture 建完数据后的句柄。
 *
 * 各 fixture 方法返回创建出来的 ID，测试用例按需取用；
 * 这里聚合"一条产线"的全套主数据 ID，方便端到端用例一次性取齐。
 *
 * @author iboot
 */
public record PilotLine(
        /* ---- MDM 组织与资源 ---- */
        Long invOrgId,
        Long plantId,
        Long workshopId,
        Long wcId,
        Long uomId,
        Long fgMatId,          // 成品
        List<Long> componentMatIds, // 3 个子件：分别按 PICK / BACKFLUSH / PHANTOM 挂 BOM
        Long matCatId,
        Long shiftId,
        Long calendarId,
        Long resId,
        Long resCapabilityId,
        Long resCalendarId,
        List<Long> opIds,      // 3 道工序
        Long routeId,
        List<Long> routeOpIds, // 3 个路线工序（seq=10,20,30；其中 20/30 同一 parallel_group）
        List<Long> routeOpMatIds,
        Long bomId,

        /* ---- WMS 仓储 ---- */
        Long whId,
        Long zoneId,
        Long locId,
        List<Long> initialInOrderIds, // 初始库存经 InOrderApi 真实过账建

        /* ---- MES / 质量基础数据 ---- */
        Long qcPlanId,
        List<Long> qcItemIds,
        List<Long> qcRuleIds  // 五检验点各一条规则
) {

    public Long anyComponentMatId() {
        return componentMatIds.get(0);
    }

    public static final BigDecimal DEMAND_QTY = new BigDecimal("100");
}
