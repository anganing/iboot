package com.iboot.pf.module.mes.service.wiptxn;

import cn.hutool.core.lang.Assert;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.wiptxn.bo.MesWipTxnBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 在制流水统一写入封装。
 *
 * <p>{@code mes_wip_txn} 的唯一写入出口。下达、开工、暂停、恢复、报工、完工、跳序、领料这些动作
 * 每次状态或数量变更都经这里留痕，不各自手写 DO：11 个动作各写一遍的话，字段口径必然漂移、幂等键必然有人漏。
 *
 * <p>三件事在这里统一兜住：
 * <ol>
 *     <li>幂等：先按 {@code idempotency_key} 判重，命中直接返回原流水；并发由表上
 *     {@code uk_mes_wip_txn_idem_deleted} 唯一索引兜第二道</li>
 *     <li>编号：{@code txn_no} 走 infra 编码规则 {@code MES_WIP_TXN_NO}，不由调用方拼字符串</li>
 *     <li>可选维度落 0 不落 null：{@code work_order_id}、{@code op_task_id}、{@code from_op_task_id}、
 *     {@code to_op_task_id} 在库表里是 NOT NULL，落 null 会让统计口径失效</li>
 * </ol>
 *
 * <p>字段含义与各 {@code txn_type} 的写入时机见 {@code dev-docs/业务流程/04-MES生产执行流程.md} 的"在制流水"章节。
 */
@Component
public class MesWipTxnRecorder {

    @Resource
    private WipTxnMapper wipTxnMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private MesCodeService codeService;

    /**
     * 写一条在制流水。
     *
     * @param bo 流水参数
     * @return 落库的流水；幂等命中时返回已有的那条，不重复写
     */
    @Transactional(rollbackFor = Exception.class)
    public WipTxnDO record(MesWipTxnBO bo) {
        // 1. 契约校验：这几项是 NOT NULL 列，漏传等于把追溯链写断，就地暴露比事后查数据强
        Assert.notNull(bo, "在制流水参数不能为空");
        Assert.notBlank(bo.getIdempotencyKey(), "在制流水幂等键不能为空，否则同一动作会重复留痕");
        Assert.notNull(bo.getProdOrderId(), "在制流水的生产工单编号不能为空");
        Assert.notNull(bo.getTxnType(), "在制流水的事务类型不能为空");
        Assert.notBlank(bo.getToStatus(), "在制流水的变更后状态不能为空");
        Assert.notNull(bo.getBizType(), "在制流水的来源业务类型不能为空");
        Assert.notBlank(bo.getBizNo(), "在制流水的来源业务单号不能为空");

        // 2. 幂等：同一幂等键只留一条
        WipTxnDO existed = wipTxnMapper.selectOne(WipTxnDO::getIdempotencyKey, bo.getIdempotencyKey());
        if (existed != null) {
            return existed;
        }

        // 3. 组装并落库，可选维度统一落 0
        WipTxnDO txn = WipTxnDO.builder()
                .txnNo(codeService.generate(MesCodeRuleConstants.WIP_TXN, null))
                .prodOrderId(bo.getProdOrderId())
                .workOrderId(nvl(bo.getWorkOrderId()))
                .opTaskId(nvl(bo.getOpTaskId()))
                .txnType(bo.getTxnType().name())
                .qty(bo.getQty() == null ? MesConstants.ZERO_QTY : bo.getQty())
                .fromStatus(bo.getFromStatus())
                .toStatus(bo.getToStatus())
                .bizType(bo.getBizType().name())
                .bizNo(bo.getBizNo())
                .fromOpTaskId(nvl(bo.getFromOpTaskId()))
                .toOpTaskId(nvl(bo.getToOpTaskId()))
                .txnTime(bo.getTxnTime() == null ? LocalDateTime.now() : bo.getTxnTime())
                .idempotencyKey(bo.getIdempotencyKey())
                .remark(bo.getRemark())
                .build();
        wipTxnMapper.insert(txn);
        return txn;
    }

    /**
     * 写一条工序任务的状态变更流水。开工、暂停、恢复、完工、跳序都用它。
     *
     * <p>三个追溯维度全部从工序任务身上取，调用方不用逐个塞：{@code opTaskId} 与 {@code workOrderId} 取自
     * 工序任务本身，{@code prodOrderId} 按作业工单反查（{@code mes_op_task} 上没有这一列）。
     * 来源业务固定记 {@code OP_TASK} 与工序任务号。
     *
     * @param opTask         工序任务
     * @param txnType        事务类型
     * @param fromStatus     变更前状态，取 {@code MesOpTaskStatusEnum} 的 {@code name()}
     * @param toStatus       变更后状态，取 {@code MesOpTaskStatusEnum} 的 {@code name()}
     * @param idempotencyKey 幂等键
     * @return 落库的流水
     */
    @Transactional(rollbackFor = Exception.class)
    public WipTxnDO recordStatusChange(OpTaskDO opTask, MesWipTxnTypeEnum txnType,
                                       String fromStatus, String toStatus, String idempotencyKey) {
        Assert.notNull(opTask, "工序任务不能为空");
        MesWipTxnBO bo = new MesWipTxnBO()
                .setProdOrderId(resolveProdOrderId(opTask))
                .setWorkOrderId(opTask.getWorkOrderId())
                .setOpTaskId(opTask.getId())
                .setTxnType(txnType)
                .setFromStatus(fromStatus)
                .setToStatus(toStatus)
                .setBizType(MesBizTypeEnum.OP_TASK)
                .setBizNo(opTask.getTaskNo())
                .setIdempotencyKey(idempotencyKey);
        return record(bo);
    }

    /**
     * 写一条报工流水。
     *
     * <p>报工不改工序任务状态，因此前后状态都记当前状态；{@code qty} 记本次合格数。
     * 需要把流水挂到具体报工单上时用 {@link #record(MesWipTxnBO)} 自行指定 {@code bizType} 与 {@code bizNo}。
     *
     * @param opTask         工序任务
     * @param qty            本次合格数
     * @param idempotencyKey 幂等键
     * @return 落库的流水
     */
    @Transactional(rollbackFor = Exception.class)
    public WipTxnDO recordReport(OpTaskDO opTask, BigDecimal qty, String idempotencyKey) {
        Assert.notNull(opTask, "工序任务不能为空");
        MesWipTxnBO bo = new MesWipTxnBO()
                .setProdOrderId(resolveProdOrderId(opTask))
                .setWorkOrderId(opTask.getWorkOrderId())
                .setOpTaskId(opTask.getId())
                .setTxnType(MesWipTxnTypeEnum.REPORT)
                .setQty(qty)
                .setFromStatus(opTask.getStatus())
                .setToStatus(opTask.getStatus())
                .setBizType(MesBizTypeEnum.OP_TASK)
                .setBizNo(opTask.getTaskNo())
                .setIdempotencyKey(idempotencyKey);
        return record(bo);
    }

    /**
     * 按作业工单反查生产工单编号。
     *
     * <p>工序任务必挂在作业工单下（{@code mes_op_task.work_order_id} 是 NOT NULL），查不到属于数据损坏，
     * 静默落 0 会让追溯链断在这里，所以直接暴露。
     */
    private Long resolveProdOrderId(OpTaskDO opTask) {
        WorkOrderDO workOrder = workOrderMapper.selectById(opTask.getWorkOrderId());
        Assert.notNull(workOrder, "工序任务({})的作业工单({})不存在", opTask.getId(), opTask.getWorkOrderId());
        return workOrder.getProdOrderId();
    }

    /** 可选维度落 0 不落 null。 */
    private Long nvl(Long value) {
        return value == null ? MesConstants.NONE_ID : value;
    }

}
