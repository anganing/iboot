package com.iboot.pf.module.mes.service.trace;

import com.iboot.pf.module.mes.service.trace.bo.MesTraceNodeBO;
import com.iboot.pf.module.mes.service.trace.bo.MesTraceQueryBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 批次追溯查询实现。
 *
 * <p>正向委托给 {@link MesTraceForwardResolver}，反向委托给 {@link MesTraceBackwardResolver}，
 * 本类只做分发，两个方向独立演进。
 */
@Service
@Validated
public class MesTraceServiceImpl implements MesTraceService {

    @Resource
    private MesTraceForwardResolver forwardResolver;
    @Resource
    private MesTraceBackwardResolver backwardResolver;

    @Override
    public MesTraceNodeBO traceForward(MesTraceQueryBO query) {
        return forwardResolver.resolve(query);
    }

    @Override
    public MesTraceNodeBO traceBackward(MesTraceQueryBO query) {
        return backwardResolver.resolve(query);
    }

}
