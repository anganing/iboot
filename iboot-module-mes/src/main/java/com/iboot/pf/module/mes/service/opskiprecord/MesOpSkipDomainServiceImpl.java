package com.iboot.pf.module.mes.service.opskiprecord;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.bpm.api.task.BpmProcessInstanceApi;
import com.iboot.pf.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import com.iboot.pf.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.iboot.pf.framework.security.core.util.SecurityFrameworkUtils;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.opskiprecord.OpSkipRecordDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.opskiprecord.OpSkipRecordMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStateMachine;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesSkipApproveStatusEnum;
import com.iboot.pf.module.mes.enums.MesSkipPolicyEnum;
import com.iboot.pf.module.mes.enums.MesSkipRecordStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.service.bpm.MesBpmApprovalActionExecutor;
import com.iboot.pf.module.mes.service.command.MesCommandService;
import com.iboot.pf.module.mes.service.optask.MesOpTaskDomainService;
import com.iboot.pf.module.mes.service.opskiprecord.bo.MesOpSkipBO;
import com.iboot.pf.module.mes.service.opskiprecord.bo.MesRevokeSkipBO;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import com.iboot.pf.module.mes.service.wiptxn.bo.MesWipTxnBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_EFFECT_REMARK_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_NOT_ALLOWED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_REASON_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_RECORD_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_RECORD_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;

/**
 * 工序跳序实现。
 *
 * <p>策略分流写在 {@link #skip}，三条路各自的落库形态见接口注释的对照表。
 *
 * <p>撤销时工序从 {@code SKIPPED} 退回 {@code READY} 走状态机的 {@code revokeSkip} 边。这条边是本任务
 * 交付后由调度方补的：交付时 {@code MesOpTaskStateMachine} 里 {@code SKIPPED} 没有出边、04 文档的状态图
 * 也把它画成终态，而同一份文档的"跳序"章节又要求"撤销跳序把记录置 REVOKED，工序任务回到 READY"——文档
 * 自身矛盾。裁定是文档正文对、状态图漏了这条边：跳序可撤销，{@code SKIPPED} 与 {@code DONE} /
 * {@code CANCELLED} 不同类，后两者做完的活和取消的活都回不来。补边后本类不再有绕开状态机的写法。
 *
 * <p>状态机裁决之外仍保留 {@code WHERE status = ? AND version = ?} 的条件更新，那是乐观锁不是状态校验，
 * 与 T1-13 的 {@code advanceOpTask} 同形：裁决管"这个迁移合不合法"，条件更新管"并发下有没有人先改了"。
 */
@Service
@Slf4j
public class MesOpSkipDomainServiceImpl implements MesOpSkipDomainService {

    /** 工序跳序审批流程定义 key，与状态监听器共用。 */
    public static final String PROCESS_KEY_OP_SKIP = "mes-op-skip";

    /** 幂等键的段分隔符，格式见 08 文档的幂等键全表。 */
    private static final String KEY_SEPARATOR = ":";
    /** 工序任务级动作没有行号，第四段固定 0。 */
    private static final String WHOLE_TASK_LINE_NO = "0";
    /** 审批回调驱动的生效动作，来源固定 BPM，与人工跳序在流水与命令记录里区分开。 */
    private static final String SOURCE_MODULE_BPM = "BPM";
    /** 审批通过生效动作的键段。 */
    private static final String ACTION_OP_SKIP_APPROVE = "OP_SKIP_APPROVE";

    @Resource
    private OpSkipRecordMapper opSkipRecordMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private MesCommandService commandService;
    @Resource
    private MesWipTxnRecorder wipTxnRecorder;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;
    /** 审批终态回写后的生效动作执行器：把生效挪出 Flowable 事务，避免动作失败连审批终态一起回滚。 */
    @Resource
    private MesBpmApprovalActionExecutor approvalActionExecutor;
    /**
     * 后序推进与工单上卷留在工序域，生效时要调回去（并行组语义只有一份）。
     *
     * <p>本类被工序域的 {@code skipOpTask} 调、生效时又要调回工序域，两个 Service 互相引用构成创建期的环，
     * 因此打 {@code @Lazy} 注入代理（与本仓 bpm 模块解开同类环的写法一致）。
     */
    @Resource
    @Lazy
    private MesOpTaskDomainService opTaskDomainService;

    @Override
    public boolean skip(OpTaskDO task, Long prodOrderId, MesOpSkipBO bo, String idempotencyKey) {
        String policy = task.getSkipPolicy();
        boolean approvalRequired = MesSkipPolicyEnum.isApprovalRequired(policy);
        // 只放行明确配了"填原因即可"或"需审批"的两档。NOT_ALLOWED 与任何未识别的取值都拒绝：
        // 库列默认值就是 NOT_ALLOWED，跳序是绕过工艺，配置缺失时放行比拦下危险得多
        if (!approvalRequired && !ObjUtil.equal(MesSkipPolicyEnum.ALLOW_WITH_REASON.name(), policy)) {
            throw exception(OP_SKIP_NOT_ALLOWED);
        }
        // 原因类型与原因说明两档都要：待审批的记录没有原因，审批人无从下手
        if (StrUtil.isBlank(bo.getSkipReasonType()) || StrUtil.isBlank(bo.getReason())) {
            throw exception(OP_SKIP_REASON_REQUIRED);
        }
        if (approvalRequired && StrUtil.isBlank(bo.getEffectRemark())) {
            throw exception(OP_SKIP_EFFECT_REMARK_REQUIRED);
        }

        // 状态机先裁决再落记录：只有 PENDING 与 READY 有 skip 出边，已开工的工序跳不了，
        // 这时候连待审批记录都不该留——留了会让人以为在等审批，其实这道工序正在加工
        String fromStatus = task.getStatus();
        String toStatus = MesOpTaskStateMachine.next(fromStatus, MesOpTaskStateMachine.ACTION_SKIP);

        LocalDateTime skipTime = LocalDateTime.now();
        OpSkipRecordDO record = OpSkipRecordDO.builder()
                .opTaskId(task.getId())
                .skipReasonType(bo.getSkipReasonType())
                .skipReason(bo.getReason())
                .operatorId(nvl(bo.getOperatorId()))
                .approvedBy(MesConstants.NONE_ID)
                .approveStatus(approvalRequired ? MesSkipApproveStatusEnum.PENDING.name()
                        : MesSkipApproveStatusEnum.NOT_REQUIRED.name())
                // 待审批时实际跳过时间留空：那一刻工序还没跳过，填上就成了假数据
                .skipTime(approvalRequired ? null : skipTime)
                .effectRemark(bo.getEffectRemark())
                .status(approvalRequired ? MesSkipRecordStatusEnum.PENDING.name()
                        : MesSkipRecordStatusEnum.EFFECTIVE.name())
                .build();
        opSkipRecordMapper.insert(record);
        if (approvalRequired) {
            // 发起 BPM 审批：以跳序记录 id 为 businessKey，审批通过后由监听器走生效那一支
            BpmProcessInstanceCreateReqDTO createReq = new BpmProcessInstanceCreateReqDTO();
            createReq.setProcessDefinitionKey(PROCESS_KEY_OP_SKIP);
            createReq.setBusinessKey(String.valueOf(record.getId()));
            String processInstanceId = processInstanceApi.createProcessInstance(
                    SecurityFrameworkUtils.getLoginUserId(), createReq);
            // 流程实例是回调幂等校验的第二道身份；不回写会使任何终态回调都被当成无关事件忽略。
            opSkipRecordMapper.updateById(new OpSkipRecordDO()
                    .setId(record.getId())
                    .setProcessInstanceId(processInstanceId)
                    .setApprovalStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus()));
            return false;
        }
        advanceToSkipped(task, toStatus);
        recordSkipTxn(task, prodOrderId, fromStatus, toStatus, idempotencyKey, bo.getReason());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeSkip(MesRevokeSkipBO bo) {
        OpSkipRecordDO record = requireRecord(bo.getSkipRecordId());
        OpTaskDO task = requireOpTask(record.getOpTaskId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.REVOKE_OP_SKIP, task.getTaskNo());
        commandService.executeVoid(MesCommandTypeEnum.REVOKE_OP_SKIP, command,
                () -> doRevoke(record, task, bo, command.getIdempotencyKey()));
    }

    /**
     * 回写跳序审批终态，三层幂等。
     *
     * <p>第三层的条件 UPDATE 同时挡住两件事：重复投递不会二次生效，且"抢到 1 行"是执行领域动作的
     * 许可证。驳回与撤销只改记录，工序任务一行不动——审批期间工序还没跳，没有什么可回滚的。
     *
     * <p>通过后的生效动作（推 SKIPPED、写流水、推后序）交给 {@link MesBpmApprovalActionExecutor}
     * 延迟到本事务提交之后执行。就地执行的话，动作一旦失败会把 Flowable 的事务标成 rollback-only，
     * 审批人每次点通过都失败（详见执行器类注释）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSkipApprovalStatus(Long skipRecordId, String processInstanceId,
                                         Integer status, String reason) {
        // 第一层：非终态不改业务事实
        if (!BpmProcessInstanceStatusEnum.isProcessEndStatus(status)) {
            return;
        }
        // 记录不存在按"与本次回调无关"处理、不抛异常：回调跑在 Flowable 事务里，抛出去就把
        // rollback-only 留给了流程，审批终态照样回滚——与驳回回调那个 BUG 是同一个失效机制。
        // （takeEffectApprovedSkip 相反：它在事务提交后的独立事务里跑，查不到就该抛，让重试看得见原因。）
        OpSkipRecordDO record = opSkipRecordMapper.selectById(skipRecordId);
        if (record == null) {
            log.warn("[updateSkipApprovalStatus] 跳序记录不存在，忽略本次审批回调。skipRecordId={}, instanceId={}",
                    skipRecordId, processInstanceId);
            return;
        }
        // 第二层：只接受这条记录正在等待的流程实例，且当前确实处于审批中
        if (!processInstanceId.equals(record.getProcessInstanceId())
                || !BpmProcessInstanceStatusEnum.RUNNING.getStatus().equals(record.getApprovalStatus())) {
            return;
        }
        boolean approved = BpmProcessInstanceStatusEnum.APPROVE.getStatus().equals(status);
        // 第三层：条件更新抢占终态处理权。status = PENDING 是这里的乐观锁
        //（mes_op_skip_record 没有 version 列，与 doRevoke 同一口径）
        int updated = opSkipRecordMapper.update(new OpSkipRecordDO()
                        .setId(skipRecordId)
                        .setStatus(approved ? MesSkipRecordStatusEnum.EFFECTIVE.name()
                                : MesSkipRecordStatusEnum.REVOKED.name())
                        .setApproveStatus(approved ? MesSkipApproveStatusEnum.APPROVED.name()
                                : MesSkipApproveStatusEnum.REJECTED.name())
                        .setApprovalStatus(status)
                        .setApprovalTime(LocalDateTime.now())
                        .setApprovalReason(reason),
                new LambdaUpdateWrapper<OpSkipRecordDO>()
                        .eq(OpSkipRecordDO::getId, skipRecordId)
                        .eq(OpSkipRecordDO::getStatus, MesSkipRecordStatusEnum.PENDING.name())
                        .eq(OpSkipRecordDO::getApprovalStatus,
                                BpmProcessInstanceStatusEnum.RUNNING.getStatus()));
        if (updated != 1) {
            return;
        }
        log.info("[updateSkipApprovalStatus] 跳序审批终态回写。recordId={}, status={}, approved={}",
                skipRecordId, status, approved);
        if (!approved) {
            return;
        }
        approvalActionExecutor.executeAfterCommit("工序跳序生效：记录 " + skipRecordId,
                // 执行器已在独立新事务里跑这段，直接调本类方法即可；跨到工序域的那一跳是另一个 bean，
                // 它的 @Transactional(REQUIRED) 会加入这个事务，动作失败整段回滚
                () -> takeEffectApprovedSkip(skipRecordId));
    }

    /**
     * 让一条已批准的跳序记录生效：工序任务推 {@code SKIPPED}、写 {@code SKIP} 流水、推后序。
     *
     * <p>推工序与写流水与 {@code ALLOW_WITH_REASON} 那一支完全同构；后序推进留在
     * {@code MesOpTaskDomainService}，经代理调 {@code completeApprovedSkip} 复用它的并行组语义
     * （后序在组里要整组一起推、组的范围按生产工单取），在跳序域另写一份必然漏项。
     *
     * <p>这一跳要经 Spring 代理调用才会带上工序域那一份事务；跳序域与工序域本来就互为上下游
     * （本类被 {@code skipOpTask} 调、生效又要调回去），所以注入打 {@code @Lazy} 解开创建期的环。
     *
     * <p><b>也是人工重试入口</b>：动作失败时记录已是 {@code EFFECTIVE} 而工序还停在原状态，
     * 这个不一致既能查（两张表的状态对不上）也能补——重跑本方法即可。幂等由三道约束共同保证：
     * <ul>
     *   <li>工序已是 {@code SKIPPED} 时不再推工序，只补后序</li>
     *   <li>推工序走带 {@code version} 条件的更新，并发下只有一个能改到</li>
     *   <li>流水幂等键 {@code BPM:{记录编号}:OP_SKIP_APPROVE:0} 由 businessKey 派生，
     *       {@code mes_wip_txn} 的唯一索引兜住重复留痕</li>
     * </ul>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void takeEffectApprovedSkip(Long skipRecordId) {
        OpSkipRecordDO record = requireRecord(skipRecordId);
        // 只让"审批已通过且记录已置生效"的跳序生效：待审批、已撤销都动不了工序
        if (!MesSkipRecordStatusEnum.isEffective(record.getStatus())
                || !MesSkipApproveStatusEnum.isApproved(record.getApproveStatus())) {
            return;
        }
        OpTaskDO task = requireOpTask(record.getOpTaskId());
        WorkOrderDO workOrder = requireWorkOrder(task.getWorkOrderId());
        String idempotencyKey = approveIdempotencyKey(skipRecordId);
        if (!MesOpTaskStatusEnum.SKIPPED.name().equals(task.getStatus())) {
            // 状态机裁决：只有未开工态（PENDING / READY）有 skip 出边。审批拖着的这段时间里这道工序
            // 可能已经开工，此时抛 OP_TASK_STATUS_INVALID、一行不改——宁可让人来看，
            // 也不跳过一道正在加工的工序
            String fromStatus = task.getStatus();
            String toStatus = MesOpTaskStateMachine.next(fromStatus, MesOpTaskStateMachine.ACTION_SKIP);
            advanceToSkipped(task, toStatus);
            // 实际跳过时间在真正跳过这一刻落，不在审批回写时落：回写时工序还没跳，提前落就是假数据
            opSkipRecordMapper.updateById(new OpSkipRecordDO()
                    .setId(skipRecordId)
                    .setSkipTime(LocalDateTime.now()));
            recordSkipTxn(task, workOrder.getProdOrderId(), fromStatus, toStatus, idempotencyKey,
                    "审批通过跳序");
        }
        opTaskDomainService.completeApprovedSkip(task.getId(), idempotencyKey);
    }

    /** 跳序审批生效动作的幂等键：{@code BPM:{跳序记录编号}:OP_SKIP_APPROVE:0}，由 businessKey 派生。 */
    private String approveIdempotencyKey(Long skipRecordId) {
        return String.join(KEY_SEPARATOR, SOURCE_MODULE_BPM, String.valueOf(skipRecordId),
                ACTION_OP_SKIP_APPROVE, WHOLE_TASK_LINE_NO);
    }

    /**
     * 撤销的三件事：记录置 {@code REVOKED}、已生效的把工序退回 {@code READY}、写反向流水。
     *
     * <p>记录的更新带 {@code WHERE status IN (PENDING, EFFECTIVE)} 条件，{@code mes_op_skip_record}
     * 没有 version 列，这个条件就是它的乐观锁：并发下只有一个请求能撤销成功，另一个更新 0 行直接报状态非法。
     */
    private void doRevoke(OpSkipRecordDO record, OpTaskDO task, MesRevokeSkipBO bo, String idempotencyKey) {
        boolean effective = MesSkipRecordStatusEnum.isEffective(record.getStatus());
        LambdaUpdateWrapper<OpSkipRecordDO> wrapper = new LambdaUpdateWrapper<OpSkipRecordDO>()
                .eq(OpSkipRecordDO::getId, record.getId())
                .in(OpSkipRecordDO::getStatus, MesSkipRecordStatusEnum.PENDING.name(),
                        MesSkipRecordStatusEnum.EFFECTIVE.name());
        if (opSkipRecordMapper.update(new OpSkipRecordDO()
                .setStatus(MesSkipRecordStatusEnum.REVOKED.name())
                .setRemark(bo.getReason()), wrapper) == 0) {
            throw exception(OP_SKIP_RECORD_STATUS_INVALID);
        }
        record.setStatus(MesSkipRecordStatusEnum.REVOKED.name());

        WorkOrderDO workOrder = requireWorkOrder(task.getWorkOrderId());
        String fromStatus = task.getStatus();
        // 待审批的记录被撤时工序从来没变过 SKIPPED，只改记录；流水仍写一条，前后状态相同，
        // 记的是"这道工序上发生过一次撤销"
        String toStatus = effective ? revertToReady(task) : fromStatus;
        recordSkipTxn(task, workOrder.getProdOrderId(), fromStatus, toStatus, idempotencyKey,
                StrUtil.isBlank(bo.getReason()) ? "撤销跳序" : bo.getReason());
    }

    /**
     * 已跳过的工序退回待开工。
     *
     * <p>目标状态由 {@code MesOpTaskStateMachine} 的 {@code revokeSkip} 边裁决，不硬写。护栏是
     * {@code WHERE status = SKIPPED AND version = 读到的版本}：状态条件挡住"这道工序其实不是跳过来的"，
     * 版本条件挡住并发。更新 0 行时报并发冲突而不是静默返回，否则会出现"记录已撤销、工序还停在已跳过"
     * 的半截状态。
     */
    private String revertToReady(OpTaskDO task) {
        String toStatus = MesOpTaskStateMachine.next(
                task.getStatus(), MesOpTaskStateMachine.ACTION_REVOKE_SKIP);
        int currentVersion = task.getVersion() == null ? MesConstants.INIT_VERSION : task.getVersion();
        LambdaUpdateWrapper<OpTaskDO> wrapper = new LambdaUpdateWrapper<OpTaskDO>()
                .eq(OpTaskDO::getId, task.getId())
                .eq(OpTaskDO::getStatus, MesOpTaskStatusEnum.SKIPPED.name())
                .eq(OpTaskDO::getVersion, currentVersion);
        if (opTaskMapper.update(OpTaskDO.builder()
                .status(toStatus).version(currentVersion + 1).build(), wrapper) == 0) {
            throw exception(OP_TASK_CONCURRENT_CONFLICT);
        }
        task.setStatus(toStatus).setVersion(currentVersion + 1);
        return toStatus;
    }

    /** 带乐观锁把工序任务推到已跳过，目标状态由状态机给出。 */
    private void advanceToSkipped(OpTaskDO task, String toStatus) {
        int currentVersion = task.getVersion() == null ? MesConstants.INIT_VERSION : task.getVersion();
        LambdaUpdateWrapper<OpTaskDO> wrapper = new LambdaUpdateWrapper<OpTaskDO>()
                .eq(OpTaskDO::getId, task.getId())
                .eq(OpTaskDO::getVersion, currentVersion);
        if (opTaskMapper.update(OpTaskDO.builder()
                .status(toStatus).version(currentVersion + 1).build(), wrapper) == 0) {
            throw exception(OP_TASK_CONCURRENT_CONFLICT);
        }
        task.setStatus(toStatus).setVersion(currentVersion + 1);
    }

    /**
     * 写一条跳序流水。
     *
     * <p>{@code SKIP} 类型按 04 文档数量记 0（跳序不产出也不消耗）。{@code from_op_task_id} 与
     * {@code to_op_task_id} 记这道工序的前序与后序：读流水时最需要知道的是"这一跳把哪两道工序接上了"。
     */
    private void recordSkipTxn(OpTaskDO task, Long prodOrderId, String fromStatus, String toStatus,
                               String idempotencyKey, String remark) {
        wipTxnRecorder.record(new MesWipTxnBO()
                .setProdOrderId(prodOrderId)
                .setWorkOrderId(task.getWorkOrderId())
                .setOpTaskId(task.getId())
                .setTxnType(MesWipTxnTypeEnum.SKIP)
                .setFromStatus(fromStatus)
                .setToStatus(toStatus)
                .setBizType(MesBizTypeEnum.OP_TASK)
                .setBizNo(task.getTaskNo())
                .setFromOpTaskId(task.getPrevTaskId())
                .setToOpTaskId(task.getNextTaskId())
                .setIdempotencyKey(idempotencyKey)
                .setRemark(remark));
    }

    /**
     * 组装幂等命令。
     *
     * <p>幂等键调用方给了以它为准，留空时按四段格式 {@code {来源模块}:{工序任务号}:{命令类型}:0} 派生，
     * 与 08 文档的幂等键全表一致。
     */
    private MesCommandReqDTO buildCommand(MesCommandReqDTO source, MesCommandTypeEnum commandType, String taskNo) {
        Assert.notBlank(source.getSourceModule(), "来源模块不能为空，它是幂等键的第一段");
        SkipCommand command = new SkipCommand();
        command.setSourceModule(source.getSourceModule());
        command.setSourceBizNo(StrUtil.isBlank(source.getSourceBizNo()) ? taskNo : source.getSourceBizNo());
        command.setSourceBizLineNo(source.getSourceBizLineNo());
        command.setIdempotencyKey(StrUtil.isBlank(source.getIdempotencyKey())
                ? String.join(KEY_SEPARATOR, source.getSourceModule(), taskNo,
                commandType.name(), WHOLE_TASK_LINE_NO)
                : source.getIdempotencyKey());
        return command;
    }

    /** 可选维度落 0 不落 null，与库表 NOT NULL DEFAULT 0 的口径一致。 */
    private Long nvl(Long value) {
        return value == null ? MesConstants.NONE_ID : value;
    }

    private OpSkipRecordDO requireRecord(Long id) {
        OpSkipRecordDO record = id == null ? null : opSkipRecordMapper.selectById(id);
        if (record == null) {
            throw exception(OP_SKIP_RECORD_NOT_EXISTS);
        }
        return record;
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

    /** 幂等命令载体，{@link MesCommandReqDTO} 是抽象类，跳序域给它一个最小实现。 */
    private static class SkipCommand extends MesCommandReqDTO {
    }

}
