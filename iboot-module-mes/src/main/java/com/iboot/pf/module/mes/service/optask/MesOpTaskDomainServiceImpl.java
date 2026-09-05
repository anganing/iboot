package com.iboot.pf.module.mes.service.optask;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStateMachine;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStateMachine;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStateMachine;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.MesBusinessRules;
import com.iboot.pf.module.mes.service.command.MesCommandService;
import com.iboot.pf.module.mes.service.opskiprecord.MesOpSkipDomainService;
import com.iboot.pf.module.mes.service.opskiprecord.bo.MesOpSkipBO;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskActionBO;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskFinishBO;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateChecker;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import com.iboot.pf.module.mes.service.prodreport.MesProgressPropagator;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import com.iboot.pf.module.mes.service.wiptxn.bo.MesWipTxnBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_FINISH_QTY_MISMATCH;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;

/**
 * 工序任务领域动作实现。
 *
 * <p>五个动作共用一套骨架：命令幂等 -&gt; 状态机裁决目标状态 -&gt; 带乐观锁条件落库 -&gt; 写在制流水。
 * 骨架抽在 {@link #execute} 与 {@link #advanceOpTask}；开工多了门禁与三级级联，完工多了数量校验与
 * 后序就绪，各自成 {@link #doStart} 与 {@link #doFinish}。
 *
 * <p>目标状态一律由状态机给出，方法签名里只有动作名。入参带目标状态就等于把状态机降级成建议。
 */
@Service
public class MesOpTaskDomainServiceImpl implements MesOpTaskDomainService {

    /** 幂等键的段分隔符，格式见 08 文档的幂等键全表。 */
    private static final String KEY_SEPARATOR = ":";
    /** 工序任务级动作没有行号，第四段固定 0。 */
    private static final String WHOLE_TASK_LINE_NO = "0";
    /** 级联流水的键后缀。一个开工动作最多改三级状态，每一级每一步各留一条流水，键必须互不相同。 */
    private static final String KEY_WORK_ORDER_READY = "WORK_ORDER_READY";
    private static final String KEY_WORK_ORDER_START = "WORK_ORDER_START";
    private static final String KEY_PROD_ORDER_READY = "PROD_ORDER_READY";
    private static final String KEY_PROD_ORDER_START = "PROD_ORDER_START";
    /** 后序就绪流水的键后缀，后面还要拼后序任务编号：一次完工可能推动并行组里的多道后序。 */
    private static final String KEY_NEXT_READY = "NEXT_READY";

    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private MesCommandService commandService;
    @Resource
    private MesWipTxnRecorder wipTxnRecorder;
    @Resource
    private MesStartGateChecker startGateChecker;
    @Resource
    private MesProgressPropagator progressPropagator;
    @Resource
    private MesOpSkipDomainService opSkipDomainService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startOpTask(MesOpTaskActionBO bo) {
        OpTaskDO task = requireOpTask(bo.getOpTaskId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.START_OP_TASK, task.getTaskNo());
        commandService.executeVoid(MesCommandTypeEnum.START_OP_TASK, command,
                () -> doStart(task, bo, command.getIdempotencyKey()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseOpTask(MesOpTaskActionBO bo) {
        execute(bo, MesCommandTypeEnum.PAUSE_OP_TASK, MesOpTaskStateMachine.ACTION_PAUSE,
                MesWipTxnTypeEnum.PAUSE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resumeOpTask(MesOpTaskActionBO bo) {
        execute(bo, MesCommandTypeEnum.RESUME_OP_TASK, MesOpTaskStateMachine.ACTION_RESUME,
                MesWipTxnTypeEnum.RESUME);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishOpTask(MesOpTaskFinishBO bo) {
        OpTaskDO task = requireOpTask(bo.getOpTaskId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.FINISH_OP_TASK, task.getTaskNo());
        commandService.executeVoid(MesCommandTypeEnum.FINISH_OP_TASK, command,
                () -> doFinish(task, bo, command.getIdempotencyKey()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void skipOpTask(MesOpSkipBO bo) {
        OpTaskDO task = requireOpTask(bo.getOpTaskId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.SKIP_OP_TASK, task.getTaskNo());
        commandService.executeVoid(MesCommandTypeEnum.SKIP_OP_TASK, command,
                () -> doSkip(task, bo, command.getIdempotencyKey()));
    }

    /**
     * 跳序：策略分流与跳过记录交给跳序域，后序推进留在本类。
     *
     * <p>为什么不整个搬到跳序域：{@link #advanceNextTasks} 已经处理了并行组的两处语义
     * （本工序在组里时同组全跳完才推后序、后序在组里时整组一起推，且组的范围按生产工单取），
     * 在跳序域另写一份必然漏掉其中之一。跳序在这一点上与完工完全同构——都是"本工序不再占着后序"。
     *
     * <p>{@code APPROVAL_REQUIRED} 时跳序域返回未生效，这里连后序都不推：审批没过，这道工序还在链上。
     */
    private void doSkip(OpTaskDO task, MesOpSkipBO bo, String idempotencyKey) {
        WorkOrderDO workOrder = requireWorkOrder(task.getWorkOrderId());
        if (!opSkipDomainService.skip(task, workOrder.getProdOrderId(), bo, idempotencyKey)) {
            return;
        }
        advanceNextTasks(task, workOrder.getProdOrderId(), idempotencyKey);
    }

    /**
     * 开工的四件事：过门禁、推状态与实际开始时间、留痕、级联推进上两级。
     *
     * <p>门禁跑在状态推进之前，且整个动作在一个事务里：门禁不通过时一行都不许改，
     * 否则工序停在 {@code RUNNING} 而料还没到工位。
     *
     * <p>幂等命中时本方法根本不执行，因此门禁也不会重跑——开工已经发生过，再判一次没有意义，
     * 反而会因为"资源已被自己占用"这类原因把重复请求判成失败。
     */
    private void doStart(OpTaskDO task, MesOpTaskActionBO bo, String idempotencyKey) {
        MesStartGateContext gateContext = new MesStartGateContext();
        gateContext.setOperatorId(bo.getOperatorId());
        startGateChecker.check(task, gateContext);

        String fromStatus = task.getStatus();
        String toStatus = MesOpTaskStateMachine.next(fromStatus, MesOpTaskStateMachine.ACTION_START);
        WorkOrderDO workOrder = requireWorkOrder(task.getWorkOrderId());
        LocalDateTime startTime = LocalDateTime.now();
        advanceOpTask(task, OpTaskDO.builder().status(toStatus).actualStartTime(startTime).build());
        task.setStatus(toStatus).setActualStartTime(startTime);

        recordTaskTxn(task, workOrder.getProdOrderId(), MesWipTxnTypeEnum.START,
                fromStatus, toStatus, null, idempotencyKey, bo.getReason());
        cascadeStart(task, workOrder, startTime, idempotencyKey);
    }

    /**
     * 完工的四件事：校验数量、推状态与实际结束时间、留痕、把后序工序推到就绪。
     *
     * <p>数量校验在状态推进之前：先改状态再发现数量不对，工序已经落成 {@code DONE} 而
     * {@code DONE} 是终态，没有回退边可走。
     */
    private void doFinish(OpTaskDO task, MesOpTaskFinishBO bo, String idempotencyKey) {
        String fromStatus = task.getStatus();
        String toStatus = MesOpTaskStateMachine.next(fromStatus, MesOpTaskStateMachine.ACTION_FINISH);
        validateDeclaredQty(task, bo);
        // 累计合格 + 累计报废 = 计划数。累计值只由报工推进，完工只校验它凑齐了，不自己写数量
        MesBusinessRules.validateFinishQty(task.getPlannedQty(), task.getCompletedQty(), task.getScrapQty());

        WorkOrderDO workOrder = requireWorkOrder(task.getWorkOrderId());
        LocalDateTime endTime = LocalDateTime.now();
        advanceOpTask(task, OpTaskDO.builder().status(toStatus).actualEndTime(endTime).build());
        task.setStatus(toStatus).setActualEndTime(endTime);

        // COMPLETE 的 qty 按 04 文档记"累计合格数"，不是状态类事务的 0
        recordTaskTxn(task, workOrder.getProdOrderId(), MesWipTxnTypeEnum.COMPLETE,
                fromStatus, toStatus, task.getCompletedQty(), idempotencyKey, bo.getReason());
        advanceNextTasks(task, workOrder.getProdOrderId(), idempotencyKey);
        // 上卷：本段全部工序完工时作业工单收尾，末道工序完工且数量达标时生产工单收尾。
        // 数量增量为零——完工不写数量，只是让上卷按新的工序状态重算一遍。这一步必须由完工触发：
        // "本段全部工序 DONE" 只可能在完工的那一刻成立，而最后一次报工发生在完工之前
        progressPropagator.propagate(task, MesConstants.ZERO_QTY, MesConstants.ZERO_QTY, idempotencyKey);
    }

    /**
     * 对账声明校验：调用方报上来的数量必须与工序任务上的累计值一致。
     *
     * <p>两个字段都留空时跳过。只给一个视为不一致：单给合格数无法判断报废数是"确实为零"还是"没填"。
     */
    private void validateDeclaredQty(OpTaskDO task, MesOpTaskFinishBO bo) {
        if (bo.getQualifiedQty() == null && bo.getScrapQty() == null) {
            return;
        }
        boolean matched = bo.getQualifiedQty() != null && bo.getScrapQty() != null
                && bo.getQualifiedQty().compareTo(task.getCompletedQty()) == 0
                && bo.getScrapQty().compareTo(task.getScrapQty()) == 0;
        if (!matched) {
            throw exception(OP_TASK_FINISH_QTY_MISMATCH, bo.getQualifiedQty(), bo.getScrapQty(),
                    task.getCompletedQty(), task.getScrapQty());
        }
    }

    /**
     * 无附带效果的状态动作骨架：命令幂等、状态机裁决、带乐观锁条件落库、写在制流水。
     *
     * <p>暂停与恢复走这里。作业工单要多查一次是为了拿 {@code prod_order_id}——在制流水的主维度是
     * 生产工单，而 {@code mes_op_task} 上没有这一列。
     */
    private void execute(MesOpTaskActionBO bo, MesCommandTypeEnum commandType, String action,
                         MesWipTxnTypeEnum txnType) {
        OpTaskDO task = requireOpTask(bo.getOpTaskId());
        MesCommandReqDTO command = buildCommand(bo, commandType, task.getTaskNo());
        commandService.executeVoid(commandType, command, () -> {
            String fromStatus = task.getStatus();
            String toStatus = MesOpTaskStateMachine.next(fromStatus, action);
            WorkOrderDO workOrder = requireWorkOrder(task.getWorkOrderId());
            advanceOpTask(task, OpTaskDO.builder().status(toStatus).build());
            task.setStatus(toStatus);
            // 状态类事务数量记 0，由 Recorder 兜底
            recordTaskTxn(task, workOrder.getProdOrderId(), txnType, fromStatus, toStatus, null,
                    command.getIdempotencyKey(), bo.getReason());
        });
    }

    /**
     * 带乐观锁条件推进工序任务，并把新版本号同步回内存对象。
     *
     * <p>{@code mes_op_task} 是全平台最热的一张表：APS 写计划与资源、MES 写状态、报工累加数量、
     * 质检以它为对象。少了 {@code WHERE version = 读到的版本}，两个并发动作会各自基于同一份旧值写回，
     * 后写的把前写的整条覆盖掉——并发开工与报工同时发生时，丢的是报工数量。
     */
    private void advanceOpTask(OpTaskDO task, OpTaskDO update) {
        int currentVersion = task.getVersion() == null ? MesConstants.INIT_VERSION : task.getVersion();
        update.setVersion(currentVersion + 1);
        LambdaUpdateWrapper<OpTaskDO> wrapper = new LambdaUpdateWrapper<OpTaskDO>()
                .eq(OpTaskDO::getId, task.getId())
                .eq(OpTaskDO::getVersion, currentVersion);
        if (opTaskMapper.update(update, wrapper) == 0) {
            throw exception(OP_TASK_CONCURRENT_CONFLICT);
        }
        task.setVersion(currentVersion + 1);
    }

    /**
     * 级联推进：一道工序开工，作业工单与生产工单一起进 {@code RUNNING}。
     *
     * <p>两级都按状态机分步走，**不硬写目标状态、也不假设它们已经就绪**：
     * <ul>
     *     <li>作业工单 {@code PENDING} 时先 {@code ready} 再 {@code start}。状态机里
     *     <b>没有 PENDING -&gt; RUNNING 这条边</b>，一步推不过去。{@code PENDING} 是常态而不是例外：
     *     下达只把作业工单摊成 {@code PENDING}，齐套检查只回写它的 {@code kit_status} 与生产工单的状态，
     *     从没有人推过作业工单的状态——车间派工按 {@code READY} 筛，因此这批单在开工前根本筛不出来</li>
     *     <li>生产工单 {@code RELEASED} 时同样先 {@code ready} 再 {@code start}。齐套通过时齐套动作会把它
     *     推到 {@code READY}，但齐套是可反复执行的可选动作，跳过它直接领料开工时工单还停在 {@code RELEASED}。
     *     此时开工已经过了门禁的"必发料已发齐"一关，正是 {@code RELEASED -> READY} 的两个条件之一
     *     （"齐套通过 <b>或</b> 已发料"），补这一步不是绕过齐套</li>
     * </ul>
     *
     * <p>已经 {@code RUNNING}（本段第二道工序开工）、处于 {@code HOLD} 或终态时静默跳过：
     * 开工是车间现场动作，不该因为上级状态已经对了而失败。
     */
    private void cascadeStart(OpTaskDO task, WorkOrderDO workOrder,
                              LocalDateTime startTime, String idempotencyKey) {
        if (MesWorkOrderStatusEnum.PENDING.name().equals(workOrder.getStatus())) {
            advanceWorkOrder(workOrder, task, MesWorkOrderStateMachine.ACTION_READY, null,
                    derive(idempotencyKey, KEY_WORK_ORDER_READY));
        }
        if (MesWorkOrderStatusEnum.READY.name().equals(workOrder.getStatus())) {
            advanceWorkOrder(workOrder, task, MesWorkOrderStateMachine.ACTION_START, startTime,
                    derive(idempotencyKey, KEY_WORK_ORDER_START));
        }
        ProdOrderDO order = requireProdOrder(workOrder.getProdOrderId());
        if (MesProdOrderStatusEnum.RELEASED.name().equals(order.getStatus())) {
            advanceProdOrder(order, task, MesProdOrderStateMachine.ACTION_READY, null,
                    derive(idempotencyKey, KEY_PROD_ORDER_READY));
        }
        if (MesProdOrderStatusEnum.READY.name().equals(order.getStatus())) {
            advanceProdOrder(order, task, MesProdOrderStateMachine.ACTION_START, startTime,
                    derive(idempotencyKey, KEY_PROD_ORDER_START));
        }
    }

    /**
     * 推进作业工单一步。
     *
     * <p>{@code startTime} 只在转 {@code RUNNING} 那一步给：{@code actual_start_time} 的语义是
     * "本段首道工序开工的时刻"，第二道工序开工时作业工单已经 {@code RUNNING}，本方法不会再被调到，
     * 因此首道那次写进去的值不会被后面的覆盖。
     */
    private void advanceWorkOrder(WorkOrderDO workOrder, OpTaskDO task, String action,
                                  LocalDateTime startTime, String idempotencyKey) {
        String fromStatus = workOrder.getStatus();
        String toStatus = MesWorkOrderStateMachine.next(fromStatus, action);
        int currentVersion = workOrder.getVersion() == null
                ? MesConstants.INIT_VERSION : workOrder.getVersion();
        LambdaUpdateWrapper<WorkOrderDO> wrapper = new LambdaUpdateWrapper<WorkOrderDO>()
                .eq(WorkOrderDO::getId, workOrder.getId())
                .eq(WorkOrderDO::getVersion, currentVersion);
        if (workOrderMapper.update(WorkOrderDO.builder().status(toStatus).actualStartTime(startTime)
                .version(currentVersion + 1).build(), wrapper) == 0) {
            throw exception(WORK_ORDER_CONCURRENT_CONFLICT);
        }
        workOrder.setStatus(toStatus).setVersion(currentVersion + 1);
        if (startTime != null) {
            workOrder.setActualStartTime(startTime);
        }
        recordCascadeTxn(workOrder.getProdOrderId(), workOrder.getId(), task,
                fromStatus, toStatus, MesBizTypeEnum.WORK_ORDER, workOrder.getWorkOrderNo(), idempotencyKey);
    }

    /** 推进生产工单一步，口径与 {@link #advanceWorkOrder} 一致。 */
    private void advanceProdOrder(ProdOrderDO order, OpTaskDO task, String action,
                                  LocalDateTime startTime, String idempotencyKey) {
        String fromStatus = order.getStatus();
        String toStatus = MesProdOrderStateMachine.next(fromStatus, action);
        int currentVersion = order.getVersion() == null ? MesConstants.INIT_VERSION : order.getVersion();
        LambdaUpdateWrapper<ProdOrderDO> wrapper = new LambdaUpdateWrapper<ProdOrderDO>()
                .eq(ProdOrderDO::getId, order.getId())
                .eq(ProdOrderDO::getVersion, currentVersion);
        if (prodOrderMapper.update(ProdOrderDO.builder().status(toStatus).actualStartTime(startTime)
                .version(currentVersion + 1).build(), wrapper) == 0) {
            throw exception(PROD_ORDER_CONCURRENT_CONFLICT);
        }
        order.setStatus(toStatus).setVersion(currentVersion + 1);
        if (startTime != null) {
            order.setActualStartTime(startTime);
        }
        recordCascadeTxn(order.getId(), MesConstants.NONE_ID, task,
                fromStatus, toStatus, MesBizTypeEnum.PROD_ORDER, order.getOrderNo(), idempotencyKey);
    }

    /**
     * 后序就绪：沿 {@code next_task_id} 把后序工序从 {@code PENDING} 推到 {@code READY}。
     *
     * <p>并行组在这里有两处语义（04 文档"工序串联规则"）：
     * <ol>
     *     <li><b>本工序在并行组里时，同组全部完工或跳序才推后序</b>。后序要的是整组的产出，
     *     只完成一道就往下走，后面那道工序拿到的是半成品</li>
     *     <li><b>后序在并行组里时，整组一起推</b>。{@code next_task_id} 是单值列，指向的是并行步骤的
     *     代表工序（组内 seq 最小的那道），只推代表的话组内其它工序永远停在 {@code PENDING}，
     *     它们的 {@code start} 会被状态机直接拒掉，那道活谁也干不了</li>
     * </ol>
     *
     * <p>并行组的范围是<b>同一生产工单</b>，与下达时的分组口径一致：
     * {@code MesOrderExpandHelper.groupIntoSteps} 按 {@code parallel_group} 在整条路线内分组，
     * 同组工序会因工作中心不同落到不同的作业工单（04 文档的"喷漆 ‖ 标识"正是这种情形）。
     * 按作业工单取组会把这种最典型的并行组切成两个单元素组，两处语义同时失效。
     */
    private void advanceNextTasks(OpTaskDO task, Long prodOrderId, String idempotencyKey) {
        if (isNone(task.getNextTaskId())) {
            return;
        }
        List<OpTaskDO> prodOrderTasks = loadProdOrderTasks(prodOrderId);
        if (!allDoneOrSkipped(sameGroup(task, prodOrderTasks))) {
            return;
        }
        OpTaskDO next = prodOrderTasks.stream()
                .filter(item -> task.getNextTaskId().equals(item.getId())).findFirst().orElse(null);
        if (next == null) {
            // 后序被删或数据不全时不拦：完工已经发生，为一条脏数据把它回滚掉没有道理
            return;
        }
        for (OpTaskDO target : sameGroup(next, prodOrderTasks)) {
            if (!MesOpTaskStatusEnum.PENDING.name().equals(target.getStatus())) {
                // 已被 APS 下达计划推到 READY、或已开工，不重复推
                continue;
            }
            String fromStatus = target.getStatus();
            String toStatus = MesOpTaskStateMachine.next(fromStatus, MesOpTaskStateMachine.ACTION_PREV_DONE);
            advanceOpTask(target, OpTaskDO.builder().status(toStatus).build());
            target.setStatus(toStatus);
            // MOVE 是九种事务类型里唯一表达工序间流转的，两个专用列正好记流出与流入工序，
            // qty 记流转数量，也就是本工序的累计合格数
            wipTxnRecorder.record(new MesWipTxnBO()
                    .setProdOrderId(prodOrderId)
                    .setWorkOrderId(target.getWorkOrderId())
                    .setOpTaskId(target.getId())
                    .setTxnType(MesWipTxnTypeEnum.MOVE)
                    .setQty(task.getCompletedQty())
                    .setFromStatus(fromStatus)
                    .setToStatus(toStatus)
                    .setBizType(MesBizTypeEnum.OP_TASK)
                    .setBizNo(target.getTaskNo())
                    .setFromOpTaskId(task.getId())
                    .setToOpTaskId(target.getId())
                    .setIdempotencyKey(derive(idempotencyKey, KEY_NEXT_READY + KEY_SEPARATOR + target.getId()))
                    .setRemark("前序工序（" + task.getOpName() + "）完工带动"));
        }
    }

    /**
     * 取同一并行组的工序任务，无并行组时只有自己。
     *
     * <p>组内查不到任何记录时退化成只有自己：{@code parallel_group} 是个自由字符串，
     * 主数据配错时不该让完工动作整个失败。
     */
    private List<OpTaskDO> sameGroup(OpTaskDO task, List<OpTaskDO> prodOrderTasks) {
        if (StrUtil.isBlank(task.getParallelGroup())) {
            return List.of(task);
        }
        List<OpTaskDO> group = prodOrderTasks.stream()
                .filter(item -> task.getParallelGroup().equals(item.getParallelGroup())).toList();
        return group.isEmpty() ? List.of(task) : group;
    }

    /** 只认已完工与已跳过，与开工门禁 {@code PrevDoneChecker} 的口径一致：取消不算完成。 */
    private boolean allDoneOrSkipped(List<OpTaskDO> tasks) {
        return tasks.stream().allMatch(item -> MesOpTaskStatusEnum.DONE.name().equals(item.getStatus())
                || MesOpTaskStatusEnum.SKIPPED.name().equals(item.getStatus()));
    }

    /**
     * 取一张生产工单下的全部工序任务。
     *
     * <p>两次批量查（按生产工单查作业工单、按作业工单集合查工序任务），不按行回查：
     * {@code mes_op_task} 上没有 {@code prod_order_id} 列，而并行组要跨作业工单看。
     */
    private List<OpTaskDO> loadProdOrderTasks(Long prodOrderId) {
        List<Long> workOrderIds = workOrderMapper.selectListByProdOrderIds(List.of(prodOrderId))
                .stream().map(WorkOrderDO::getId).toList();
        return workOrderIds.isEmpty() ? List.of() : opTaskMapper.selectListByWorkOrderIds(workOrderIds);
    }

    /**
     * 写一条工序任务级在制流水。
     *
     * <p>不走 Recorder 的 {@code recordStatusChange} 便捷方法，因为暂停原因这类可读说明要落 remark、
     * 完工要落累计合格数，而那个方法两者都没有入参。{@code prodOrderId} 由调用方传进来，
     * 避免 Recorder 内部再按作业工单反查一次。
     */
    private void recordTaskTxn(OpTaskDO task, Long prodOrderId, MesWipTxnTypeEnum txnType, String fromStatus,
                               String toStatus, BigDecimal qty, String idempotencyKey, String remark) {
        wipTxnRecorder.record(new MesWipTxnBO()
                .setProdOrderId(prodOrderId)
                .setWorkOrderId(task.getWorkOrderId())
                .setOpTaskId(task.getId())
                .setTxnType(txnType)
                .setQty(qty)
                .setFromStatus(fromStatus)
                .setToStatus(toStatus)
                .setBizType(MesBizTypeEnum.OP_TASK)
                .setBizNo(task.getTaskNo())
                .setIdempotencyKey(idempotencyKey)
                .setRemark(remark));
    }

    /**
     * 写一条级联流水，记的是作业工单或生产工单这一级的状态变更。
     *
     * <p>三个维度全部落上：{@code op_task_id} 记的是"哪道工序带动的"，这是读流水时最需要的一条线索；
     * 变更主体由 {@code bizType} 与 {@code bizNo} 指明，{@code from/to} 是该主体的状态。
     * 数量记 0，级联只改状态。
     */
    private void recordCascadeTxn(Long prodOrderId, Long workOrderId, OpTaskDO task, String fromStatus,
                                  String toStatus, MesBizTypeEnum bizType, String bizNo, String idempotencyKey) {
        wipTxnRecorder.record(new MesWipTxnBO()
                .setProdOrderId(prodOrderId)
                .setWorkOrderId(workOrderId)
                .setOpTaskId(task.getId())
                .setTxnType(MesWipTxnTypeEnum.START)
                .setFromStatus(fromStatus)
                .setToStatus(toStatus)
                .setBizType(bizType)
                .setBizNo(bizNo)
                .setIdempotencyKey(idempotencyKey)
                .setRemark("工序（" + task.getOpName() + "）开工带动"));
    }

    /**
     * 组装幂等命令。
     *
     * <p>幂等键调用方给了以它为准，留空时按四段格式 {@code {来源模块}:{工序任务号}:{命令类型}:0} 派生，
     * 与 08 文档的幂等键全表一致。动作段取 {@link MesCommandTypeEnum} 的枚举名而不是字面量：
     * 键会落进 {@code mes_command_record}，字面量拼出来的历史键在命令改名后就与枚举对不上。
     */
    private MesCommandReqDTO buildCommand(MesCommandReqDTO source, MesCommandTypeEnum commandType, String taskNo) {
        // 来源模块是幂等键的第一段，也是排查"这条命令谁下的"的唯一线索，漏传属于调用方契约违约
        Assert.notBlank(source.getSourceModule(), "来源模块不能为空，它是幂等键的第一段");
        TaskCommand command = new TaskCommand();
        command.setSourceModule(source.getSourceModule());
        command.setSourceBizNo(StrUtil.isBlank(source.getSourceBizNo()) ? taskNo : source.getSourceBizNo());
        command.setSourceBizLineNo(source.getSourceBizLineNo());
        command.setIdempotencyKey(StrUtil.isBlank(source.getIdempotencyKey())
                ? String.join(KEY_SEPARATOR, source.getSourceModule(), taskNo,
                commandType.name(), WHOLE_TASK_LINE_NO)
                : source.getIdempotencyKey());
        return command;
    }

    /** 派生流水键：一个动作要写多条流水时，各自带自己的后缀，避免被 Recorder 判成重复留痕。 */
    private String derive(String idempotencyKey, String suffix) {
        return idempotencyKey + KEY_SEPARATOR + suffix;
    }

    private boolean isNone(Long id) {
        return id == null || MesConstants.NONE_ID.equals(id);
    }

    private OpTaskDO requireOpTask(Long id) {
        OpTaskDO task = id == null ? null : opTaskMapper.selectById(id);
        if (task == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        return task;
    }

    private WorkOrderDO requireWorkOrder(Long id) {
        WorkOrderDO workOrder = id == null ? null : workOrderMapper.selectById(id);
        if (workOrder == null) {
            throw exception(WORK_ORDER_NOT_EXISTS);
        }
        return workOrder;
    }

    private ProdOrderDO requireProdOrder(Long id) {
        ProdOrderDO order = id == null ? null : prodOrderMapper.selectById(id);
        if (order == null) {
            throw exception(PROD_ORDER_NOT_EXISTS);
        }
        return order;
    }

    /**
     * 幂等命令载体。
     *
     * <p>{@link MesCommandReqDTO} 是抽象类，工序域给它一个最小实现：既不必新建公共 DTO，
     * 也不用把派生出来的幂等键写回调用方的 BO——改调用方传进来的对象是隐蔽的副作用。
     */
    private static class TaskCommand extends MesCommandReqDTO {
    }

}
