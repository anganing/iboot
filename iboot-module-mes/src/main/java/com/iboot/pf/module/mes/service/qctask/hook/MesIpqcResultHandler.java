package com.iboot.pf.module.mes.service.qctask.hook;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStateMachine;
import com.iboot.pf.module.mes.enums.MesProdReportStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_STATUS_INVALID;

/** IPQC 结论回写报工记录并解除或保留工序待检门禁。 */
@Component
public class MesIpqcResultHandler implements MesQcResultHandler {

    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private ProdReportMapper prodReportMapper;

    @Override
    public String triggerPoint() {
        return MesQcTriggerPointEnum.IPQC.name();
    }

    @Override
    public void onConclusion(QcTaskDO qcTask) {
        if (!MesQcConclusionEnum.QUALIFIED.name().equals(qcTask.getConclusion())) {
            return;
        }
        ProdReportDO report = prodReportMapper.selectByReportNo(qcTask.getBizNo());
        if (report == null) {
            throw exception(PROD_REPORT_NOT_EXISTS);
        }
        OpTaskDO opTask = opTaskMapper.selectById(report.getOpTaskId());
        if (opTask == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        String action = qtyReached(opTask)
                ? MesOpTaskStateMachine.ACTION_QC_PASS_AND_FINISH : MesOpTaskStateMachine.ACTION_QC_PASS;
        String targetStatus = MesOpTaskStateMachine.next(opTask.getStatus(), action);
        int version = opTask.getVersion() == null ? MesConstants.INIT_VERSION : opTask.getVersion();
        if (opTaskMapper.update(OpTaskDO.builder().status(targetStatus).version(version + 1).build(),
                new LambdaUpdateWrapper<OpTaskDO>().eq(OpTaskDO::getId, opTask.getId())
                        .eq(OpTaskDO::getVersion, version)) == 0) {
            throw exception(OP_TASK_CONCURRENT_CONFLICT);
        }
        if (prodReportMapper.update(new ProdReportDO().setStatus(MesProdReportStatusEnum.CONFIRMED.name()),
                new LambdaUpdateWrapper<ProdReportDO>().eq(ProdReportDO::getId, report.getId())
                        .eq(ProdReportDO::getStatus, MesProdReportStatusEnum.QC_PENDING.name())) == 0) {
            throw exception(PROD_REPORT_STATUS_INVALID);
        }
    }

    private boolean qtyReached(OpTaskDO opTask) {
        return nvl(opTask.getCompletedQty()).add(nvl(opTask.getScrapQty()))
                .compareTo(nvl(opTask.getPlannedQty())) >= 0;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? MesConstants.ZERO_QTY : value;
    }

}
