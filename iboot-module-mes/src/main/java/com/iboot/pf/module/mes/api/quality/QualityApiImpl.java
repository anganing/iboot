package com.iboot.pf.module.mes.api.quality;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.module.mes.api.quality.dto.CreateDispositionReqDTO;
import com.iboot.pf.module.mes.api.quality.dto.CreateQcTaskReqDTO;
import com.iboot.pf.module.mes.api.quality.dto.QcResultSubmitReqDTO;
import com.iboot.pf.module.mes.api.quality.dto.QcTaskRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/** 质量 API 实现。质量结论由 MES 维护，库存质量维度由 WMS 联动。 */
@Service
@Validated
public class QualityApiImpl implements QualityApi {

    @Override
    public QcTaskRespDTO createQualityTask(CreateQcTaskReqDTO reqDTO) {
        throw new UnsupportedOperationException("质检任务创建命令尚未接入领域规则");
    }

    @Override
    public void submitQualityResult(QcResultSubmitReqDTO reqDTO) {
        throw new UnsupportedOperationException("检验结果提交命令尚未接入领域规则");
    }

    @Override
    public void createDisposition(CreateDispositionReqDTO reqDTO) {
        throw new UnsupportedOperationException("不合格品处置命令尚未接入领域规则");
    }

    @Override
    @DataPermission(enable = false)
    public QcTaskRespDTO getQualityTask(Long id) {
        throw new UnsupportedOperationException("质检任务查询尚未接入领域 Service");
    }

}
