package com.iboot.pf.module.mes.service.trace;

import com.iboot.pf.module.mes.controller.admin.trace.vo.TraceExcelRowRespVO;
import com.iboot.pf.module.mes.controller.admin.trace.vo.TraceExportReqVO;
import com.iboot.pf.module.mes.enums.MesTraceDirectionEnum;
import com.iboot.pf.module.mes.enums.MesTraceStartTypeEnum;
import com.iboot.pf.module.mes.service.trace.bo.MesTraceNodeBO;
import com.iboot.pf.module.mes.service.trace.bo.MesTraceQueryBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

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

    @Override
    public List<TraceExcelRowRespVO> exportTraceExcel(TraceExportReqVO reqVO) {
        // 导出复用查询端的树：拍平的输入与页面渲染的是同一份数据，页面所见即导出所得
        MesTraceNodeBO root = MesTraceDirectionEnum.BACKWARD.name().equals(reqVO.getDirection())
                ? traceBackward(reqVO.toQueryBO())
                : traceForward(reqVO.toQueryBO());
        MesTraceStartTypeEnum startType = MesTraceStartTypeEnum.ofName(reqVO.getStartType());
        MesTraceDirectionEnum direction = MesTraceDirectionEnum.ofName(reqVO.getDirection());
        return MesTraceExcelFlattener.flatten(root,
                direction == null ? reqVO.getDirection() : direction.getName(),
                startType == null ? reqVO.getStartType() : startType.getName(),
                reqVO.getStartValue());
    }

}
