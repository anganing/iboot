package com.iboot.pf.module.mes.service.kitsnapshot;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.mdm.api.matalt.MatAltApi;
import com.iboot.pf.module.mdm.api.matalt.dto.MatAltRespDTO;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.kitsnapshot.KitSnapshotDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.kitsnapshot.KitSnapshotMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitCheckScopeEnum;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqLineStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStateMachine;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandService;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitCheckBO;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitCheckResultBO;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitRequirementBO;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import com.iboot.pf.module.mes.service.wiptxn.bo.MesWipTxnBO;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.BOM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.KIT_CHECK_SCOPE_TARGET_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.MAT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;

/**
 * 齐套检查实现。
 *
 * <p>四步：展开需求（{@link MesKitRequirementExpander}）、比对库存、逐行写快照、汇总结论并回写齐套状态。
 *
 * <p><b>齐不齐的判准是 {@code 可用量 + 本工单已预留 + 本工单已领用 >= 需求量}</b>，不是"可用量 >= 需求量"。
 * 原因是 WMS 的可用量口径为 {@code 在库 - 预留 - 冻结}：本工单提交领料后自己占的预留会把可用量压下去，
 * 只看可用量的话，同一张工单在领料后再查齐套会从"齐套"倒退成"缺料"，
 * 已经推到 {@code READY} 的工单再也回不去。这也正是 {@code mes_kit_snapshot} 要单独存
 * {@code reserved_qty} 与 {@code issued_qty} 两列的原因。
 *
 * <p>库存取数一共两次批量查（主料一次、替代料一次），不按行回查。
 */
@Service
public class MesKitCheckServiceImpl implements MesKitCheckService {

    /** 幂等键的段分隔符，格式见 08 文档的幂等键全表。 */
    private static final String KEY_SEPARATOR = ":";

    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private KitSnapshotMapper kitSnapshotMapper;
    @Resource
    private MaterialReqMapper materialReqMapper;
    @Resource
    private MaterialReqLineMapper materialReqLineMapper;
    @Resource
    private MesKitRequirementExpander requirementExpander;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesCommandService commandService;
    @Resource
    private MesWipTxnRecorder wipTxnRecorder;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private InventoryApi inventoryApi;
    @Resource
    private MatAltApi matAltApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MesKitCheckResultBO checkKit(MesKitCheckBO bo) {
        ProdOrderDO order = requireProdOrder(bo.getProdOrderId());
        // 检查编号先生成：它是幂等键的末段，也是本次快照全部行的归组键。每次调用都是新编号，
        // 因此齐套天然可以反复查；只有调用方显式传同一个键时才会命中幂等，返回上一次那份快照。
        String checkNo = codeService.generate(MesCodeRuleConstants.KIT_CHECK, null);
        MesCommandReqDTO command = buildCommand(bo, order.getOrderNo(), checkNo);
        AtomicReference<String> executedCheckNo = new AtomicReference<>();
        Long firstLineId = commandService.execute(MesCommandTypeEnum.KIT_CHECK, command, () -> {
            executedCheckNo.set(checkNo);
            return doCheck(order, bo, checkNo, command.getIdempotencyKey());
        });
        return buildResult(resolveCheckNo(executedCheckNo.get(), firstLineId), bo);
    }

    /**
     * 一次完整检查：展开需求、比对库存、写快照、汇总、回写齐套状态、齐套则推进工单到就绪。
     *
     * @return 本次快照的首行编号，供幂等命中时回溯到同一份快照；无需求行时返回 null
     */
    private Long doCheck(ProdOrderDO order, MesKitCheckBO bo, String checkNo, String idempotencyKey) {
        List<MesKitRequirementBO> requirements = expandRequirements(order, bo);
        List<KitSnapshotDO> lines = evaluate(order, bo, requirements, checkNo, LocalDateTime.now());
        lines.forEach(kitSnapshotMapper::insert);
        String result = summarize(lines);
        writeBackKitStatus(order, bo, result);
        advanceToReadyIfFullKit(order, result, idempotencyKey);
        return lines.isEmpty() ? null : lines.get(0).getId();
    }

    /**
     * 三档检查范围各自的需求来源。
     *
     * <p>父项数量按档次取：整单取工单计划数量，作业工单取该段计划数量，工序取该工序任务计划数量。
     * 让调用方传父项数量就等于让它决定检查多少件，快照便不再是工单的事实。
     */
    private List<MesKitRequirementBO> expandRequirements(ProdOrderDO order, MesKitCheckBO bo) {
        String scope = bo.getCheckScope();
        if (MesKitCheckScopeEnum.isOpTaskScope(scope)) {
            OpTaskDO opTask = requireOpTask(bo.getOpTaskId());
            return requirementExpander.expandByRouteOps(List.of(opTask.getRouteOpId()), opTask.getPlannedQty());
        }
        if (MesKitCheckScopeEnum.isWorkOrderScope(scope)) {
            WorkOrderDO workOrder = requireWorkOrder(bo.getWorkOrderId());
            // 一次查回本段全部工序任务，取路线工序集合后一次查回全部用料，不按工序逐个查
            List<Long> routeOpIds = opTaskMapper.selectListByWorkOrderIds(List.of(workOrder.getId()))
                    .stream().map(OpTaskDO::getRouteOpId).distinct().toList();
            return requirementExpander.expandByRouteOps(routeOpIds, workOrder.getPlannedQty());
        }
        if (!ObjUtil.equal(MesKitCheckScopeEnum.PROD_ORDER.name(), scope)) {
            throw exception(KIT_CHECK_SCOPE_TARGET_REQUIRED, scope);
        }
        // 整单齐套按下达时锁定的 BOM 展开。未锁版本（bom_id 为 0）时拒绝检查：
        // 空 BOM 展开不出需求行，会静默判成"齐套"并把工单推到就绪，车间到现场才发现没料
        if (order.getBomId() == null || MesConstants.NONE_ID.equals(order.getBomId())) {
            throw exception(BOM_NOT_EXISTS);
        }
        return requirementExpander.expandByBom(order.getBomId(), order.getQty());
    }

    /**
     * 逐行比对库存，产出待落库的快照行。
     *
     * <p>分两轮：第一轮按主料算出短缺，第二轮只对短缺行试替代料。替代料的可用量在两轮之间一次批量取回，
     * 不在循环里按行查（红线 5）。
     */
    private List<KitSnapshotDO> evaluate(ProdOrderDO order, MesKitCheckBO bo,
                                        List<MesKitRequirementBO> requirements,
                                        String checkNo, LocalDateTime checkTime) {
        List<KitSnapshotDO> lines = new ArrayList<>(requirements.size());
        if (CollUtil.isEmpty(requirements)) {
            return lines;
        }
        List<Long> matIds = requirements.stream().map(MesKitRequirementBO::getMatId).toList();
        Map<Long, MaterialRespDTO> materialMap = materialApi.getMaterialMap(matIds);
        Map<Long, BigDecimal> availableMap = availableQtyMap(matIds, bo.getWhId());
        OwnedQty owned = ownedQty(order.getId());

        int lineNo = MesConstants.FIRST_LINE_NO;
        for (MesKitRequirementBO requirement : requirements) {
            MaterialRespDTO material = materialMap.get(requirement.getMatId());
            if (material == null) {
                throw exception(MAT_NOT_EXISTS);
            }
            BigDecimal availQty = availableMap.getOrDefault(requirement.getMatId(), BigDecimal.ZERO);
            BigDecimal reservedQty = owned.reserved().getOrDefault(requirement.getMatId(), BigDecimal.ZERO);
            BigDecimal issuedQty = owned.issued().getOrDefault(requirement.getMatId(), BigDecimal.ZERO);
            BigDecimal shortQty = shortage(requirement.getReqQty(), availQty, reservedQty, issuedQty);
            lines.add(KitSnapshotDO.builder()
                    .checkNo(checkNo)
                    .checkScope(bo.getCheckScope())
                    .prodOrderId(order.getId())
                    .workOrderId(nvl(bo.getWorkOrderId()))
                    .opTaskId(nvl(bo.getOpTaskId()))
                    .lineNo(lineNo++)
                    .matId(requirement.getMatId())
                    .matCode(material.getCode())
                    .matName(material.getName())
                    .reqQty(requirement.getReqQty())
                    .availQty(availQty)
                    .reservedQty(reservedQty)
                    .issuedQty(issuedQty)
                    .shortQty(shortQty)
                    .altMatId(MesConstants.NONE_ID)
                    .result(shortQty.signum() > 0
                            ? MesKitStatusEnum.SHORTAGE.name() : MesKitStatusEnum.FULL_KIT.name())
                    .checkTime(checkTime)
                    .build());
        }
        applyAlternatives(lines, bo.getWhId());
        return lines;
    }

    /**
     * 短缺行改判替代料：按 {@code priority} 升序逐个试，第一个能补齐缺口的胜出。
     *
     * <p>判准是"替代料可用量 >= 缺口"而不是"替代料可用量 >= 全部需求量"：主料还有一部分可用时，
     * 车间本来就是主料与替代料混着用，要求替代料独立满足全部需求会把能开的工挡住。
     *
     * <p>命中后 {@code avail_qty} 仍记主料可用量——快照语义是"检查时点这个物料账上有多少"，
     * 换成替代料的可用量会让事后复盘看不出主料到底缺了多少。缺口由 {@code alt_mat_id} 非 0 表达。
     */
    private void applyAlternatives(List<KitSnapshotDO> lines, Long whId) {
        List<Long> shortMatIds = lines.stream()
                .filter(line -> line.getShortQty().signum() > 0)
                .map(KitSnapshotDO::getMatId).distinct().toList();
        if (CollUtil.isEmpty(shortMatIds)) {
            return;
        }
        Map<Long, List<MatAltRespDTO>> altMap = matAltApi.getAltMap(shortMatIds);
        List<Long> altMatIds = altMap.values().stream()
                .flatMap(List::stream).map(MatAltRespDTO::getAltMatId).distinct().toList();
        if (CollUtil.isEmpty(altMatIds)) {
            return;
        }
        Map<Long, BigDecimal> altAvailableMap = availableQtyMap(altMatIds, whId);
        for (KitSnapshotDO line : lines) {
            if (line.getShortQty().signum() <= 0) {
                continue;
            }
            for (MatAltRespDTO alt : altMap.getOrDefault(line.getMatId(), List.of())) {
                BigDecimal altAvailQty = altAvailableMap.getOrDefault(alt.getAltMatId(), BigDecimal.ZERO);
                if (altAvailQty.compareTo(line.getShortQty()) >= 0) {
                    line.setAltMatId(alt.getAltMatId())
                            .setShortQty(BigDecimal.ZERO)
                            .setResult(MesKitStatusEnum.FULL_KIT.name());
                    break;
                }
            }
        }
    }

    /**
     * 短缺量 = 需求量 − 可用量 − 本工单已预留 − 本工单已领用，负数归零。
     *
     * <p>后两项必须减：本工单自己占的预留已经把 WMS 可用量压低了，已领到车间的料在仓库账上也已经没有了，
     * 不加回来就会把自己刚备好的料算成缺料。
     */
    private BigDecimal shortage(BigDecimal reqQty, BigDecimal availQty,
                                BigDecimal reservedQty, BigDecimal issuedQty) {
        BigDecimal shortQty = reqQty.subtract(availQty).subtract(reservedQty).subtract(issuedQty);
        return shortQty.signum() > 0 ? shortQty : BigDecimal.ZERO;
    }

    /**
     * 批量取可用量，一次 SQL。
     *
     * <p>只统计合格库存：待检与不合格的料不能拿来开工，算进可用量等于让齐套骗人。
     * 仓库编号为空或 0 时不按仓库过滤，按全部仓库汇总。
     */
    private Map<Long, BigDecimal> availableQtyMap(List<Long> matIds, Long whId) {
        InventoryQueryReqDTO query = new InventoryQueryReqDTO();
        query.setMatIds(matIds);
        if (whId != null && !MesConstants.NONE_ID.equals(whId)) {
            query.setWhId(whId);
        }
        query.setQualifiedOnly(Boolean.TRUE);
        Map<Long, BigDecimal> availableMap = inventoryApi.getAvailableQtyMap(query);
        return availableMap == null ? Map.of() : availableMap;
    }

    /**
     * 本工单已经为各物料备下的数量：已预留（提交领料后被 WMS 留住的）与已领用（已出库到车间的）。
     *
     * <p>两次批量查（先查本工单的领料申请，再按申请编号集合查明细），不按行回查。
     *
     * <p>退料单不计：退料是把料还回仓库，还回去的量已经体现在可用量里，再算一遍就是重复计入。
     * 草稿与已取消的明细行也不计：它们还没有、或已经没有任何库存后果。
     */
    private OwnedQty ownedQty(Long prodOrderId) {
        List<MaterialReqDO> reqs = materialReqMapper.selectList(MaterialReqDO::getProdOrderId, prodOrderId)
                .stream().filter(req -> !MesMaterialReqTypeEnum.isReturn(req.getReqType())).toList();
        if (CollUtil.isEmpty(reqs)) {
            return new OwnedQty(Map.of(), Map.of());
        }
        List<MaterialReqLineDO> lines = materialReqLineMapper.selectList(MaterialReqLineDO::getReqId,
                reqs.stream().map(MaterialReqDO::getId).toList());
        Map<Long, BigDecimal> reserved = new HashMap<>();
        Map<Long, BigDecimal> issued = new HashMap<>();
        for (MaterialReqLineDO line : lines) {
            if (ObjUtil.equal(MesMaterialReqLineStatusEnum.DRAFT.name(), line.getStatus())
                    || ObjUtil.equal(MesMaterialReqLineStatusEnum.CANCELLED.name(), line.getStatus())) {
                continue;
            }
            BigDecimal processedQty = line.getProcessedQty() == null
                    ? BigDecimal.ZERO : line.getProcessedQty();
            BigDecimal pendingQty = line.getQty().subtract(processedQty);
            issued.merge(line.getMatId(), processedQty, BigDecimal::add);
            if (pendingQty.signum() > 0) {
                reserved.merge(line.getMatId(), pendingQty, BigDecimal::add);
            }
        }
        return new OwnedQty(reserved, issued);
    }

    /** 汇总整体结论：全齐 {@code FULL_KIT}、有齐有缺 {@code PARTIAL_KIT}、全缺 {@code SHORTAGE}。 */
    private String summarize(List<KitSnapshotDO> lines) {
        // 无需求行即齐套：倒冲件全被剔除后就是这种情况，没有需求自然没有缺料
        if (CollUtil.isEmpty(lines)) {
            return MesKitStatusEnum.FULL_KIT.name();
        }
        boolean anyShortage = lines.stream()
                .anyMatch(line -> MesKitStatusEnum.SHORTAGE.name().equals(line.getResult()));
        if (!anyShortage) {
            return MesKitStatusEnum.FULL_KIT.name();
        }
        boolean anyFullKit = lines.stream()
                .anyMatch(line -> MesKitStatusEnum.FULL_KIT.name().equals(line.getResult()));
        return anyFullKit ? MesKitStatusEnum.PARTIAL_KIT.name() : MesKitStatusEnum.SHORTAGE.name();
    }

    /**
     * 把结论回写到被检查的那一层。
     *
     * <p>{@code OP_TASK} 级不回写：{@code mes_op_task} 没有 {@code kit_status} 列，
     * 而把一道工序的结论写到它所属的作业工单上是错的——一道工序齐套不代表整段齐套。
     * 工序级结论留在快照里，开工门禁（T1-12）按领料行与快照判定，不读作业工单的齐套状态。
     *
     * <p>齐套状态不是状态机管的状态，用增量更新不带乐观锁：它是每次检查的结论快照，
     * 后一次检查覆盖前一次本来就是预期行为。
     */
    private void writeBackKitStatus(ProdOrderDO order, MesKitCheckBO bo, String result) {
        if (MesKitCheckScopeEnum.isOpTaskScope(bo.getCheckScope())) {
            return;
        }
        if (MesKitCheckScopeEnum.isWorkOrderScope(bo.getCheckScope())) {
            workOrderMapper.updateById(new WorkOrderDO().setId(bo.getWorkOrderId()).setKitStatus(result));
            return;
        }
        prodOrderMapper.updateById(new ProdOrderDO().setId(order.getId()).setKitStatus(result));
    }

    /**
     * 齐套时把生产工单从 {@code RELEASED} 推到 {@code READY}。
     *
     * <p>{@code READY} 的含义是"料已备好、可以开工"，这条推进是齐套动作对车间唯一可见的结果：
     * 少了它，工单永远停在 {@code RELEASED}，工序开工门禁那一关就永远过不去。
     *
     * <p>工单不在 {@code RELEASED} 时静默跳过而不报错：齐套是可反复执行的查询型动作，
     * 已经 {@code READY} 或已经开工的工单再查一次齐套是正常操作，为此让整个检查失败没有道理。
     * 目标状态仍由状态机裁决，不硬写 {@code READY}。
     *
     * <p>更新带 {@code version} 条件：并发下只有一个请求能把工单推走，另一个更新 0 行直接报冲突。
     */
    private void advanceToReadyIfFullKit(ProdOrderDO order, String result, String idempotencyKey) {
        if (!MesKitStatusEnum.FULL_KIT.name().equals(result)
                || !MesProdOrderStatusEnum.RELEASED.name().equals(order.getStatus())) {
            return;
        }
        String fromStatus = order.getStatus();
        String toStatus = MesProdOrderStateMachine.next(fromStatus, MesProdOrderStateMachine.ACTION_READY);
        int currentVersion = order.getVersion() == null ? MesConstants.INIT_VERSION : order.getVersion();
        LambdaUpdateWrapper<ProdOrderDO> wrapper = new LambdaUpdateWrapper<ProdOrderDO>()
                .eq(ProdOrderDO::getId, order.getId())
                .eq(ProdOrderDO::getVersion, currentVersion);
        if (prodOrderMapper.update(ProdOrderDO.builder()
                .status(toStatus).version(currentVersion + 1).build(), wrapper) == 0) {
            throw exception(PROD_ORDER_CONCURRENT_CONFLICT);
        }
        order.setStatus(toStatus).setVersion(currentVersion + 1);
        // 04 文档的九种事务类型里没有"备料就绪"这一项，取语义最近的 START（工单下达与开工都用它），
        // 由 from/to 状态区分是哪一步：下达是 DRAFT->RELEASED，这里是 RELEASED->READY
        wipTxnRecorder.record(new MesWipTxnBO()
                .setProdOrderId(order.getId())
                .setTxnType(MesWipTxnTypeEnum.START)
                .setFromStatus(fromStatus)
                .setToStatus(toStatus)
                .setBizType(MesBizTypeEnum.PROD_ORDER)
                .setBizNo(order.getOrderNo())
                .setIdempotencyKey(idempotencyKey)
                .setRemark("齐套检查通过，转就绪"));
    }

    /**
     * 定位本次结果对应的检查编号。
     *
     * <p>首次执行用本次生成的编号；幂等命中时业务动作没跑，用命令记录里存的首行编号反查上一次的检查编号，
     * 让重复提交拿到与首次完全相同的那份快照。上一次也没有需求行时无从反查，返回 null，
     * 结果为空行 + {@code FULL_KIT} 结论，与首次执行一致。
     */
    private String resolveCheckNo(String executedCheckNo, Long firstLineId) {
        if (executedCheckNo != null) {
            return executedCheckNo;
        }
        if (firstLineId == null) {
            return null;
        }
        KitSnapshotDO firstLine = kitSnapshotMapper.selectById(firstLineId);
        return firstLine == null ? null : firstLine.getCheckNo();
    }

    /** 按检查编号读回落库的快照行，组装对外结果。结论在这里重算一次，与落库时的汇总同一个方法。 */
    private MesKitCheckResultBO buildResult(String checkNo, MesKitCheckBO bo) {
        List<KitSnapshotDO> lines = checkNo == null ? List.of()
                : kitSnapshotMapper.selectList(KitSnapshotDO::getCheckNo, checkNo).stream()
                .sorted(Comparator.comparing(KitSnapshotDO::getLineNo)).toList();
        return new MesKitCheckResultBO()
                .setCheckNo(checkNo)
                .setCheckScope(bo.getCheckScope())
                .setProdOrderId(bo.getProdOrderId())
                .setWorkOrderId(nvl(bo.getWorkOrderId()))
                .setOpTaskId(nvl(bo.getOpTaskId()))
                .setResult(summarize(lines))
                .setCheckTime(lines.isEmpty() ? null : lines.get(0).getCheckTime())
                .setLines(lines);
    }

    /**
     * 组装幂等命令。
     *
     * <p>幂等键调用方给了以它为准，留空时按四段格式 {@code {来源模块}:{工单号}:KIT_CHECK:{检查编号}} 派生。
     * 末段放检查编号而不是行号：齐套是查询型动作，允许反复执行，键里放固定值会让第二次检查被当成重复提交拦掉。
     */
    private MesCommandReqDTO buildCommand(MesKitCheckBO bo, String orderNo, String checkNo) {
        Assert.notBlank(bo.getSourceModule(), "来源模块不能为空，它是幂等键的第一段");
        KitCheckCommand command = new KitCheckCommand();
        command.setSourceModule(bo.getSourceModule());
        command.setSourceBizNo(StrUtil.isBlank(bo.getSourceBizNo()) ? orderNo : bo.getSourceBizNo());
        command.setSourceBizLineNo(bo.getSourceBizLineNo());
        command.setIdempotencyKey(StrUtil.isBlank(bo.getIdempotencyKey())
                ? String.join(KEY_SEPARATOR, bo.getSourceModule(), orderNo,
                MesCommandTypeEnum.KIT_CHECK.name(), checkNo)
                : bo.getIdempotencyKey());
        return command;
    }

    private ProdOrderDO requireProdOrder(Long id) {
        ProdOrderDO order = id == null ? null : prodOrderMapper.selectById(id);
        if (order == null) {
            throw exception(PROD_ORDER_NOT_EXISTS);
        }
        return order;
    }

    private WorkOrderDO requireWorkOrder(Long id) {
        WorkOrderDO workOrder = isNone(id) ? null : workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw exception(WORK_ORDER_NOT_EXISTS);
        }
        return workOrder;
    }

    private OpTaskDO requireOpTask(Long id) {
        OpTaskDO opTask = isNone(id) ? null : opTaskMapper.selectById(id);
        if (opTask == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        return opTask;
    }

    /** 可选维度落 0 不落 null：快照表这两列是 NOT NULL。 */
    private Long nvl(Long value) {
        return value == null ? MesConstants.NONE_ID : value;
    }

    private boolean isNone(Long id) {
        return id == null || MesConstants.NONE_ID.equals(id);
    }

    /**
     * 本工单已经备下的数量，按物料汇总。
     *
     * @param reserved 已预留但还没出库的数量
     * @param issued   已经出库到车间的数量
     */
    private record OwnedQty(Map<Long, BigDecimal> reserved, Map<Long, BigDecimal> issued) {
    }

    /**
     * 幂等命令载体。
     *
     * <p>{@link MesCommandReqDTO} 是抽象类，这里给它一个最小实现，既不必新建公共 DTO，
     * 也不用把派生出的幂等键写回调用方的 BO——改调用方传进来的对象是隐蔽的副作用。
     */
    private static class KitCheckCommand extends MesCommandReqDTO {
    }

}
