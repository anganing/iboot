package com.iboot.pf.module.aps.service.projection.kit;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsInventoryProjectionDO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import com.iboot.pf.module.aps.dal.mysql.projection.ApsInventoryProjectionMapper;
import com.iboot.pf.module.aps.enums.ApsConstants;
import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.bom.dto.BomLineRespDTO;
import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;
import com.iboot.pf.module.mes.enums.MesIssueMethodEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private static final int MAX_PHANTOM_DEPTH = 10;
    private static final int QTY_SCALE = 6;

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
        for (MaterialRequirementAllocation allocation : calculateRequirementAllocations(scenarioId, rows)) {
            if (allocation.shortQty().signum() > 0) {
                allocation.order().setMaterialShortageFlag(true);
                allocation.order().setMaterialShortQty(allocation.order().getMaterialShortQty()
                        .add(allocation.shortQty()));
            }
        }
    }

    /**
     * 按投影冻结的库存，计算每道待排工序、每种物料的需求/可用/缺口。
     *
     * <p>投影齐套标记和 {@code aps_shortage} 重建必须共用该方法，二者才能保证需求展开、交期优先扣料
     * 与短缺数量完全一致。
     */
    public List<MaterialRequirementAllocation> calculateRequirementAllocations(Long scenarioId,
                                                                                 List<ApsOrderProjectionDO> rows) {
        if (CollUtil.isEmpty(rows)) {
            return List.of();
        }
        Map<Long, List<BomLineRespDTO>> bomLineMap = loadBomLines(rows);
        if (bomLineMap.isEmpty()) {
            return List.of();
        }
        Map<Long, BigDecimal> availableMap = sumAvailableByMat(scenarioId);
        List<MaterialRequirementAllocation> result = new ArrayList<>();
        for (ApsOrderProjectionDO row : sortByAllocationOrder(rows)) {
            // 已开工已领料；已完工、跳过、取消的工序只作追溯，都不应再占用待排库存。
            if (Boolean.TRUE.equals(row.getLockedFlag())
                    || MesOpTaskStatusEnum.isFinal(row.getCurrentStatus())) {
                continue;
            }
            Map<Long, BigDecimal> requirements = calculateRequirements(row,
                    bomLineMap.getOrDefault(row.getMatId(), List.of()), firstSeqOfOrder(rows, row.getProdOrderId()));
            for (Map.Entry<Long, BigDecimal> entry : requirements.entrySet()) {
                BigDecimal required = entry.getValue();
                if (required.signum() <= 0) {
                    continue;
                }
                BigDecimal available = availableMap.getOrDefault(entry.getKey(), ApsConstants.ZERO_QTY);
                BigDecimal consumed = required.min(available);
                availableMap.put(entry.getKey(), available.subtract(consumed));
                result.add(new MaterialRequirementAllocation(row, entry.getKey(), required, available,
                        required.subtract(consumed)));
            }
        }
        return result;
    }

    /**
     * 成品物料 -> BOM 明细。
     *
     * <p>保留 BOM 原始行，实际需求在分配时按 MES 齐套规则展开：虚拟件本身不占库存、但要递归展开其子件；
     * 倒冲件不计需求。不能在此处提前过滤虚拟件，否则其子件会从 APS 齐套口径中丢失。
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
                    .filter(line -> line.getMatId() != null).toList();
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
    private Map<Long, BigDecimal> calculateRequirements(ApsOrderProjectionDO row, List<BomLineRespDTO> lines,
                                                          Integer firstSeq) {
        Map<Long, BigDecimal> requirements = new LinkedHashMap<>();
        if (lines.isEmpty()) {
            return requirements;
        }
        BigDecimal remaining = remainingQty(row);
        if (remaining.signum() <= 0) {
            return requirements;
        }
        for (BomLineRespDTO line : lines) {
            if (!belongsToOperation(line, row.getSeq(), firstSeq)) {
                continue;
            }
            accumulateRequirement(requirements, line, remaining, LocalDate.now(), 0);
        }
        return requirements;
    }

    /** 用料是否归属这道工序。 */
    private boolean belongsToOperation(BomLineRespDTO line, Integer seq, Integer firstSeq) {
        Integer bindSeq = line.getRouteOpSeq();
        if (bindSeq == null || bindSeq == 0) {
            return java.util.Objects.equals(seq, firstSeq);
        }
        return java.util.Objects.equals(bindSeq, seq);
    }

    /**
     * 按 MES 齐套规则递归展开一行需求。
     *
     * <p>损耗量用 {@code parentQty × unitQty ÷ (1 - scrapRate)}；虚拟件不记自身、改向下展开；
     * 倒冲件完全剔除。三条规则必须与 {@code MesKitRequirementExpander} 同步，否则 APS 缺料结论
     * 会和开工门禁相互矛盾。
     */
    private void accumulateRequirement(Map<Long, BigDecimal> requirements, BomLineRespDTO line,
                                       BigDecimal parentQty, LocalDate today, int depth) {
        if (MesIssueMethodEnum.isBackflush(line.getIssueMethod())) {
            return;
        }
        BigDecimal required = requiredQty(parentQty, line.getQty(), line.getScrapRate());
        if (!MesIssueMethodEnum.isPhantom(line.getIssueMethod())) {
            requirements.merge(line.getMatId(), required, BigDecimal::add);
            return;
        }
        if (depth >= MAX_PHANTOM_DEPTH) {
            throw new IllegalStateException("虚拟件 BOM 展开超过最大层数：" + MAX_PHANTOM_DEPTH);
        }
        BomRespDTO childBom = bomApi.getEffectiveBom(line.getMatId(), today);
        if (childBom == null) {
            throw new IllegalStateException("虚拟件缺少生效 BOM：" + line.getMatId());
        }
        List<BomLineRespDTO> childLines = bomApi.getBomLineList(childBom.getId());
        if (CollUtil.isEmpty(childLines)) {
            throw new IllegalStateException("虚拟件缺少子件：" + line.getMatId());
        }
        for (BomLineRespDTO child : childLines) {
            if (child.getMatId() != null) {
                accumulateRequirement(requirements, child, required, today, depth + 1);
            }
        }
    }

    /** 与 MES {@code requiredQty} 同口径：父项数量 × 单位用量 ÷ (1 - 损耗率)。 */
    private BigDecimal requiredQty(BigDecimal parentQty, BigDecimal unitQty, BigDecimal scrapRate) {
        BigDecimal actualScrapRate = scrapRate == null ? ApsConstants.ZERO_QTY : scrapRate;
        if (parentQty == null || unitQty == null || actualScrapRate.signum() < 0
                || actualScrapRate.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("数量和损耗率参数不合法");
        }
        return parentQty.multiply(unitQty).divide(BigDecimal.ONE.subtract(actualScrapRate),
                QTY_SCALE, RoundingMode.HALF_UP);
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

    /** 单道工序、单种物料按交期优先分配后的冻结数量。 */
    public record MaterialRequirementAllocation(ApsOrderProjectionDO order, Long matId, BigDecimal reqQty,
                                                BigDecimal availQty, BigDecimal shortQty) {
    }
}
