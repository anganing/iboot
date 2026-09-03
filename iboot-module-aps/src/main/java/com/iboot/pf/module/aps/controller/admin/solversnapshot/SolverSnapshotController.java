package com.iboot.pf.module.aps.controller.admin.solversnapshot;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.SolverSnapshotConstraintMatchRespVO;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.SolverSnapshotPageReqVO;
import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.SolverSnapshotRespVO;
import com.iboot.pf.module.aps.service.solversnapshot.ApsSolverSnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * APS-求解快照。
 *
 * <p>快照是求解过程留痕，只提供查询与导出：写入由求解流程负责，放开人工改写就没法用它复现问题。
 */
@Tag(name = "管理后台 - APS-求解快照")
@RestController
@RequestMapping("/aps/solver-snapshot")
@Validated
public class SolverSnapshotController {

    @Resource
    private ApsSolverSnapshotService solverSnapshotService;

    @GetMapping("/get")
    @Operation(summary = "获得求解快照")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:solver-snapshot:query')")
    public CommonResult<SolverSnapshotRespVO> getSolverSnapshot(@RequestParam("id") Long id) {
        return success(solverSnapshotService.getSolverSnapshot(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得求解快照分页")
    @PreAuthorize("@ss.hasPermission('aps:solver-snapshot:query')")
    public CommonResult<PageResult<SolverSnapshotRespVO>> getSolverSnapshotPage(
            @Valid SolverSnapshotPageReqVO pageReqVO) {
        return success(solverSnapshotService.getSolverSnapshotPage(pageReqVO));
    }

    /**
     * 按排程版本取约束命中分析。
     *
     * <p>存在 {@code /get} 的情况下还要这个接口，是因为使用场景不同：从排程版本页问"这版为什么是
     * 这个分数"时，人手上只有 versionId，不该先去快照列表里翻编号。返回的是解析后的对照表，
     * 而不是原始 JSON。
     */
    @GetMapping("/constraint-match")
    @Operation(summary = "获得排程版本的约束命中分析")
    @Parameter(name = "versionId", description = "排程版本编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:solver-snapshot:query')")
    public CommonResult<SolverSnapshotConstraintMatchRespVO> getConstraintMatch(
            @RequestParam("versionId") Long versionId) {
        return success(solverSnapshotService.getConstraintMatchByVersionId(versionId));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出求解快照 Excel")
    @PreAuthorize("@ss.hasPermission('aps:solver-snapshot:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSolverSnapshotExcel(@Valid SolverSnapshotPageReqVO pageReqVO,
                                          HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SolverSnapshotRespVO> list = solverSnapshotService.getSolverSnapshotPage(pageReqVO).getList();
        ExcelUtils.write(response, "APS-求解快照.xls", "数据", SolverSnapshotRespVO.class, list);
    }

}
