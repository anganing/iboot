package com.iboot.pf.module.mes.service.prodorder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;
import com.iboot.pf.module.mdm.api.route.dto.RouteRespDTO;
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
import com.iboot.pf.module.mes.enums.MesProdOrderStateMachine;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStateMachine;
import com.iboot.pf.module.mes.service.command.MesCommandService;
import com.iboot.pf.module.mes.service.prodorder.bo.MesOrderActionBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesReleaseOrderBO;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import com.iboot.pf.module.mes.service.wiptxn.bo.MesWipTxnBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_WORKSHOP_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_STATUS_INVALID;

/**
 * 生产工单领域动作实现。
 *
 * <p>四个动作共用一套骨架：命令幂等 -&gt; 状态机裁决目标状态 -&gt; 带乐观锁条件落库 -&gt; 写在制流水。
 * 骨架抽在 {@link #execute} 与 {@link #advanceProdOrder}；下达多了锁版本与工序展开两步，单独成
 * {@link #doRelease}，展开本身又拆到 {@link MesOrderExpandHelper}。
 */
@Service
public class MesProdOrderDomainServiceImpl implements MesProdOrderDomainService {

    /** 幂等键的段分隔符，格式见 08 文档的幂等键全表。 */
    private static final String KEY_SEPARATOR = ":";
    /** 整单动作的行号段，工单级动作没有行号，固定 0。 */
    private static final String WHOLE_ORDER_LINE_NO = "0";

    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesCommandService commandService;
    @Resource
    private MesWipTxnRecorder wipTxnRecorder;
    @Resource
    private MesOrderExpandHelper expandHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseProdOrder(MesReleaseOrderBO bo) {
        ProdOrderDO order = requireProdOrder(bo.getProdOrderId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.RELEASE_PROD_ORDER, order.getOrderNo());
        commandService.executeVoid(MesCommandTypeEnum.RELEASE_PROD_ORDER, command,
                () -> doRelease(order, command.getIdempotencyKey(), bo.getRemark()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void holdProdOrder(MesOrderActionBO bo) {
        execute(bo, MesCommandTypeEnum.HOLD_PROD_ORDER, MesProdOrderStateMachine.ACTION_HOLD,
                MesWipTxnTypeEnum.PAUSE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resumeProdOrder(MesOrderActionBO bo) {
        execute(bo, MesCommandTypeEnum.RESUME_PROD_ORDER, MesProdOrderStateMachine.ACTION_RESUME,
                MesWipTxnTypeEnum.RESUME);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProdOrder(MesOrderActionBO bo) {
        execute(bo, MesCommandTypeEnum.CANCEL_PROD_ORDER, MesProdOrderStateMachine.ACTION_CANCEL,
                MesWipTxnTypeEnum.SKIP, this::cascadeCancel);
    }

    /**
     * 下达的四件事：锁生效 BOM 版本、锁生效工艺路线版本、按路线展开、写下达时间与在制流水。
     *
     * <p>幂等有两道：命令记录拦住重复下发，状态机拦住第二次下达（已是 {@code RELEASED}，没有 release 出边），
     * 因此不会出现第二批工序任务。
     */
    private void doRelease(ProdOrderDO order, String idempotencyKey, String remark) {
        String fromStatus = order.getStatus();
        String toStatus = MesProdOrderStateMachine.next(fromStatus, MesProdOrderStateMachine.ACTION_RELEASE);

        // 车间必须已指派：展开出来的每张作业工单都从工单身上抄 workshop_id，落 0 就是一批没有归属的
        // 作业工单，车间派工按车间筛选时根本看不到它们。需求转单只到工厂粒度，这里是最后一道关卡。
        if (order.getWorkshopId() == null || MesConstants.NONE_ID.equals(order.getWorkshopId())) {
            throw exception(PROD_ORDER_WORKSHOP_REQUIRED);
        }

        // 锁的是"下达这一刻"物料的生效版本：之后主数据改 BOM、改工艺，在制工单不受影响。
        // 无生效版本直接拒绝下达，不允许拿一个不生效的版本开工。
        LocalDate today = LocalDate.now();
        BomRespDTO bom = snapshotService.requireEffectiveBom(order.getMatId(), today);
        RouteRespDTO route = snapshotService.requireEffectiveRoute(order.getMatId(), today);

        LocalDateTime releaseTime = LocalDateTime.now();
        advanceProdOrder(order, ProdOrderDO.builder()
                .bomId(bom.getId())
                .bomVersion(bom.getVersion())
                .routeId(route.getId())
                .routeVersion(route.getVersion())
                .status(toStatus)
                .releaseTime(releaseTime)
                .build());
        // 内存对象同步锁定后的值：展开必须按锁定的 routeId 走，用工单原来的路线等于展开结果与执行依据对不上
        order.setBomId(bom.getId()).setBomVersion(bom.getVersion())
                .setRouteId(route.getId()).setRouteVersion(route.getVersion())
                .setStatus(toStatus).setReleaseTime(releaseTime);

        expandHelper.expand(order);

        recordOrderTxn(order, MesWipTxnTypeEnum.START, fromStatus, toStatus,
                order.getQty(), idempotencyKey, remark);
    }

    /** 无后续联动的状态动作。 */
    private void execute(MesOrderActionBO bo, MesCommandTypeEnum commandType, String action,
                         MesWipTxnTypeEnum txnType) {
        execute(bo, commandType, action, txnType, order -> {
        });
    }

    /**
     * 状态动作的公共骨架：命令幂等、状态机裁决、带乐观锁条件落库、写在制流水，最后跑动作专属的联动。
     *
     * <p>目标状态一律由状态机给出，方法签名里只有动作名：入参带目标状态就等于把状态机降级成建议，
     * 调用方可以把工单推到任意状态。
     */
    private void execute(MesOrderActionBO bo, MesCommandTypeEnum commandType, String action,
                         MesWipTxnTypeEnum txnType, Consumer<ProdOrderDO> cascade) {
        ProdOrderDO order = requireProdOrder(bo.getProdOrderId());
        MesCommandReqDTO command = buildCommand(bo, commandType, order.getOrderNo());
        commandService.executeVoid(commandType, command, () -> {
            String fromStatus = order.getStatus();
            String toStatus = MesProdOrderStateMachine.next(fromStatus, action);
            advanceProdOrder(order, ProdOrderDO.builder().status(toStatus).build());
            order.setStatus(toStatus);
            // 状态类事务数量记 0，由 Recorder 兜底
            recordOrderTxn(order, txnType, fromStatus, toStatus, null,
                    command.getIdempotencyKey(), bo.getReason());
            cascade.accept(order);
        });
    }

    /**
     * 带乐观锁条件推进生产工单，并把新版本号同步回内存对象。
     *
     * <p>版本号自增 + {@code WHERE version = 读到的版本}：并发下只有一个请求能把工单从当前状态推走，
     * 另一个更新 0 行直接报冲突。少了这个条件，两个并发下达会各展开一批工序任务。
     */
    private void advanceProdOrder(ProdOrderDO order, ProdOrderDO update) {
        int currentVersion = order.getVersion() == null ? MesConstants.INIT_VERSION : order.getVersion();
        update.setVersion(currentVersion + 1);
        LambdaUpdateWrapper<ProdOrderDO> wrapper = new LambdaUpdateWrapper<ProdOrderDO>()
                .eq(ProdOrderDO::getId, order.getId())
                .eq(ProdOrderDO::getVersion, currentVersion);
        if (prodOrderMapper.update(update, wrapper) == 0) {
            throw exception(PROD_ORDER_CONCURRENT_CONFLICT);
        }
        order.setVersion(currentVersion + 1);
    }

    /**
     * 级联取消：作业工单与工序任务一起置 {@code CANCELLED}。
     *
     * <p>不级联的后果是工单已取消、下面的工序任务还停在 {@code PENDING}，车间照样能开工。
     *
     * <p>两次批量查（按生产工单查作业工单、按作业工单集合查工序任务），不按行回查，避免 N+1。
     * 目标状态仍由各自状态机裁决而不是硬写 {@code CANCELLED}：已开工的作业工单出现在可取消的工单下面
     * 属于数据损坏，就地报错比静默改状态强。
     */
    private void cascadeCancel(ProdOrderDO order) {
        List<WorkOrderDO> workOrders = workOrderMapper.selectListByProdOrderIds(List.of(order.getId()));
        if (CollUtil.isEmpty(workOrders)) {
            return;
        }
        for (WorkOrderDO workOrder : workOrders) {
            String toStatus = MesWorkOrderStateMachine.next(
                    workOrder.getStatus(), MesWorkOrderStateMachine.ACTION_CANCEL);
            int currentVersion = workOrder.getVersion() == null
                    ? MesConstants.INIT_VERSION : workOrder.getVersion();
            LambdaUpdateWrapper<WorkOrderDO> wrapper = new LambdaUpdateWrapper<WorkOrderDO>()
                    .eq(WorkOrderDO::getId, workOrder.getId())
                    .eq(WorkOrderDO::getVersion, currentVersion);
            if (workOrderMapper.update(WorkOrderDO.builder()
                    .status(toStatus).version(currentVersion + 1).build(), wrapper) == 0) {
                throw exception(WORK_ORDER_STATUS_INVALID);
            }
        }
        List<OpTaskDO> opTasks = opTaskMapper.selectListByWorkOrderIds(
                workOrders.stream().map(WorkOrderDO::getId).toList());
        for (OpTaskDO opTask : opTasks) {
            String toStatus = MesOpTaskStateMachine.next(opTask.getStatus(), MesOpTaskStateMachine.ACTION_CANCEL);
            int currentVersion = opTask.getVersion() == null ? MesConstants.INIT_VERSION : opTask.getVersion();
            LambdaUpdateWrapper<OpTaskDO> wrapper = new LambdaUpdateWrapper<OpTaskDO>()
                    .eq(OpTaskDO::getId, opTask.getId())
                    .eq(OpTaskDO::getVersion, currentVersion);
            if (opTaskMapper.update(OpTaskDO.builder()
                    .status(toStatus).version(currentVersion + 1).build(), wrapper) == 0) {
                throw exception(OP_TASK_STATUS_INVALID);
            }
        }
    }

    /**
     * 写一条工单级在制流水。
     *
     * <p>没有作业工单与工序任务维度（Recorder 自动落 0），来源业务记生产工单。幂等键直接用动作的键：
     * 一个动作只写一条工单级流水，键与命令记录同值但落在不同表，互不影响。
     */
    private void recordOrderTxn(ProdOrderDO order, MesWipTxnTypeEnum txnType, String fromStatus,
                                String toStatus, BigDecimal qty, String idempotencyKey, String remark) {
        wipTxnRecorder.record(new MesWipTxnBO()
                .setProdOrderId(order.getId())
                .setTxnType(txnType)
                .setQty(qty)
                .setFromStatus(fromStatus)
                .setToStatus(toStatus)
                .setBizType(MesBizTypeEnum.PROD_ORDER)
                .setBizNo(order.getOrderNo())
                .setIdempotencyKey(idempotencyKey)
                .setRemark(remark));
    }

    /**
     * 组装幂等命令。
     *
     * <p>幂等键调用方给了就以它为准，留空时按四段格式 {@code {来源模块}:{工单号}:{命令类型}:{行号}} 派生，
     * 与 08 文档的幂等键全表一致。动作段取 {@link MesCommandTypeEnum} 的枚举名而不是写字面量：
     * 键会落进 {@code mes_command_record}，字面量拼出来的历史键在命令改名后就与枚举对不上了。
     * 工单级动作没有行号，第四段固定 0。
     *
     * <p>来源单号留空时兜底成工单号，因为 {@code mes_command_record.source_biz_no} 是 NOT NULL。
     */
    private MesCommandReqDTO buildCommand(MesCommandReqDTO source, MesCommandTypeEnum commandType, String orderNo) {
        // 来源模块是幂等键的第一段，也是排查"这条命令谁下的"的唯一线索，漏传属于调用方契约违约
        Assert.notBlank(source.getSourceModule(), "来源模块不能为空，它是幂等键的第一段");
        OrderCommand command = new OrderCommand();
        command.setSourceModule(source.getSourceModule());
        command.setSourceBizNo(StrUtil.isBlank(source.getSourceBizNo()) ? orderNo : source.getSourceBizNo());
        command.setSourceBizLineNo(source.getSourceBizLineNo());
        command.setIdempotencyKey(StrUtil.isBlank(source.getIdempotencyKey())
                ? String.join(KEY_SEPARATOR, source.getSourceModule(), orderNo,
                commandType.name(), WHOLE_ORDER_LINE_NO)
                : source.getIdempotencyKey());
        return command;
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
     * <p>{@link MesCommandReqDTO} 是抽象类，工单域给它一个最小实现：既不必新建一个公共 DTO，
     * 也不用把派生出来的幂等键写回调用方的 BO——改调用方传进来的对象是隐蔽的副作用。
     */
    private static class OrderCommand extends MesCommandReqDTO {
    }

}
