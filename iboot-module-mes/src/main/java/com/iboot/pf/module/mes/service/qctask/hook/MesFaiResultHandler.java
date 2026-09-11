package com.iboot.pf.module.mes.service.qctask.hook;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStateMachine;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;

/**
 * 首件检验结论处理。
 *
 * <p>合格不需要写业务状态，后续开工门禁按 FAI 任务的合格结论自行放行；拒收则立即把对应工序
 * 挂起，避免仍在加工的现场继续批量生产。处置完成后沿用工序的 resume 动作恢复。
 */
@Component
public class MesFaiResultHandler implements MesQcResultHandler {

    @Resource
    private OpTaskMapper opTaskMapper;

    @Override
    public String triggerPoint() {
        return MesQcTriggerPointEnum.FAI.name();
    }

    /** 处理已经提交结论的 FAI 任务。 */
    @Override
    public void onConclusion(QcTaskDO qcTask) {
        if (!MesQcConclusionEnum.REJECTED.name().equals(qcTask.getConclusion())) {
            return;
        }
        OpTaskDO opTask = opTaskMapper.selectByTaskNo(qcTask.getBizNo());
        if (opTask == null) {
            throw exception(OP_TASK_NOT_EXISTS);
        }
        String targetStatus = MesOpTaskStateMachine.next(opTask.getStatus(), MesOpTaskStateMachine.ACTION_HOLD);
        int version = opTask.getVersion() == null ? MesConstants.INIT_VERSION : opTask.getVersion();
        if (opTaskMapper.update(OpTaskDO.builder().status(targetStatus).version(version + 1).build(),
                new LambdaUpdateWrapper<OpTaskDO>().eq(OpTaskDO::getId, opTask.getId())
                        .eq(OpTaskDO::getVersion, version)) == 0) {
            throw exception(OP_TASK_CONCURRENT_CONFLICT);
        }
    }

}
