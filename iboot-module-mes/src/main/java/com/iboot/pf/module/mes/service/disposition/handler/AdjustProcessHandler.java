package com.iboot.pf.module.mes.service.disposition.handler;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDispositionTypeEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStateMachine;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_NOT_EXISTS;

/** 调整工艺处置：将关联工序挂起，阻止继续生产。 */
@Component
public class AdjustProcessHandler implements MesDispositionHandler {

    @Resource
    private QcTaskMapper qcTaskMapper;
    @Resource
    private ProdReportMapper prodReportMapper;
    @Resource
    private OpTaskMapper opTaskMapper;

    @Override
    public String type() {
        return MesDispositionTypeEnum.ADJUST_PROCESS.name();
    }

    @Override
    public DispositionResult execute(DispositionDO disposition) {
        QcTaskDO qcTask = qcTaskMapper.selectById(disposition.getTaskId());
        if (qcTask == null) {
            throw exception(QC_TASK_NOT_EXISTS);
        }
        Long opTaskId = qcTask.getBizLineId();
        if (MesBizTypeEnum.PROD_REPORT.name().equals(qcTask.getBizType())) {
            ProdReportDO report = prodReportMapper.selectByReportNo(qcTask.getBizNo());
            opTaskId = report == null ? opTaskId : report.getOpTaskId();
        }
        OpTaskDO task = opTaskMapper.selectById(opTaskId);
        if (task == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        int version = task.getVersion() == null ? MesConstants.INIT_VERSION : task.getVersion();
        String target = MesOpTaskStateMachine.next(task.getStatus(), MesOpTaskStateMachine.ACTION_HOLD);
        if (opTaskMapper.update(OpTaskDO.builder().status(target).version(version + 1).build(),
                new LambdaUpdateWrapper<OpTaskDO>().eq(OpTaskDO::getId, task.getId())
                        .eq(OpTaskDO::getVersion, version)) == 0) {
            throw exception(OP_TASK_CONCURRENT_CONFLICT);
        }
        return new DispositionResult(MesBizTypeEnum.OP_TASK.name(), task.getTaskNo());
    }
}
