package com.iboot.pf.module.mes.service.kitsnapshot;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.bom.dto.BomLineRespDTO;
import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpMatRespDTO;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesIssueMethodEnum;
import com.iboot.pf.module.mes.service.MesBusinessRules;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitRequirementBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.KIT_CHECK_BOM_DEPTH_EXCEEDED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.KIT_CHECK_PHANTOM_BOM_MISSING;

/**
 * 物料需求展开。齐套检查与领料申请的需求量都从这里出。
 *
 * <p>做三件事，顺序不能换：
 * <ol>
 *     <li>按供料方式分流：常规料计入需求，{@code PHANTOM} 不计但向下展开子件，{@code BACKFLUSH} 直接剔除。
 *     行为差异表见 {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"三种发料方式的行为差异"</li>
 *     <li>算需求量：一律走 {@link MesBusinessRules#requiredQty}，不在本类重写公式</li>
 *     <li>按物料合并：同一物料出现在多道工序时合并成一行</li>
 * </ol>
 *
 * <p>第 3 步是齐套判准的前提，不是为了少几行：库存可用量对一个物料只有一份，
 * 两道工序各要 100、库存 150 时不合并就会两行都判齐套，实际开不了工。
 *
 * <p>从 {@link MesKitCheckServiceImpl} 里拆出来独立成类，原因是虚拟件递归本身就是一段完整逻辑，
 * 混在齐套动作里会让"算需求"和"比库存"两件事纠缠。
 */
@Component
public class MesKitRequirementExpander {

    /**
     * 虚拟件递归展开的深度上限。
     *
     * <p>虚拟件的子件又是虚拟件是合法的 BOM 结构，但环（A 的子件是 B、B 的子件是 A）会让递归不返回。
     * 真实制造 BOM 十层已经极深，超限说明主数据成环，就地报错比栈溢出强。
     */
    private static final int MAX_DEPTH = 10;

    @Resource
    private BomApi bomApi;
    @Resource
    private RouteApi routeApi;

    /**
     * 按整单 BOM 展开需求，用于 {@code PROD_ORDER} 级齐套与整单领料。
     *
     * @param bomId     工单下达时锁定的 BOM 编号
     * @param parentQty 父项数量，取工单计划数量
     * @return 需求行，按物料合并，顺序为首次出现的次序
     */
    public List<MesKitRequirementBO> expandByBom(Long bomId, BigDecimal parentQty) {
        List<BomLineRespDTO> bomLines = bomApi.getBomLineList(bomId);
        Map<Long, MesKitRequirementBO> merged = new LinkedHashMap<>();
        for (BomLineRespDTO line : bomLines) {
            accumulate(merged, line.getMatId(), parentQty, line.getQty(), line.getScrapRate(),
                    line.getIssueMethod(), MesConstants.NONE_ID, line.getUomId(), Boolean.TRUE, 0);
        }
        return new ArrayList<>(merged.values());
    }

    /**
     * 按路线工序用料展开需求，用于 {@code WORK_ORDER} / {@code OP_TASK} 级齐套与按工序领料。
     *
     * <p>{@code routeOpIds} 一次传全集，门面一次 SQL 查回全部用料行，不按工序逐个查。
     *
     * @param routeOpIds 路线工序编号集合
     * @param parentQty  父项数量，取工序任务计划数量
     * @return 需求行，按物料合并
     */
    public List<MesKitRequirementBO> expandByRouteOps(Collection<Long> routeOpIds, BigDecimal parentQty) {
        if (CollUtil.isEmpty(routeOpIds)) {
            return List.of();
        }
        List<RouteOpMatRespDTO> opMats = routeApi.getRouteOpMatList(routeOpIds);
        Map<Long, MesKitRequirementBO> merged = new LinkedHashMap<>();
        for (RouteOpMatRespDTO opMat : opMats) {
            accumulate(merged, opMat.getMatId(), parentQty, opMat.getQty(), opMat.getScrapRate(),
                    opMat.getIssueMethod(), opMat.getId(), opMat.getUomId(),
                    opMat.getMustIssueBeforeStart(), 0);
        }
        return new ArrayList<>(merged.values());
    }

    /**
     * 处理一行用料：倒冲剔除、虚拟件递归、常规料累加。
     *
     * @param merged        按物料合并的结果容器
     * @param matId         本行物料
     * @param parentQty     本行的父项数量
     * @param qty           单位用量
     * @param scrapRate     损耗率，为空按 0 处理
     * @param issueMethod   供料方式；取值为空的历史数据按常规料处理，漏算需求比多算一行的代价大
     * @param routeOpMatId  路线工序用料编号，BOM 行传 0
     * @param uomId         计量单位
     * @param mustIssue     是否开工前必发
     * @param depth         当前递归深度，顶层为 0
     */
    private void accumulate(Map<Long, MesKitRequirementBO> merged, Long matId, BigDecimal parentQty,
                            BigDecimal qty, BigDecimal scrapRate, String issueMethod,
                            Long routeOpMatId, Long uomId, Boolean mustIssue, int depth) {
        // 倒冲件在这里就出局：报工时按实际产量反冲，齐套算它会让辅料没领就开不了工，领料也不该出现它
        if (MesIssueMethodEnum.isBackflush(issueMethod)) {
            return;
        }
        BigDecimal requiredQty = MesBusinessRules.requiredQty(parentQty, qty,
                scrapRate == null ? BigDecimal.ZERO : scrapRate);
        if (MesIssueMethodEnum.isPhantom(issueMethod)) {
            expandPhantom(merged, matId, requiredQty, depth);
            return;
        }
        MesKitRequirementBO existed = merged.get(matId);
        if (existed == null) {
            merged.put(matId, new MesKitRequirementBO()
                    .setMatId(matId)
                    .setReqQty(requiredQty)
                    .setRouteOpMatId(routeOpMatId == null ? MesConstants.NONE_ID : routeOpMatId)
                    .setUomId(uomId)
                    .setMustIssueBeforeStart(Boolean.TRUE.equals(mustIssue)));
            return;
        }
        existed.setReqQty(existed.getReqQty().add(requiredQty));
        // 合并时服从更严的那条：一处要求开工前必发，合并后就必须开工前必发
        existed.setMustIssueBeforeStart(existed.getMustIssueBeforeStart() || Boolean.TRUE.equals(mustIssue));
    }

    /**
     * 虚拟件向下展开：本身不产生需求，把它"应有的数量"当作父项数量继续展开它的子件。
     *
     * <p>虚拟件是 BOM 的结构层次（比如"某组螺栓套件"），物理上不存在、库存永远是 0，
     * 当常规料算会让齐套永远缺料。
     *
     * <p>没有生效 BOM 时直接报错而不是静默跳过：虚拟件没有子件等于这部分需求凭空消失，
     * 齐套会永远判通过，车间到现场才发现没料。
     */
    private void expandPhantom(Map<Long, MesKitRequirementBO> merged, Long phantomMatId,
                               BigDecimal phantomQty, int depth) {
        if (depth >= MAX_DEPTH) {
            throw exception(KIT_CHECK_BOM_DEPTH_EXCEEDED, MAX_DEPTH);
        }
        BomRespDTO childBom = bomApi.getEffectiveBom(phantomMatId, LocalDate.now());
        if (childBom == null) {
            throw exception(KIT_CHECK_PHANTOM_BOM_MISSING, phantomMatId);
        }
        List<BomLineRespDTO> childLines = bomApi.getBomLineList(childBom.getId());
        if (CollUtil.isEmpty(childLines)) {
            throw exception(KIT_CHECK_PHANTOM_BOM_MISSING, phantomMatId);
        }
        for (BomLineRespDTO child : childLines) {
            // 子件的开工前必发标记继承虚拟件所在行的语义：BOM 行没有这个字段，统一按必发处理
            accumulate(merged, child.getMatId(), phantomQty, child.getQty(), child.getScrapRate(),
                    child.getIssueMethod(), MesConstants.NONE_ID, child.getUomId(), Boolean.TRUE, depth + 1);
        }
    }

}
