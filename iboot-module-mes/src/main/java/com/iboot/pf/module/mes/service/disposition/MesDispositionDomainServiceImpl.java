package com.iboot.pf.module.mes.service.disposition;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.framework.security.core.util.SecurityFrameworkUtils;
import com.iboot.pf.module.bpm.api.task.BpmProcessInstanceApi;
import com.iboot.pf.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import com.iboot.pf.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.disposition.DispositionMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesDispositionStateMachine;
import com.iboot.pf.module.mes.enums.MesDispositionStatusEnum;
import com.iboot.pf.module.mes.enums.MesDispositionTypeEnum;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.service.bpm.MesBpmApprovalActionExecutor;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandService;
import com.iboot.pf.module.mes.service.disposition.bo.MesDispositionActionBO;
import com.iboot.pf.module.mes.service.disposition.handler.DispositionResult;
import com.iboot.pf.module.mes.service.disposition.handler.MesDispositionHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_CONCESSION_WITHOUT_REASON;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_HANDLER_NOT_FOUND;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_QTY_EXCEEDED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_TARGET_EMPTY;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_NOT_EXISTS;

/** 处置状态推进及六类联动编排。 */
@Slf4j
@Service
public class MesDispositionDomainServiceImpl implements MesDispositionDomainService {

    private static final String SOURCE_MODULE_MES = MesSourceModuleEnum.MES.name();
    /** 审批回调发起的联动，来源记 BPM，与人工执行在命令记录里区分开。 */
    private static final String SOURCE_MODULE_BPM = "BPM";
    /** 让步接收流程定义 key，与 {@code MesConcessionStatusListener} 共用同一个常量。 */
    public static final String PROCESS_KEY_CONCESSION = "mes-concession";
    /** 报废处置流程定义 key，与 {@code MesScrapStatusListener} 共用同一个常量。 */
    public static final String PROCESS_KEY_SCRAP = "mes-scrap";

    @Resource
    private DispositionMapper dispositionMapper;
    @Resource
    private MesCommandService commandService;
    @Resource
    private MesCodeService codeService;
    @Resource
    private QcTaskMapper qcTaskMapper;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;
    @Resource
    private MesBpmApprovalActionExecutor approvalActionExecutor;

    private final Map<String, MesDispositionHandler> handlers;

    public MesDispositionDomainServiceImpl(List<MesDispositionHandler> handlers) {
        this.handlers = handlers.stream().collect(Collectors.toMap(MesDispositionHandler::type,
                Function.identity(), (left, right) -> {
                    throw new IllegalStateException("处置类型存在重复执行器：" + left.type());
                }));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDisposition(DispositionDO disposition) {
        DispositionDO existed = StrUtil.isBlank(disposition.getIdempotencyKey()) ? null
                : dispositionMapper.selectByIdempotencyKey(disposition.getIdempotencyKey());
        if (existed != null) {
            return existed.getId();
        }
        QcTaskDO qcTask = disposition.getTaskId() == null ? null : qcTaskMapper.selectById(disposition.getTaskId());
        if (qcTask == null) {
            throw exception(QC_TASK_NOT_EXISTS);
        }
        if (disposition.getQty() == null || disposition.getQty().signum() <= 0
                || disposition.getQty().compareTo(qcTask.getQtyRejected()) > 0) {
            throw exception(DISPOSITION_QTY_EXCEEDED);
        }
        disposition.setId(null);
        disposition.setDispositionNo(codeService.generate(MesCodeRuleConstants.DISPOSITION,
                disposition.getDispositionNo()));
        disposition.setStatus(MesDispositionStatusEnum.DRAFT.name());
        dispositionMapper.insert(disposition);
        return disposition.getId();
    }

    /**
     * 提交处置单。六类处置分成两拨：需要审批的两拨与即时批准的四拨。
     *
     * <p>让步接收与报废处置必须过审批：前者把不合格品当合格用，后者直接减库存，都是没有留痕就查不到
     * 谁放行的动作。这一拨只推状态与发起流程，<b>一行库存都不动</b>；库存后果留到审批通过的回调里执行。
     * 其余四类（返工、调工艺、冻结、退供）本轮不设审批，提交即视为业务批准，由人工再点执行。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitDisposition(MesDispositionActionBO bo) {
        DispositionDO disposition = requireDisposition(bo.getDispositionId());
        if (MesDispositionTypeEnum.CONCESSION.name().equals(disposition.getDispositionType())
                && StrUtil.isBlank(disposition.getRemark())) {
            throw exception(DISPOSITION_CONCESSION_WITHOUT_REASON);
        }
        commandService.executeVoid(MesCommandTypeEnum.SUBMIT_DISPOSITION,
                command(bo, disposition, MesCommandTypeEnum.SUBMIT_DISPOSITION), () -> {
                    dispositionMapper.updateById(new DispositionDO().setId(disposition.getId())
                            .setStatus(MesDispositionStateMachine.next(disposition.getStatus(),
                                    MesDispositionStateMachine.ACTION_SUBMIT)));
                    String type = disposition.getDispositionType();
                    if (requiresApproval(type)) {
                        startApproval(disposition, type);
                        return;
                    }
                    // 不需要审批的类型：提交即业务批准，等着人工点执行
                    dispositionMapper.updateById(new DispositionDO().setId(disposition.getId())
                            .setStatus(MesDispositionStateMachine.next(MesDispositionStatusEnum.SUBMITTED.name(),
                                    MesDispositionStateMachine.ACTION_APPROVE)));
                });
    }

    /** 该处置类型是否需要走 BPM 审批。加新类型只改这一处。 */
    private boolean requiresApproval(String dispositionType) {
        return MesDispositionTypeEnum.CONCESSION.name().equals(dispositionType)
                || MesDispositionTypeEnum.SCRAP.name().equals(dispositionType);
    }

    /** 发起审批流程并回写实例编号，与提交处置单同事务：流程起了单据没落会留孤儿流程。 */
    private void startApproval(DispositionDO disposition, String dispositionType) {
        BpmProcessInstanceCreateReqDTO createReq = new BpmProcessInstanceCreateReqDTO();
        createReq.setProcessDefinitionKey(MesDispositionTypeEnum.SCRAP.name().equals(dispositionType)
                ? PROCESS_KEY_SCRAP : PROCESS_KEY_CONCESSION);
        createReq.setBusinessKey(String.valueOf(disposition.getId()));
        // 报废按数量分档，阈值判断在流程定义的条件网关里，Service 只负责把数量塞进变量
        createReq.setVariables(Map.of("qty", disposition.getQty() == null
                ? "0" : disposition.getQty().toPlainString()));
        String instanceId = processInstanceApi.createProcessInstance(
                SecurityFrameworkUtils.getLoginUserId(), createReq);
        dispositionMapper.updateById(new DispositionDO().setId(disposition.getId())
                .setProcessInstanceId(instanceId)
                .setApprovalStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeDisposition(MesDispositionActionBO bo) {
        DispositionDO disposition = requireDisposition(bo.getDispositionId());
        commandService.executeVoid(MesCommandTypeEnum.EXECUTE_DISPOSITION,
                command(bo, disposition, MesCommandTypeEnum.EXECUTE_DISPOSITION), () -> {
                    String executing = MesDispositionStateMachine.next(disposition.getStatus(),
                            MesDispositionStateMachine.ACTION_EXECUTE);
                    dispositionMapper.updateById(new DispositionDO().setId(disposition.getId()).setStatus(executing));
                    MesDispositionHandler handler = handlers.get(disposition.getDispositionType());
                    if (handler == null) {
                        throw exception(DISPOSITION_HANDLER_NOT_FOUND);
                    }
                    DispositionResult result = handler.execute(disposition);
                    if (result == null || StrUtil.isBlank(result.getTargetBizType()) || StrUtil.isBlank(result.getTargetBizNo())) {
                        throw exception(DISPOSITION_TARGET_EMPTY);
                    }
                    dispositionMapper.updateById(new DispositionDO().setId(disposition.getId())
                            .setStatus(MesDispositionStateMachine.next(executing, MesDispositionStateMachine.ACTION_DONE))
                            .setTargetBizType(result.getTargetBizType()).setTargetBizNo(result.getTargetBizNo()));
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelDisposition(MesDispositionActionBO bo) {
        DispositionDO disposition = requireDisposition(bo.getDispositionId());
        // 审批流程尚在运行时，业务单不能单方面取消，否则终态回调会面对已取消的处置单。
        if (BpmProcessInstanceStatusEnum.RUNNING.getStatus().equals(disposition.getApprovalStatus())) {
            throw exception(DISPOSITION_STATUS_INVALID);
        }
        dispositionMapper.updateById(new DispositionDO().setId(disposition.getId())
                .setStatus(MesDispositionStateMachine.next(disposition.getStatus(), MesDispositionStateMachine.ACTION_CANCEL)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConcessionApprovalStatus(Long dispositionId, String processInstanceId,
                                               Integer status, String reason) {
        applyApprovalResult(dispositionId, processInstanceId, status, reason, "让步接收");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScrapApprovalStatus(Long dispositionId, String processInstanceId,
                                          Integer status, String reason) {
        applyApprovalResult(dispositionId, processInstanceId, status, reason, "报废处置");
    }

    /**
     * 审批终态回写，两类处置共用同一套三层幂等，只有日志口径不同。
     *
     * <p>第一层只认终态；第二层校验实例编号与"当前确实处于审批中"；第三层带
     * {@code WHERE approval_status = RUNNING} 条件更新，并发或重投下只有一个线程能改到 1 行。
     * 第三层的受影响行数就是执行领域动作的许可证：拿不到就不执行，这是"重复回调不产生第二笔过账"
     * 的全部内容。
     *
     * <p>驳回与撤销把单据退回 {@code DRAFT}（走状态机的 {@code approvalReturn} 边，不在这里裸写状态值），
     * 语义是"这次提交不算"：单据还能改、还能重新提交。库存分文未动，无需任何冲销。
     *
     * <p>审批通过后的领域动作经 {@link MesBpmApprovalActionExecutor} 延迟到本事务提交之后执行。
     * 直接在这里调用的话，过账失败会把 Flowable 的事务标成 rollback-only，审批终态随流程一起回滚，
     * 审批人每次点通过都失败。人工重试入口是既有的执行处置单端点：审批通过后单据停在
     * {@code APPROVED}，前置校验（状态必须是已批准）已经放开，点执行即可补齐。
     */
    private void applyApprovalResult(Long dispositionId, String processInstanceId,
                                     Integer status, String reason, String bizName) {
        // 第一层：非终态（审批中、未开始）不改业务事实
        if (!BpmProcessInstanceStatusEnum.isProcessEndStatus(status)) {
            return;
        }
        // 单据不存在按"与本次回调无关"处理，不抛异常：回调跑在 Flowable 事务里，抛出去等于把
        // rollback-only 标记留给流程，审批终态照样回滚——和 T3-06 那个驳回回调 BUG 是同一个失效机制。
        // 流程还在跑而单据被删（逻辑删）是可能发生的，此时只留一条 warn 供对账。
        DispositionDO disposition = dispositionMapper.selectById(dispositionId);
        if (disposition == null) {
            log.warn("[applyApprovalResult] {}处置单不存在，忽略本次审批回调。dispositionId={}, instanceId={}",
                    bizName, dispositionId, processInstanceId);
            return;
        }
        // 第二层：只接受这张单正在等待的那一个流程实例，且当前确实处于审批中
        if (!processInstanceId.equals(disposition.getProcessInstanceId())
                || !BpmProcessInstanceStatusEnum.RUNNING.getStatus().equals(disposition.getApprovalStatus())) {
            return;
        }
        boolean approved = BpmProcessInstanceStatusEnum.APPROVE.getStatus().equals(status);
        // 状态迁移一律由状态机裁决：通过走 approve 边，驳回/撤销走 approvalReturn 边
        String targetStatus = MesDispositionStateMachine.next(disposition.getStatus(),
                approved ? MesDispositionStateMachine.ACTION_APPROVE
                        : MesDispositionStateMachine.ACTION_APPROVAL_RETURN);
        // 第三层：条件更新抢占终态处理权，重复投递与并发只有一个能拿到
        int updated = dispositionMapper.update(new DispositionDO()
                        .setId(dispositionId)
                        .setStatus(targetStatus)
                        .setApprovalStatus(status)
                        .setApprovalTime(LocalDateTime.now())
                        .setApprovalReason(reason),
                new LambdaUpdateWrapper<DispositionDO>()
                        .eq(DispositionDO::getId, dispositionId)
                        .eq(DispositionDO::getApprovalStatus,
                                BpmProcessInstanceStatusEnum.RUNNING.getStatus()));
        if (updated != 1) {
            return;
        }
        log.info("[applyApprovalResult] {}审批终态回写。dispositionNo={}, status={}",
                bizName, disposition.getDispositionNo(), status);
        if (!approved) {
            return;
        }
        approvalActionExecutor.executeAfterCommit(bizName + "处置执行", () -> {
            MesDispositionActionBO action = new MesDispositionActionBO();
            action.setDispositionId(dispositionId);
            action.setSourceModule(SOURCE_MODULE_BPM);
            // 执行器已经在独立新事务里跑这段，这里直接调本类方法即可（不需要再经代理取事务：
            // 事务边界由执行器统一给出，动作失败时整段回滚，单据停在 APPROVED 等人工重试）
            executeDisposition(action);
        });
    }

    private DispositionDO requireDisposition(Long id) {
        DispositionDO disposition = id == null ? null : dispositionMapper.selectById(id);
        if (disposition == null) {
            throw exception(DISPOSITION_NOT_EXISTS);
        }
        return disposition;
    }

    private MesCommandReqDTO command(MesDispositionActionBO source, DispositionDO disposition,
                                     MesCommandTypeEnum commandType) {
        MesDispositionActionBO command = new MesDispositionActionBO();
        command.setDispositionId(disposition.getId());
        command.setSourceModule(StrUtil.blankToDefault(source.getSourceModule(), SOURCE_MODULE_MES));
        command.setSourceBizNo(StrUtil.blankToDefault(source.getSourceBizNo(), disposition.getDispositionNo()));
        command.setSourceBizLineNo(source.getSourceBizLineNo());
        command.setIdempotencyKey(StrUtil.blankToDefault(source.getIdempotencyKey(),
                SOURCE_MODULE_MES + ":" + disposition.getDispositionNo() + ":" + commandType.name() + ":0"));
        return command;
    }
}
