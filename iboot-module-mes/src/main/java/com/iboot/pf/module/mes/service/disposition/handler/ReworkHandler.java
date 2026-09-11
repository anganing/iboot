package com.iboot.pf.module.mes.service.disposition.handler;

import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.rework.ReworkDO;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.rework.ReworkMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesDispositionTypeEnum;
import com.iboot.pf.module.mes.service.rework.MesReworkDomainService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_NOT_EXISTS;

/** 返工处置：生成真实返工任务。 */
@Component
public class ReworkHandler implements MesDispositionHandler {

    @Resource
    private QcTaskMapper qcTaskMapper;
    @Resource
    private ProdReportMapper prodReportMapper;
    @Resource
    private MesReworkDomainService reworkDomainService;
    @Resource
    private ReworkMapper reworkMapper;

    @Override
    public String type() {
        return MesDispositionTypeEnum.REWORK.name();
    }

    @Override
    public DispositionResult execute(DispositionDO disposition) {
        QcTaskDO qcTask = qcTaskMapper.selectById(disposition.getTaskId());
        if (qcTask == null) {
            throw exception(QC_TASK_NOT_EXISTS);
        }
        Long opTaskId = resolveOpTaskId(qcTask);
        ReworkDO rework = ReworkDO.builder()
                .sourceQcTaskId(qcTask.getId())
                .opTaskId(opTaskId)
                .qty(disposition.getQty())
                .remark(disposition.getRemark())
                .build();
        Long reworkId = reworkDomainService.createRework(rework);
        ReworkDO created = reworkMapper.selectById(reworkId);
        return new DispositionResult(MesBizTypeEnum.REWORK.name(), created.getReworkNo());
    }

    private Long resolveOpTaskId(QcTaskDO qcTask) {
        if (MesBizTypeEnum.OP_TASK.name().equals(qcTask.getBizType())) {
            return qcTask.getBizLineId();
        }
        if (MesBizTypeEnum.PROD_REPORT.name().equals(qcTask.getBizType())) {
            ProdReportDO report = prodReportMapper.selectByReportNo(qcTask.getBizNo());
            return report == null ? qcTask.getBizLineId() : report.getOpTaskId();
        }
        return qcTask.getBizLineId();
    }
}
