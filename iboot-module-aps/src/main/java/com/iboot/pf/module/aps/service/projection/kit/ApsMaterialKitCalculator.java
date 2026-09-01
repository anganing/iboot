package com.iboot.pf.module.aps.service.projection.kit;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsInventoryProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsInventoryProjectionMapper;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.bom.dto.BomLineRespDTO;
import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工序级物料齐套判定：BOM 展开的用料需求对冲库存投影的可用量。
 *
 * <p>结论冻结进 {@code aps_projection_order.material_shortage_flag}，求解时由物料齐套约束读取。
 * 放在投影阶段而不是求解时现算：库存每分钟都在动，现算会让同一场景重复求解得出不同结论，
 * 追溯链断掉；投影是场景冻结的输入副本，齐套结论属于输入的一部分。
 *
 * <p>按交期先后分配库存，而不是各工序独立判断：同一种料被多个工单争用时，独立判断会让每个工单
 * 都以为自己有料，算出一份全都齐套的假结论。先到期的先占，是车间实际的分配逻辑。
 *
 * <p>本版只算现有库存，不含在途采购与上游工序产出，结论偏保守：采购件下周到货、本可先排上的
 * 工序会被判为缺料。要放开需要 WMS 提供在途量门面，属于独立范围。
 */
@Component
public class ApsMaterialKitCalculator {

    @Resource
    private ApsInventoryProjectionMapper inventoryProjectionMapper;
    @Resource
    private BomApi bomApi;

    /**
     * 给工单投影补齐缺料标记与缺料数量。
     *
     * <p>直接改传入的行对象：调用方紧接着就把它们批量写库，多返回一份副本没有意义。
     *
     * @param scenarioId 排程场景编号
     * @param rows 工单投影行，会被就地修改
     */
    public void fillMaterialShortage(Long scenarioId, List<ApsOrderProjectionDO> rows) {
        if (CollUtil.isEmpty(rows)) {
            return;
        }
        // 先把所有行的结论归零：没有 BOM、没有用料的工序就是齐套的
        rows.forEach(row -> {
            row.setMaterialShortageFlag(false);
            row.setMaterialShortQty(ApsConstants.ZERO_QTY);
        });
        Map<Long, List<BomLineRespDTO>> bomLineMap = loadBomLines(rows);
        if (bomLineMap.isEmpty()) {
            return;
        }
        Map<Long, BigDecimal> availableMap = sumAvailableByMat(scenarioId);
        for (ApsOrderProjectionDO row : sortByAllocationOrder(rows)) {
            // 已开工的工序料已经领了，不参与分配也不判缺料
            if (Boolean.TRUE.equals(row.getLockedFlag())) {
                continue;
            }
            allocate(row, bomLineMap.getOrDefault(row.getMatId(), List.of()), availableMap,
                    firstSeqOfOrder(rows, row.getProdOrderId()));
        }
    }

    /**
     * 成品物料 -> BOM 明细。
     *
     * <p>只展开一层：多层展开要靠 MRP 逐层下达，那是另一套逻辑；排程关心的是"这道工序要领的料够不够"，
     * 而领料清单本来就是单层 BOM。虚拟件（PHANTOM）跳过，它不占实物库存，需要继续往下展开，
     * 本版按"不影响齐套判定"处理。
     */
    private Map<Long, List<BomLineRespDTO>> loadBomLines(List<ApsOrderProjectionDO> rows) {
        LocalDate today = LocalDate.now();
        Map<Long, List<BomLineRespDTO>> result = new LinkedHashMap<>();
        for (ApsOrderProjectionDO row : rows) {
            if (row.getMatId() == null || result.containsKey(row.getMatId())) {
                continue;
            }
            BomRespDTO bom = bomApi.getEffectiveBom(row.getMatId(), today);
            if (bom == null) {
                result.put(row.getMatId(), List.of());
                continue;
            }
            List<BomLineRespDTO> lines = bomApi.getBomLineList(bom.getId()).stream()
                    .filter(line -> !PHANTOM_ISSUE_METHOD.equals(line.getIssueMethod()))
                    .filter(line -> line.getMatId() != null)
                    .toList();
            result.put(row.getMatId(), lines);
        }
        return result;
    }

    /** 按物料汇总场景内的合格可用库存：待检、不合格、冻结的料不能拿来兑需求。 */
    private Map<Long, BigDecimal> sumAvailableByMat(Long scenarioId) {
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        List<ApsInventoryProjectionDO> balances = inventoryProjectionMapper
                .selectListByScenarioIdAndQualityStatus(scenarioId, WmsQualityStatusEnum.QUALIFIED.name());
        for (ApsInventoryProjectionDO balance : balances) {
            BigDecimal available = balance.getQtyAvailable() == null
                    ? ApsConstants.ZERO_QTY : balance.getQtyAvailable();
            result.merge(balance.getMatId(), available, BigDecimal::add);
        }
        return result;
    }

    /** 分配顺序：交期早的先占料，同交期按工单与工序顺序号。 */
    private List<ApsOrderProjectionDO> sortByAllocationOrder(List<ApsOrderProjectionDO> rows) {
        List<ApsOrderProjectionDO> sorted = new ArrayList<>(rows);
        sorted.sort(Comparator
                .comparing(ApsOrderProjectionDO::getDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ApsOrderProjectionDO::getProdOrderId,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ApsOrderProjectionDO::getSeq,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        return sorted;
    }

    /**
     * 扣减库存并判定单道工序是否缺料。
     *
     * <p>用料绑定到工序：BOM 行的 {@code routeOpSeq} 与工序顺序号一致时归到这道工序。
     * 没绑工序（为空或 0）的料归到工单的首道工序——整单开工前就得齐，不能等到某道中间工序。
     */
    private void allocate(ApsOrderProjectionDO row, List<BomLineRespDTO> lines,
                          Map<Long, BigDecimal> availableMap, Integer firstSeq) {
        if (lines.isEmpty()) {
            return;
        }
        BigDecimal remaining = remainingQty(row);
        if (remaining.signum() <= 0) {
            return;
        }
        BigDecimal shortTotal = ApsConstants.ZERO_QTY;
        for (BomLineRespDTO line : lines) {
            if (!belongsToOperation(line, row.getSeq(), firstSeq)) {
                continue;
            }
            BigDecimal required = requiredQty(line, remaining);
            if (required.signum() <= 0) {
                continue;
            }
            BigDecimal available = availableMap.getOrDefault(line.getMatId(), ApsConstants.ZERO_QTY);
            BigDecimal consumed = required.min(available);
            availableMap.put(line.getMatId(), available.subtract(consumed));
            shortTotal = shortTotal.add(required.subtract(consumed));
        }
        if (shortTotal.signum() > 0) {
            row.setMaterialShortageFlag(true);
            row.setMaterialShortQty(shortTotal);
        }
    }

    /** 用料是否归属这道工序。 */
    private boolean belongsToOperation(BomLineRespDTO line, Integer seq, Integer firstSeq) {
        Integer bindSeq = line.getRouteOpSeq();
        if (bindSeq == null || bindSeq == 0) {
            return java.util.Objects.equals(seq, firstSeq);
        }
        return java.util.Objects.equals(bindSeq, seq);
    }

    /** 需求量 = 单件用量 × 待加工数量 × (1 + 损耗率)，损耗率按小数配置。 */
    private BigDecimal requiredQty(BomLineRespDTO line, BigDecimal remaining) {
        BigDecimal lineQty = line.getQty() == null ? ApsConstants.ZERO_QTY : line.getQty();
        BigDecimal scrapRate = line.getScrapRate() == null ? ApsConstants.ZERO_QTY : line.getScrapRate();
        return lineQty.multiply(remaining).multiply(BigDecimal.ONE.add(scrapRate));
    }

    /** 待加工数量 = 计划数量 - 已完成数量，负数归零。 */
    private BigDecimal remainingQty(ApsOrderProjectionDO row) {
        BigDecimal qty = row.getQty() == null ? ApsConstants.ZERO_QTY : row.getQty();
        BigDecimal completed = row.getCompletedQty() == null ? ApsConstants.ZERO_QTY : row.getCompletedQty();
        BigDecimal remaining = qty.subtract(completed);
        return remaining.signum() < 0 ? ApsConstants.ZERO_QTY : remaining;
    }

    /** 工单的首道工序顺序号，未绑工序的用料归到它。 */
    private Integer firstSeqOfOrder(List<ApsOrderProjectionDO> rows, Long prodOrderId) {
        return rows.stream()
                .filter(item -> java.util.Objects.equals(item.getProdOrderId(), prodOrderId))
                .map(ApsOrderProjectionDO::getSeq)
                .filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    /** 虚拟件供料方式：不占实物库存，不参与齐套判定。 */
    private static final String PHANTOM_ISSUE_METHOD = "PHANTOM";

}
