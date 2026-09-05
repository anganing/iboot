package com.iboot.pf.module.mes.api.quality;

import com.iboot.pf.module.mes.api.quality.dto.CreateDispositionReqDTO;
import com.iboot.pf.module.mes.api.quality.dto.CreateQcTaskReqDTO;
import com.iboot.pf.module.mes.api.quality.dto.QcResultSubmitReqDTO;
import com.iboot.pf.module.mes.api.quality.dto.QcTaskRespDTO;

/**
 * 质检任务、检验结果与不合格品处置 API 门面。
 *
 * <p>质量结论由 MES 维护，库存的质量维度由 WMS 联动：两边各写各自的账，靠处置单串起来。
 */
public interface QualityApi {

    /**
     * 创建质检任务，检验方案由规则解析后锁定。
     *
     * @param reqDTO 创建命令
     * @return 质检任务事实
     */
    QcTaskRespDTO createQualityTask(CreateQcTaskReqDTO reqDTO);

    /**
     * 提交检验结果，任务级结论由检验项判定汇总。
     *
     * @param reqDTO 提交命令
     */
    void submitQualityResult(QcResultSubmitReqDTO reqDTO);

    /**
     * 创建不合格品处置单，按处置类型联动返工、报废或让步接收。
     *
     * @param reqDTO 创建命令
     */
    void createDisposition(CreateDispositionReqDTO reqDTO);

    /**
     * 查询单个质检任务。
     *
     * @param id 质检任务编号
     * @return 质检任务事实，不存在时返回 null
     */
    QcTaskRespDTO getQualityTask(Long id);

}
