package com.iboot.pf.module.mes.service.qctask.hook;

import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;

/** 不同检验点的结论联动处理器。 */
public interface MesQcResultHandler {

    /** 对应的质检触发点。 */
    String triggerPoint();

    /** 质检任务结论已落库后执行下游放行或拦截。 */
    void onConclusion(QcTaskDO task);
}
