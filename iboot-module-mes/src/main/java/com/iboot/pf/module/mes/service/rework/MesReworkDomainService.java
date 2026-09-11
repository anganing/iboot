package com.iboot.pf.module.mes.service.rework;

import com.iboot.pf.module.mes.dal.dataobject.rework.ReworkDO;
import com.iboot.pf.module.mes.service.rework.bo.MesReworkActionBO;

/**
 * 返工领域动作。
 *
 * <p>创建由处置单同事务调用；复检结果由 FQC 结论分派方调用，领域服务不依赖质检 Handler。
 */
public interface MesReworkDomainService {

    Long createRework(ReworkDO rework);

    void releaseRework(MesReworkActionBO bo);

    void startRework(MesReworkActionBO bo);

    void finishRework(MesReworkActionBO bo);

    void cancelRework(MesReworkActionBO bo);

    void completeRework(Long reworkId, String idempotencyKey);

    void rejectRework(Long reworkId, String idempotencyKey);
}
