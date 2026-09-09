package com.iboot.pf.module.mes.service.prodreport;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStateMachine;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStateMachine;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.prodorder.MesFinishInboundService;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import com.iboot.pf.module.mes.service.wiptxn.bo.MesWipTxnBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;

/**
 * 三级进度推进。报工与完工都经这里把进度往上卷，是 {@code completed_qty} / {@code scrap_qty} 的唯一写入路径。
 *
 * <p><b>口径是这个类的全部价值，写错一处整条进度链就不可信</b>（见
 * {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"数量推进口径"）：
 * <table>
 *     <tr><th>层级</th><th>completed_qty</th><th>scrap_qty</th></tr>
 *     <tr><td>工序任务</td><td>{@code += qty_good}（累加）</td><td>{@code += qty_scrap}（累加）</td></tr>
 *     <tr><td>作业工单</td><td>本段<b>最后一道实际执行的工序</b>的值</td><td>无此列，不推进</td></tr>
 *     <tr><td>生产工单</td><td>整单<b>最后一道实际执行的工序</b>的值</td><td>各工序之和</td></tr>
 * </table>
 *
 * <p>"最后一道实际执行的工序"指从末道开始、整层全是 SKIPPED 就退到前一个 seq 的那批：
 * 被跳过的工序不出产出，把它当末道会把产出清零。作业工单与生产工单的收尾判定一样容忍跳序——
 * 末道层全部 DONE（数量达标）或 SKIPPED 都算干完，否则末道被跳过的工单永远关不掉，预留永久挂着。
 *
 * <p>为什么工单取末道工序而不是各工序相加：流水线上 100 个件依次经过 5 道工序，每道都报 100，
 * 产出仍然是 100 个而不是 500 个。相加得到的是"工时口径的加工次数"，不是产出。
 * 报废反过来要相加：每道工序报废掉的都是不同的件，加起来才是这张工单一共损失了多少。
 *
 * <p>上卷用<b>重算</b>而不是增量累加，这让取消报工不需要另写一套反向公式：把工序级的数量扣回去，
 * 再按同一套规则重算一遍，两级自然回到正确值。
 */
@Component
public class MesProgressPropagator {

    /** 上卷流水的键后缀。 */
    private static final String KEY_SEPARATOR = ":";
    private static final String KEY_WORK_ORDER_DONE = "WORK_ORDER_DONE";
    private static final String KEY_PROD_ORDER_DONE = "PROD_ORDER_DONE";

    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private MesWipTxnRecorder wipTxnRecorder;
    @Resource
    private MesFinishInboundService finishInboundService;

    /**
     * 把一次数量变化推进到三级，并按数量与状态判定作业工单、生产工单是否完工。
     *
     * @param task           工序任务，方法内会把新数量与新版本号同步回这个对象
     * @param deltaGood      合格数增量，取消报工时传负数
     * @param deltaScrap     报废数增量，取消报工时传负数
     * @param idempotencyKey 触发动作的幂等键，上卷产生的流水按它派生
     */
    @Transactional(rollbackFor = Exception.class)
    public void propagate(OpTaskDO task, BigDecimal deltaGood, BigDecimal deltaScrap, String idempotencyKey) {
        if (isNonZero(deltaGood) || isNonZero(deltaScrap)) {
            accumulate(task, deltaGood, deltaScrap);
        }
        // 两次批量查取全单工序：作业工单要本段的末道工序，生产工单要整单的末道工序与全部报废数
        WorkOrderDO workOrder = requireWorkOrder(task.getWorkOrderId());
        ProdOrderDO order = requireProdOrder(workOrder.getProdOrderId());
        List<Long> workOrderIds = workOrderMapper.selectListByProdOrderIds(List.of(order.getId()))
                .stream().map(WorkOrderDO::getId).toList();
        List<OpTaskDO> orderTasks = opTaskMapper.selectListByWorkOrderIds(workOrderIds);

        rollUpWorkOrder(workOrder, orderTasks, task, idempotencyKey);
        rollUpProdOrder(order, orderTasks, task, idempotencyKey);
    }

    /**
     * 数量达标判定：累计合格 + 累计报废 &gt;= 计划数。
     *
     * <p>判的是"这道工序的活干完了没有"，与完工动作要求的严格相等不是一回事：
     * 完工是人点的、要求账对得上；达标只是个判定，用来决定生产工单能不能收尾。
     */
    public boolean qtyReached(OpTaskDO task) {
        return nvl(task.getCompletedQty()).add(nvl(task.getScrapQty()))
                .compareTo(nvl(task.getPlannedQty())) >= 0;
    }

    /**
     * 工序任务累加，带乐观锁条件。
     *
     * <p>累加而不是覆盖：一道工序分批报 30 / 30 / 40，覆盖会让最后剩下 40。
     * 取消报工传负增量走同一条路径，不另写一套反向公式。
     */
    private void accumulate(OpTaskDO task, BigDecimal deltaGood, BigDecimal deltaScrap) {
        BigDecimal completedQty = nvl(task.getCompletedQty()).add(nvl(deltaGood));
        BigDecimal scrapQty = nvl(task.getScrapQty()).add(nvl(deltaScrap));
        int currentVersion = task.getVersion() == null ? MesConstants.INIT_VERSION : task.getVersion();
        LambdaUpdateWrapper<OpTaskDO> wrapper = new LambdaUpdateWrapper<OpTaskDO>()
                .eq(OpTaskDO::getId, task.getId())
                .eq(OpTaskDO::getVersion, currentVersion);
        if (opTaskMapper.update(OpTaskDO.builder().completedQty(completedQty).scrapQty(scrapQty)
                .version(currentVersion + 1).build(), wrapper) == 0) {
            throw exception(OP_TASK_CONCURRENT_CONFLICT);
        }
        task.setCompletedQty(completedQty).setScrapQty(scrapQty).setVersion(currentVersion + 1);
    }

    /**
     * 作业工单上卷：数量取本段最后一道实际执行的工序，本段全部完工或跳序时置 {@code DONE}。
     *
     * <p>{@code mes_work_order} 没有 {@code scrap_qty} 列，因此报废在这一级不落地，
     * 由生产工单那一级按各工序之和汇总。
     */
    private void rollUpWorkOrder(WorkOrderDO workOrder, List<OpTaskDO> orderTasks,
                                 OpTaskDO trigger, String idempotencyKey) {
        List<OpTaskDO> segmentTasks = orderTasks.stream()
                .filter(item -> workOrder.getId().equals(item.getWorkOrderId())).toList();
        BigDecimal completedQty = lastExecutedOpCompletedQty(segmentTasks);
        boolean done = MesWorkOrderStatusEnum.RUNNING.name().equals(workOrder.getStatus())
                && allDoneOrSkipped(segmentTasks);
        String toStatus = done
                ? MesWorkOrderStateMachine.next(workOrder.getStatus(), MesWorkOrderStateMachine.ACTION_DONE)
                : null;
        if (completedQty.compareTo(nvl(workOrder.getCompletedQty())) == 0 && toStatus == null) {
            // 数量与状态都没变化就不更新：无谓的版本自增会让并发报工互相撞乐观锁
            return;
        }
        LocalDateTime endTime = done ? LocalDateTime.now() : null;
        int currentVersion = workOrder.getVersion() == null
                ? MesConstants.INIT_VERSION : workOrder.getVersion();
        LambdaUpdateWrapper<WorkOrderDO> wrapper = new LambdaUpdateWrapper<WorkOrderDO>()
                .eq(WorkOrderDO::getId, workOrder.getId())
                .eq(WorkOrderDO::getVersion, currentVersion);
        if (workOrderMapper.update(WorkOrderDO.builder().completedQty(completedQty).status(toStatus)
                .actualEndTime(endTime).version(currentVersion + 1).build(), wrapper) == 0) {
            throw exception(WORK_ORDER_CONCURRENT_CONFLICT);
        }
        workOrder.setCompletedQty(completedQty).setVersion(currentVersion + 1);
        if (toStatus == null) {
            return;
        }
        String fromStatus = workOrder.getStatus();
        workOrder.setStatus(toStatus).setActualEndTime(endTime);
        recordDoneTxn(workOrder.getProdOrderId(), workOrder.getId(), trigger, completedQty,
                fromStatus, toStatus, MesBizTypeEnum.WORK_ORDER, workOrder.getWorkOrderNo(),
                idempotencyKey + KEY_SEPARATOR + KEY_WORK_ORDER_DONE);
    }

    /**
     * 生产工单上卷：{@code completed_qty} 取整单最后一道实际执行的工序，{@code scrap_qty} 取各工序之和，
     * 最后一道实际执行的工序全部完工（数量达标）或已跳过时置 {@code DONE}。
     *
     * <p>收尾与产出共用同一个锚点（{@link #lastExecutedOpTasks}）：末道被跳过时往后退层，
     * 由仍在执行的那道同时决定产出与能否收尾。全部工序都被跳过的工单没有产出，不收尾——
     * 那是应该走取消而不是完工的场景。
     */
    private void rollUpProdOrder(ProdOrderDO order, List<OpTaskDO> orderTasks,
                                 OpTaskDO trigger, String idempotencyKey) {
        BigDecimal completedQty = lastExecutedOpCompletedQty(orderTasks);
        BigDecimal scrapQty = orderTasks.stream().map(item -> nvl(item.getScrapQty()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<OpTaskDO> lastTasks = lastExecutedOpTasks(orderTasks);
        boolean done = MesProdOrderStatusEnum.RUNNING.name().equals(order.getStatus())
                && !lastTasks.isEmpty()
                && lastTasks.stream().allMatch(this::lastOpReached);
        String toStatus = done
                ? MesProdOrderStateMachine.next(order.getStatus(), MesProdOrderStateMachine.ACTION_DONE)
                : null;
        if (completedQty.compareTo(nvl(order.getCompletedQty())) == 0
                && scrapQty.compareTo(nvl(order.getScrapQty())) == 0 && toStatus == null) {
            return;
        }
        LocalDateTime endTime = done ? LocalDateTime.now() : null;
        int currentVersion = order.getVersion() == null ? MesConstants.INIT_VERSION : order.getVersion();
        LambdaUpdateWrapper<ProdOrderDO> wrapper = new LambdaUpdateWrapper<ProdOrderDO>()
                .eq(ProdOrderDO::getId, order.getId())
                .eq(ProdOrderDO::getVersion, currentVersion);
        if (prodOrderMapper.update(ProdOrderDO.builder().completedQty(completedQty).scrapQty(scrapQty)
                .status(toStatus).actualEndTime(endTime).version(currentVersion + 1).build(), wrapper) == 0) {
            throw exception(PROD_ORDER_CONCURRENT_CONFLICT);
        }
        order.setCompletedQty(completedQty).setScrapQty(scrapQty).setVersion(currentVersion + 1);
        if (toStatus == null) {
            return;
        }
        String fromStatus = order.getStatus();
        order.setStatus(toStatus).setActualEndTime(endTime);
        recordDoneTxn(order.getId(), MesConstants.NONE_ID, trigger, completedQty,
                fromStatus, toStatus, MesBizTypeEnum.PROD_ORDER, order.getOrderNo(),
                idempotencyKey + KEY_SEPARATOR + KEY_PROD_ORDER_DONE);
        // 工单一转 DONE 就把成品入库（04 文档流程七）：入库量取"已完工减已入库"的剩余，
        // 成品物料没配默认仓库与库位时静默跳过，不拿主数据缺失去挡车间完工
        finishInboundService.autoInboundOnOrderDone(order.getId(), trigger.getWorkOrderId());
    }

    /**
     * 最后一道实际执行的工序的完成数。
     *
     * <p>跳过的工序贡献 0，不能让它把产出清零——并行组混合 DONE 与 SKIPPED 时只对未跳过的取最小值，
     * 整层被跳过时退到前一层，见 {@link #lastExecutedOpTasks}。
     */
    private BigDecimal lastExecutedOpCompletedQty(List<OpTaskDO> tasks) {
        return lastExecutedOpTasks(tasks).stream()
                .filter(item -> !MesOpTaskStatusEnum.SKIPPED.name().equals(item.getStatus()))
                .map(item -> nvl(item.getCompletedQty()))
                .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    /**
     * 末道是否已经把活干完：DONE 且数量达标，或 SKIPPED。
     *
     * <p>判定只对"最后一道实际执行的工序"层生效（见 {@link #lastExecutedOpTasks}），因此出现 SKIPPED
     * 分支时整层必然全是跳过的工序；DONE 分支保留数量条件——完工动作自己校验过严格相等，
     * 数量不达标的 DONE 是异常数据，不能拿去收尾整单。混合层（并行组跳了一道）按 DONE 的那道要求数量达标。
     */
    private boolean lastOpReached(OpTaskDO task) {
        if (MesOpTaskStatusEnum.SKIPPED.name().equals(task.getStatus())) {
            return true;
        }
        return MesOpTaskStatusEnum.DONE.name().equals(task.getStatus()) && qtyReached(task);
    }

    /** 只认已完工与已跳过，与开工门禁的前序判定口径一致：取消不算完成。 */
    private boolean allDoneOrSkipped(List<OpTaskDO> tasks) {
        return !tasks.isEmpty() && tasks.stream()
                .allMatch(item -> MesOpTaskStatusEnum.DONE.name().equals(item.getStatus())
                        || MesOpTaskStatusEnum.SKIPPED.name().equals(item.getStatus()));
    }

    /** 末道工序集合：{@code seq} 最大的那一批，并行组会有多道。 */
    private List<OpTaskDO> lastOpTasks(List<OpTaskDO> tasks) {
        int maxSeq = tasks.stream().mapToInt(this::seqOf).max().orElse(Integer.MIN_VALUE);
        return tasks.stream().filter(item -> seqOf(item) == maxSeq).toList();
    }

    /**
     * 最后一道实际执行的工序集合：从 {@code seq} 最大的那批开始，整批都被跳过就退到前一个 seq。
     *
     * <p>跳序的语义是"这道工序不干了"，被跳过的工序不出产出也不出报废，把它当末道会把产出清零、
     * 把收尾判定压死在数量条件上。退层只看状态不看数量：没有任何一道真正执行过时返回空集、产出记 0。
     */
    private List<OpTaskDO> lastExecutedOpTasks(List<OpTaskDO> tasks) {
        int seq = tasks.stream().mapToInt(this::seqOf).max().orElse(Integer.MIN_VALUE);
        while (seq > Integer.MIN_VALUE) {
            int layerSeq = seq;
            List<OpTaskDO> layer = tasks.stream().filter(item -> seqOf(item) == layerSeq).toList();
            if (layer.stream().anyMatch(item ->
                    !MesOpTaskStatusEnum.SKIPPED.name().equals(item.getStatus()))) {
                return layer;
            }
            // 整层全是跳过的，退到下一个更小的 seq
            seq = tasks.stream().mapToInt(this::seqOf).filter(item -> item < layerSeq)
                    .max().orElse(Integer.MIN_VALUE);
        }
        return List.of();
    }

    /**
     * 写一条完工流水。
     *
     * <p>{@code op_task_id} 记的是"哪道工序带动的"；变更主体由 {@code bizType} 与 {@code bizNo} 指明，
     * {@code qty} 按 04 文档记 COMPLETE 的累计合格数。
     */
    private void recordDoneTxn(Long prodOrderId, Long workOrderId, OpTaskDO trigger, BigDecimal qty,
                               String fromStatus, String toStatus, MesBizTypeEnum bizType, String bizNo,
                               String idempotencyKey) {
        wipTxnRecorder.record(new MesWipTxnBO()
                .setProdOrderId(prodOrderId)
                .setWorkOrderId(workOrderId)
                .setOpTaskId(trigger.getId())
                .setTxnType(MesWipTxnTypeEnum.COMPLETE)
                .setQty(qty)
                .setFromStatus(fromStatus)
                .setToStatus(toStatus)
                .setBizType(bizType)
                .setBizNo(bizNo)
                .setIdempotencyKey(idempotencyKey)
                .setRemark("工序（" + trigger.getOpName() + "）完工带动"));
    }

    private int seqOf(OpTaskDO task) {
        return task.getSeq() == null ? 0 : task.getSeq();
    }

    private boolean isNonZero(BigDecimal value) {
        return value != null && value.signum() != 0;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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

}
