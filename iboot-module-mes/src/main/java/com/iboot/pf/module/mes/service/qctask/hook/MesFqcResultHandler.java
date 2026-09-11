package com.iboot.pf.module.mes.service.qctask.hook;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStateMachine;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.service.prodreport.MesProgressPropagator;
import com.iboot.pf.module.mes.service.rework.MesReworkDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;

/** FQC 合格后释放末道工序；拒收保持待检并等待处置或返工。 */
@Component
public class MesFqcResultHandler implements MesQcResultHandler {

    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private MesProgressPropagator progressPropagator;
    @Resource
    private MesReworkDomainService reworkDomainService;

    @Override
    public String triggerPoint() {
        return MesQcTriggerPointEnum.FQC.name();
    }

    @Override
    public void onConclusion(QcTaskDO qcTask) {
        if (com.iboot.pf.module.mes.enums.MesBizTypeEnum.REWORK.name().equals(qcTask.getBizType())) {
            if (MesQcConclusionEnum.QUALIFIED.name().equals(qcTask.getConclusion())
                    || MesQcConclusionEnum.CONCESSION.name().equals(qcTask.getConclusion())) {
                reworkDomainService.completeRework(qcTask.getBizLineId(), "MES:" + qcTask.getTaskNo() + ":FQC_PASS:0");
            } else if (MesQcConclusionEnum.REJECTED.name().equals(qcTask.getConclusion())) {
                reworkDomainService.rejectRework(qcTask.getBizLineId(), "MES:" + qcTask.getTaskNo() + ":FQC_REJECT:0");
            }
            return;
        }
        if (!MesQcConclusionEnum.QUALIFIED.name().equals(qcTask.getConclusion())
                && !MesQcConclusionEnum.CONCESSION.name().equals(qcTask.getConclusion())) {
            return;
        }
        OpTaskDO task = opTaskMapper.selectById(qcTask.getBizLineId());
        if (task == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        int version = task.getVersion() == null ? MesConstants.INIT_VERSION : task.getVersion();
        String target = MesOpTaskStateMachine.next(task.getStatus(), MesOpTaskStateMachine.ACTION_QC_PASS_AND_FINISH);
        if (opTaskMapper.update(OpTaskDO.builder().status(target).version(version + 1).build(),
                new LambdaUpdateWrapper<OpTaskDO>().eq(OpTaskDO::getId, task.getId())
                        .eq(OpTaskDO::getVersion, version)) == 0) {
            throw exception(OP_TASK_CONCURRENT_CONFLICT);
        }
        task.setStatus(target).setVersion(version + 1);
        progressPropagator.propagate(task, MesConstants.ZERO_QTY, MesConstants.ZERO_QTY,
                "MES:" + qcTask.getTaskNo() + ":FQC_PASS:0");
    }
}
