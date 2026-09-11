package com.iboot.pf.module.mes.api.quality;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.quality.dto.CreateDispositionReqDTO;
import com.iboot.pf.module.mes.api.quality.dto.CreateQcTaskReqDTO;
import com.iboot.pf.module.mes.api.quality.dto.QcResultSubmitReqDTO;
import com.iboot.pf.module.mes.api.quality.dto.QcTaskRespDTO;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.service.disposition.MesDispositionDomainService;
import com.iboot.pf.module.mes.service.qcresult.MesQcResultDomainService;
import com.iboot.pf.module.mes.service.qcresult.bo.MesSaveQcResultBO;
import com.iboot.pf.module.mes.service.qctask.MesQcTaskDomainService;
import com.iboot.pf.module.mes.service.qctask.bo.MesCreateQcTaskBO;
import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskConclusionBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/** 质量 API 实现。质量结论由 MES 维护，库存质量维度由 WMS 联动。 */
@Service
@Validated
@DataPermission(enable = false)
public class QualityApiImpl implements QualityApi {

    @Resource
    private MesQcTaskDomainService qcTaskDomainService;
    @Resource
    private MesQcResultDomainService qcResultDomainService;
    @Resource
    private MesDispositionDomainService dispositionDomainService;
    @Resource
    private QcTaskMapper qcTaskMapper;

    @Override
    public QcTaskRespDTO createQualityTask(CreateQcTaskReqDTO reqDTO) {
        MesCreateQcTaskBO bo = BeanUtils.toBean(reqDTO, MesCreateQcTaskBO.class);
        return qcTaskDomainService.createQcTask(bo)
                .map(this::getQualityTask).orElse(null);
    }

    @Override
    public void submitQualityResult(QcResultSubmitReqDTO reqDTO) {
        MesSaveQcResultBO saveBO = new MesSaveQcResultBO();
        saveBO.setTaskId(reqDTO.getQcTaskId());
        saveBO.setInspectorId(reqDTO.getInspectorId());
        saveBO.setResults(reqDTO.getItems().stream().map(item -> {
            MesSaveQcResultBO.Item result = new MesSaveQcResultBO.Item();
            result.setItemId(item.getItemId());
            result.setSampleNo(item.getSampleNo());
            result.setActualValue(item.getActualValue());
            return result;
        }).toList());
        qcResultDomainService.batchSaveResult(saveBO);
        MesQcTaskConclusionBO conclusionBO = new MesQcTaskConclusionBO();
        conclusionBO.setTaskId(reqDTO.getQcTaskId());
        conclusionBO.setSourceModule(reqDTO.getSourceModule());
        conclusionBO.setSourceBizNo(reqDTO.getSourceBizNo());
        conclusionBO.setSourceBizLineNo(reqDTO.getSourceBizLineNo());
        conclusionBO.setIdempotencyKey(reqDTO.getIdempotencyKey());
        conclusionBO.setConclusion(reqDTO.getConclusion());
        conclusionBO.setQtyQualified(reqDTO.getQtyQualified());
        conclusionBO.setQtyRejected(reqDTO.getQtyRejected());
        qcTaskDomainService.submitConclusion(conclusionBO);
    }

    @Override
    public void createDisposition(CreateDispositionReqDTO reqDTO) {
        DispositionDO disposition = new DispositionDO();
        disposition.setTaskId(reqDTO.getQcTaskId());
        disposition.setDispositionType(reqDTO.getDispositionType());
        disposition.setQty(reqDTO.getQty());
        disposition.setIdempotencyKey(reqDTO.getIdempotencyKey());
        disposition.setRemark(reqDTO.getRemark());
        dispositionDomainService.createDisposition(disposition);
    }

    @Override
    public QcTaskRespDTO getQualityTask(Long id) {
        QcTaskDO task = id == null ? null : qcTaskMapper.selectById(id);
        return task == null ? null : BeanUtils.toBean(task, QcTaskRespDTO.class);
    }

}
