package com.iboot.pf.module.mes.service.disposition;

import com.iboot.pf.module.mes.service.disposition.bo.MesDispositionActionBO;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;

/** 不合格品处置领域动作。 */
public interface MesDispositionDomainService {

    Long createDisposition(DispositionDO disposition);

    void submitDisposition(MesDispositionActionBO bo);

    void executeDisposition(MesDispositionActionBO bo);

    void cancelDisposition(MesDispositionActionBO bo);
}
