package com.iboot.pf.module.mes.service.disposition.handler;

import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;

/** 一种处置类型对应一个执行器，禁止在领域服务中堆叠类型分支。 */
public interface MesDispositionHandler {

    String type();

    DispositionResult execute(DispositionDO disposition);
}
