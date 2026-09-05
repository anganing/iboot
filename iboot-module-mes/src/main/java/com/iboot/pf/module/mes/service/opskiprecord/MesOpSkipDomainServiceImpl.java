package com.iboot.pf.module.mes.service.opskiprecord;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.iboot.pf.module.mes.service.command.MesCommandService;
import com.iboot.pf.module.mes.service.opskiprecord.bo.MesOpSkipBO;
import com.iboot.pf.module.mes.service.opskiprecord.bo.MesRevokeSkipBO;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import com.iboot.pf.module.mes.service.wiptxn.bo.MesWipTxnBO;
import jakarta.annotation.Resource;
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
public class MesOpSkipDomainServiceImpl implements MesOpSkipDomainService {

    /** 幂等键的段分隔符，格式见 08 文档的幂等键全表。 */
    private static final String KEY_SEPARATOR = ":";
    /** 工序任务级动作没有行号，第四段固定 0。 */
    private static final String WHOLE_TASK_LINE_NO = "0";

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
            // TODO T3-07 发起 BPM 审批：以本条记录为业务单据发起流程，审批通过后再走生效那一支
            // （置记录 EFFECTIVE、工序 -> SKIPPED、写 SKIP 流水、推后序）。本阶段不接 BPM，
            // 记录停在 PENDING 等人处理，工序任务状态一行不动
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
