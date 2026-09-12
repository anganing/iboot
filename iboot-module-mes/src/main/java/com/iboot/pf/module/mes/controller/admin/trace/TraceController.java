package com.iboot.pf.module.mes.controller.admin.trace;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.trace.vo.TraceBackwardReqVO;
import com.iboot.pf.module.mes.controller.admin.trace.vo.TraceExcelRowRespVO;
import com.iboot.pf.module.mes.controller.admin.trace.vo.TraceExportReqVO;
import com.iboot.pf.module.mes.controller.admin.trace.vo.TraceForwardReqVO;
import com.iboot.pf.module.mes.controller.admin.trace.vo.TraceNodeRespVO;
import com.iboot.pf.module.mes.service.trace.MesTraceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * 批次追溯管理端接口。
 *
 * <p>只读查询域：追溯只做查询（08 文档），无新增修改删除。正向拉出到出货客户，
 * 反向拉到原料供应商，两个查询端点复用同一权限码 {@code mes:trace:query}；
 * 导出单独一个权限码 {@code mes:trace:export}，因为交付材料是给外部看的动作。
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

    @GetMapping("/backward")
    @Operation(summary = "批次反向追溯", description = "从成品批次 / 生产工单 / 销售出库单出发，一次反查到原料批次与供应商的完整链路树")
    @PreAuthorize("@ss.hasPermission('mes:trace:query')")
    public CommonResult<TraceNodeRespVO> traceBackward(@Valid TraceBackwardReqVO reqVO) {
        return success(TraceNodeRespVO.of(
                traceService.traceBackward(reqVO.toQueryBO())));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出批次追溯链路 Excel", description = "按方向取回完整链路树后前序拍平成行，含层级与路径列，供客户审核交材料")
    @PreAuthorize("@ss.hasPermission('mes:trace:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTraceExcel(@Valid TraceExportReqVO reqVO,
                                 HttpServletResponse response) throws IOException {
        // 拍平与枚举中文化都在 Service 里做完，Controller 只写流（08 文档：补全逻辑不写在 Controller）
        List<TraceExcelRowRespVO> rows = traceService.exportTraceExcel(reqVO);
        ExcelUtils.write(response, "MES-批次追溯-" + reqVO.getStartValue() + ".xls", "追溯链路",
                TraceExcelRowRespVO.class, rows);
    }

}
