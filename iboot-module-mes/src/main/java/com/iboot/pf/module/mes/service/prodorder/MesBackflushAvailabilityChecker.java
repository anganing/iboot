package com.iboot.pf.module.mes.service.prodorder;

import cn.hutool.core.util.ObjUtil;
import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.bom.dto.BomLineRespDTO;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesIssueMethodEnum;
import com.iboot.pf.module.mes.service.MesBusinessRules;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import com.iboot.pf.module.wms.api.warehouse.WarehouseApi;
import com.iboot.pf.module.wms.api.warehouse.dto.WhRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_BACKFLUSH_QTY_INSUFFICIENT;

/**
 * 关闭生产工单前的倒冲料可用量前置校验，只读，不改任何数据。
 *
 * <p><b>存在的唯一理由是错误信息</b>。关闭工单会给倒冲件按实际产量建出库单并过账，可用量不足时抛错的是 WMS
 * （{@code INV_AVAILABLE_QTY_NOT_ENOUGH}），那条消息里只有物料<b>编号</b>，车间拿着一串数字既不知道缺什么、
 * 也不知道缺多少，只能猜。本类在动作之前批量查一次可用量，把物料编码与缺口量写进消息
 * （做法与领料提交的前置校验同源）。
 *
 * <p><b>它不是并发下的防线</b>，最终防线仍是 WMS 过账那一步：两次查询之间别人可以把料领走。
 *
 * <p>查询口径刻意取"上界"，只按物料 + 库存组织 + 仓库汇总，<b>不</b>按库位、项目、质量状态过滤，
 * 而 WMS 过账时是按行上的库位、批次、项目与质量状态逐行查的。因此本类看到的可用量恒 ≥ WMS 看到的：
 * <ul>
 *     <li>本类判缺料 ⇒ WMS 必然也缺 ⇒ 拦下来一定不冤，不会出现"明明能关却被前置校验挡住"</li>
 *     <li>本类判够而 WMS 判缺（库位不对、项目专用库存）⇒ 退化成原来的行为，车间看到 WMS 那条消息</li>
 * </ul>
 * 反过来按库位或质量状态收紧过滤会把可用量算小，出现假拦截 —— 那比消息不可读严重得多。
 *
 * <p>需求量算法与 {@link MesOrderCloseHelper} 那一份必须一致：同一个公式
 * （{@link MesBusinessRules#requiredQty}）、同样按 {@link MesIssueMethodEnum#isBackflush} 筛倒冲件、
 * 同样按物料合并多行、父项数量同样取<b>实际产量</b>。眼下是两处代码：
 * {@code MesOrderCloseHelper.backflushRequirements} 是私有方法，而那个文件在本任务的禁改清单里。
 * 正解是让它转调本类的 {@link #backflushRequirements}，只能由调度方做，已记入自查的遗留问题。
 */
@Component
public class MesBackflushAvailabilityChecker {

    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private BomApi bomApi;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private InventoryApi inventoryApi;
    @Resource
    private WarehouseApi warehouseApi;

    /**
     * 关闭工单前校验倒冲料够不够反冲。
     *
     * <p>四种情形直接放过，交给关闭动作自己裁决，本类不新增任何拦截条件：工单不存在、没锁 BOM（没下达过）、
     * 实际产量为零、仓库解析不出来。最后一种尤其重要 —— 倒冲物料默认仓库不一致时关闭动作会抛
     * {@code PROD_ORDER_BACKFLUSH_WH_CONFLICT}（消息里带物料编码，已经可读），
     * 在这里抢先抛一个"可用量不足"反而把真正的原因盖掉了。
     *
     * @param prodOrderId 生产工单编号
     * @param whId        调用方指定的发料仓库，为空或 0 时回落倒冲物料的默认仓库
     */
    public void validateBeforeClose(Long prodOrderId, Long whId) {
        ProdOrderDO order = prodOrderId == null ? null : prodOrderMapper.selectById(prodOrderId);
        if (order == null) {
            return;
        }
        BigDecimal actualQty = nvl(order.getCompletedQty());
        if (isNone(order.getBomId()) || actualQty.signum() <= 0) {
            return;
        }
        Map<Long, BigDecimal> requirements = backflushRequirements(order.getBomId(), actualQty);
        if (requirements.isEmpty()) {
            return;
        }
        Map<Long, MaterialRespDTO> materialMap = materialApi.getMaterialMap(requirements.keySet());
        Long resolvedWhId = resolveWhId(whId, requirements.keySet(), materialMap);
        if (isNone(resolvedWhId)) {
            return;
        }
        Map<Long, BigDecimal> availableMap = loadAvailableQty(requirements.keySet(), resolvedWhId);
        for (Map.Entry<Long, BigDecimal> entry : requirements.entrySet()) {
            BigDecimal availableQty = availableMap.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            if (availableQty.compareTo(entry.getValue()) < 0) {
                throw exception(PROD_ORDER_BACKFLUSH_QTY_INSUFFICIENT,
                        matText(entry.getKey(), materialMap), plain(entry.getValue()), plain(availableQty));
            }
        }
    }

    /**
     * 取 BOM 里的倒冲件并按实际产量算需求量，同一物料多行合并。
     *
     * <p>合并的理由与齐套展开、反冲扣料一致：库存对一个物料只有一份，不合并会分两行各算一遍缺口，
     * 两行各自都够而加起来不够时会全放过去。
     *
     * @param bomId     工单锁定的 BOM 编号
     * @param actualQty 实际产量，即工单的已完工数量
     * @return 物料编号 -&gt; 需反冲数量，按 BOM 行顺序
     */
    public Map<Long, BigDecimal> backflushRequirements(Long bomId, BigDecimal actualQty) {
        Map<Long, BigDecimal> merged = new LinkedHashMap<>();
        for (BomLineRespDTO line : bomApi.getBomLineList(bomId)) {
            if (!MesIssueMethodEnum.isBackflush(line.getIssueMethod())) {
                continue;
            }
            BigDecimal reqQty = MesBusinessRules.requiredQty(actualQty, line.getQty(),
                    line.getScrapRate() == null ? BigDecimal.ZERO : line.getScrapRate());
            if (reqQty.signum() <= 0) {
                continue;
            }
            merged.merge(line.getMatId(), reqQty, BigDecimal::add);
        }
        return merged;
    }

    /** 发料仓库：入参优先，否则取倒冲物料的默认仓库；各物料默认仓库不一致时返回 0，让关闭动作报它自己的冲突错误。 */
    private Long resolveWhId(Long whId, Collection<Long> matIds, Map<Long, MaterialRespDTO> materialMap) {
        if (!isNone(whId)) {
            return whId;
        }
        Long resolved = null;
        for (Long matId : matIds) {
            MaterialRespDTO material = materialMap == null ? null : materialMap.get(matId);
            Long defaultWhId = material == null ? null : material.getDefaultWhId();
            if (isNone(defaultWhId) || (resolved != null && !resolved.equals(defaultWhId))) {
                return MesConstants.NONE_ID;
            }
            resolved = defaultWhId;
        }
        return resolved == null ? MesConstants.NONE_ID : resolved;
    }

    /** 批量查可用量，一次门面调用不产生 N+1。 */
    private Map<Long, BigDecimal> loadAvailableQty(Collection<Long> matIds, Long whId) {
        WhRespDTO wh = warehouseApi.getWh(whId);
        InventoryQueryReqDTO query = new InventoryQueryReqDTO();
        query.setMatIds(matIds);
        query.setInvOrgId(wh == null ? null : wh.getInvOrgId());
        query.setWhId(whId);
        Map<Long, BigDecimal> availableMap = inventoryApi.getAvailableQtyMap(query);
        return availableMap == null ? Map.of() : availableMap;
    }

    /** 消息里给物料编码，车间照着编码就能找到料；主数据查不到时退化成物料编号。 */
    private String matText(Long matId, Map<Long, MaterialRespDTO> materialMap) {
        MaterialRespDTO material = materialMap == null ? null : materialMap.get(matId);
        return material == null ? String.valueOf(matId) : material.getCode();
    }

    /** 数量转可读文本：库表是 decimal(20,6)，直接拼会给车间看到"100.000000"这种噪声。 */
    private String plain(BigDecimal qty) {
        return qty.stripTrailingZeros().toPlainString();
    }

    private BigDecimal nvl(BigDecimal qty) {
        return qty == null ? BigDecimal.ZERO : qty;
    }

    private boolean isNone(Long id) {
        return id == null || ObjUtil.equal(MesConstants.NONE_ID, id);
    }

}
