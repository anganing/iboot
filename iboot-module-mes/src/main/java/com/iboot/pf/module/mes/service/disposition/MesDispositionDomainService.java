package com.iboot.pf.module.mes.service.disposition;

import com.iboot.pf.module.mes.service.disposition.bo.MesDispositionActionBO;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;

/** 不合格品处置领域动作。 */
public interface MesDispositionDomainService {

    Long createDisposition(DispositionDO disposition);

    void submitDisposition(MesDispositionActionBO bo);

    void executeDisposition(MesDispositionActionBO bo);

    void cancelDisposition(MesDispositionActionBO bo);

    /**
     * 回写让步接收的审批终态（三层幂等）。
     *
     * <p>通过：单据置已批准并在事务提交后执行处置（质量状态过账）；驳回/撤销：单据回新建，库存不动。
     *
     * @param dispositionId     处置单编号
     * @param processInstanceId 流程实例编号
     * @param status            流程终态，取值见 {@code BpmProcessInstanceStatusEnum}
     * @param reason            审批意见或驳回原因
     */
    void updateConcessionApprovalStatus(Long dispositionId, String processInstanceId,
                                        Integer status, String reason);

    /**
     * 回写报废处置的审批终态（三层幂等）。
     *
     * <p>通过：单据置已批准并在事务提交后执行处置（报废出库过账）；驳回/撤销：单据回新建，库存不动。
     *
     * @param dispositionId     处置单编号
     * @param processInstanceId 流程实例编号
     * @param status            流程终态，取值见 {@code BpmProcessInstanceStatusEnum}
     * @param reason            审批意见或驳回原因
     */
    void updateScrapApprovalStatus(Long dispositionId, String processInstanceId,
                                   Integer status, String reason);

}
