package com.iboot.pf.module.mes.service.disposition;

import cn.hutool.core.util.StrUtil;
import com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil;
import com.iboot.pf.module.bpm.api.task.BpmProcessInstanceApi;
import com.iboot.pf.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import com.iboot.pf.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.iboot.pf.framework.security.core.util.SecurityFrameworkUtils;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.dal.mysql.disposition.DispositionMapper;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesDispositionStatusEnum;
import com.iboot.pf.module.mes.enums.MesDispositionTypeEnum;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesDispositionStateMachine;
import com.iboot.pf.module.mes.service.command.MesCommandService;
import com.iboot.pf.module.mes.service.disposition.bo.MesDispositionActionBO;
import com.iboot.pf.module.mes.service.disposition.handler.DispositionResult;
import com.iboot.pf.module.mes.service.disposition.handler.MesDispositionHandler;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_HANDLER_NOT_FOUND;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_CONCESSION_WITHOUT_REASON;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_TARGET_EMPTY;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_QTY_EXCEEDED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_NOT_EXISTS;

/** 处置状态推进及六类联动编排。 */
@Service
public class MesDispositionDomainServiceImpl implements MesDispositionDomainService {

    private static final String SOURCE_MODULE_MES = "MES";
    public static final String PROCESS_KEY_CONCESSION = "mes-concession";
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
                    if (MesDispositionTypeEnum.CONCESSION.name().equals(type)
                            || MesDispositionTypeEnum.SCRAP.name().equals(type)) {
                        String processKey = MesDispositionTypeEnum.SCRAP.name().equals(type)
                                ? PROCESS_KEY_SCRAP : PROCESS_KEY_CONCESSION;
                        BpmProcessInstanceCreateReqDTO createReq = new BpmProcessInstanceCreateReqDTO();
                        createReq.setProcessDefinitionKey(processKey);
                        createReq.setBusinessKey(String.valueOf(disposition.getId()));
                        // 报废分档审批的数量与物料标准成本由流程定义的条件网关使用
                        createReq.setVariables(Map.of("qty", disposition.getQty() != null
                                ? disposition.getQty().toPlainString() : "0"));
                        String instanceId = processInstanceApi.createProcessInstance(
                                SecurityFrameworkUtils.getLoginUserId(), createReq);
                        dispositionMapper.updateById(new DispositionDO().setId(disposition.getId())
                                .setProcessInstanceId(instanceId)
                                .setApprovalStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus()));
                    } else {
                        dispositionMapper.updateById(new DispositionDO().setId(disposition.getId())
                                .setStatus(MesDispositionStateMachine.next("SUBMITTED",
                                        MesDispositionStateMachine.ACTION_APPROVE)));
                    }
                });
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

    private DispositionDO requireDisposition(Long id) {
        DispositionDO disposition = dispositionMapper.selectById(id);
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
