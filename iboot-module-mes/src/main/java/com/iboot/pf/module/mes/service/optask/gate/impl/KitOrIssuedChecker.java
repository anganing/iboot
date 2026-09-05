package com.iboot.pf.module.mes.service.optask.gate.impl;

import cn.hutool.core.collection.CollUtil;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.service.kitsnapshot.MesKitRequirementExpander;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitRequirementBO;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateItem;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateNames;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_KITTED;

/**
 * 门禁二：齐套或已发料。
 *
 * <p><b>判定口径</b>：只拦 {@code must_issue_before_start = true} 的物料，要求它们的<b>净发料量</b>
 * （领料与补料的 {@code processed_qty} 之和减退料的）达到本工序需求量。
 *
 * <p>为什么是"已发料"而不是"齐套"：门禁在车间现场触发，工序要开工，料必须在工位上。
 * 仓库账上有但没发下来，工人手里没料照样干不了活。{@code must_issue_before_start} 这个字段名
 * 已经把答案写死了——"必须在开工前发料"。
 *
 * <p>那 04 文档"齐套<b>或</b>已发料"的"或"指什么：指这一关的两条放行路径。必发料走"已发料"这条，
 * 非必发料（边做边领、倒冲）走"齐套"这条，而后者由齐套检查动作负责、不阻塞开工。
 * 所以这里只实现会拦人的那一支，与任务条目的描述一致。
 *
 * <p>与齐套检查判准的关系：齐套判的是 {@code 可用量 + 已预留 + 已领用 >= 需求量}（料能不能凑齐），
 * 门禁判的是 {@code 已发料 >= 需求量}（料到没到工位）。前者是计划口径、后者是现场口径，
 * 前者通过不代表后者通过——这正是领料三段存在的理由。两者共用同一个需求量展开器，
 * 不会出现"齐套判齐了而门禁按另一个需求量算缺料"。
 */
@Component
public class KitOrIssuedChecker implements MesStartGateItem {

    @Resource
    private MesKitRequirementExpander requirementExpander;
    @Resource
    private MaterialReqMapper materialReqMapper;
    @Resource
    private MaterialReqLineMapper materialReqLineMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private MaterialApi materialApi;

    @Override
    public String name() {
        return MesStartGateNames.KIT_OR_ISSUED;
    }

    @Override
    public int order() {
        return 20;
    }

    /** 没挂路线工序的任务查不到用料关系，无从判定。 */
    @Override
    public boolean enabled(OpTaskDO task, MesStartGateContext ctx) {
        return task.getRouteOpId() != null && !MesConstants.NONE_ID.equals(task.getRouteOpId());
    }

    @Override
    public void check(OpTaskDO task, MesStartGateContext ctx) {
        List<MesKitRequirementBO> mustIssueRequirements =
                requirementExpander.expandByRouteOps(List.of(task.getRouteOpId()), task.getPlannedQty())
                        .stream().filter(item -> Boolean.TRUE.equals(item.getMustIssueBeforeStart())).toList();
        if (CollUtil.isEmpty(mustIssueRequirements)) {
            return;
        }
        Map<Long, BigDecimal> issuedByMat = issuedQtyByMat(task);
        List<Shortage> shortages = new ArrayList<>();
        for (MesKitRequirementBO requirement : mustIssueRequirements) {
            BigDecimal issuedQty = issuedByMat.getOrDefault(requirement.getMatId(), BigDecimal.ZERO);
            BigDecimal shortQty = requirement.getReqQty().subtract(issuedQty);
            if (shortQty.signum() > 0) {
                shortages.add(new Shortage(requirement.getMatId(), shortQty));
            }
        }
        if (CollUtil.isNotEmpty(shortages)) {
            throw exception(OP_TASK_NOT_KITTED, describe(shortages));
        }
    }

    /**
     * 本工序可用的净发料量，按物料汇总。
     *
     * <p>范围是"能覆盖本工序的申请"三档：工序级、本工序所属作业工单级、整单级。只看工序级的话，
     * 按整单一次领料的场景会被误判成缺料——料明明在车间，门禁却不认。
     *
     * <p>退料按负数计入：退回仓库的料不在工位上了，开工时用不了。
     *
     * <p>直接累计 {@code processed_qty} 而不是只认 {@code status = DONE} 的行：确认发料支持部分发，
     * 部分发的行状态是 {@code PARTIAL} 而它已经发下去的量是实打实的。只认 DONE 会把已发的量算成 0，
     * 缺口虚高，把能开的工挡住。
     */
    private Map<Long, BigDecimal> issuedQtyByMat(OpTaskDO task) {
        List<MaterialReqDO> reqs = coveringReqs(task);
        if (CollUtil.isEmpty(reqs)) {
            return Map.of();
        }
        Map<Long, Boolean> returnFlagByReq = reqs.stream().collect(Collectors.toMap(
                MaterialReqDO::getId, req -> MesMaterialReqTypeEnum.isReturn(req.getReqType())));
        List<MaterialReqLineDO> lines =
                materialReqLineMapper.selectList(MaterialReqLineDO::getReqId, returnFlagByReq.keySet());
        Map<Long, BigDecimal> issuedByMat = new HashMap<>();
        for (MaterialReqLineDO line : lines) {
            BigDecimal processedQty = line.getProcessedQty() == null ? BigDecimal.ZERO : line.getProcessedQty();
            if (processedQty.signum() == 0) {
                continue;
            }
            boolean isReturn = Boolean.TRUE.equals(returnFlagByReq.get(line.getReqId()));
            issuedByMat.merge(line.getMatId(), isReturn ? processedQty.negate() : processedQty, BigDecimal::add);
        }
        return issuedByMat;
    }

    /** 一次查回覆盖本工序的三档申请，不逐档查。 */
    private List<MaterialReqDO> coveringReqs(OpTaskDO task) {
        Long workOrderId = nvl(task.getWorkOrderId());
        WorkOrderDO workOrder = MesConstants.NONE_ID.equals(workOrderId) ? null
                : workOrderMapper.selectById(workOrderId);
        Long prodOrderId = workOrder == null ? MesConstants.NONE_ID : nvl(workOrder.getProdOrderId());
        return materialReqMapper.selectList(new LambdaQueryWrapperX<MaterialReqDO>()
                .and(outer -> outer
                        .eq(MaterialReqDO::getOpTaskId, task.getId())
                        .or(middle -> middle
                                .eq(MaterialReqDO::getWorkOrderId, workOrderId)
                                .eq(MaterialReqDO::getOpTaskId, MesConstants.NONE_ID))
                        .or(inner -> inner
                                .eq(MaterialReqDO::getProdOrderId, prodOrderId)
                                .eq(MaterialReqDO::getWorkOrderId, MesConstants.NONE_ID))));
    }

    /** 消息给物料编码与缺口量：车间照着编码就能去仓库催料，给物料编号没人看得懂。 */
    private String describe(List<Shortage> shortages) {
        List<Long> matIds = shortages.stream().map(item -> item.matId).distinct().toList();
        Map<Long, MaterialRespDTO> materialMap = materialApi.getMaterialMap(matIds);
        return shortages.stream().map(item -> {
            MaterialRespDTO material = materialMap == null ? null : materialMap.get(item.matId);
            String matText = material == null ? String.valueOf(item.matId)
                    : material.getCode() + " " + material.getName();
            return matText + " 还缺 " + item.shortQty.stripTrailingZeros().toPlainString();
        }).collect(Collectors.joining("、"));
    }

    private Long nvl(Long id) {
        return id == null ? MesConstants.NONE_ID : id;
    }

    /** 一个物料的缺口。 */
    private static final class Shortage {

        private final Long matId;
        private final BigDecimal shortQty;

        private Shortage(Long matId, BigDecimal shortQty) {
            this.matId = matId;
            this.shortQty = shortQty;
        }

    }

}
