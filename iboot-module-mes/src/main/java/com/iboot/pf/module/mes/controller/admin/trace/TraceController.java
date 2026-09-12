package com.iboot.pf.module.mes.controller.admin.trace;

import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.module.mes.controller.admin.trace.vo.TraceForwardReqVO;
import com.iboot.pf.module.mes.controller.admin.trace.vo.TraceNodeRespVO;
import com.iboot.pf.module.mes.service.trace.MesTraceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * 批次追溯管理端接口。
 *
 * <p>只读查询域：追溯只做查询（08 文档），无新增修改删除。
 * 反向追溯由 T5-02 在这里追加 {@code GET /trace/backward}。
 */
@Tag(name = "管理后台 - MES-批次追溯")
@RestController
@RequestMapping("/mes/trace")
@Validated
public class TraceController {

    @Resource
    private MesTraceService traceService;

    @GetMapping("/forward")
    @Operation(summary = "批次正向追溯", description = "从原料批次 / 生产工单 / 销售出库单出发，一次返回完整正向链路树")
    @PreAuthorize("@ss.hasPermission('mes:trace:query')")
    public CommonResult<TraceNodeRespVO> traceForward(@Valid TraceForwardReqVO reqVO) {
        return success(TraceNodeRespVO.of(
                traceService.traceForward(reqVO.toQueryBO())));
    }

}
