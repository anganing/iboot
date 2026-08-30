package com.iboot.pf.module.aps.controller.admin.solversnapshot;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.CommonResult;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

import com.iboot.pf.framework.excel.core.util.ExcelUtils;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.*;

import com.iboot.pf.module.aps.controller.admin.solversnapshot.vo.*;
import com.iboot.pf.module.aps.service.solversnapshot.SolverSnapshotService;

@Tag(name = "管理后台 - 求解快照")
@RestController
@RequestMapping("/aps/solver-snapshot")
@Validated
public class SolverSnapshotController {

    @Resource
    private SolverSnapshotService solverSnapshotService;

    @PostMapping("/create")
    @Operation(summary = "创建求解快照")
    @PreAuthorize("@ss.hasPermission('aps:solver-snapshot:create')")
    public CommonResult<Long> createSolverSnapshot(@Valid @RequestBody SolverSnapshotSaveReqVO createReqVO) {
        return success(solverSnapshotService.createSolverSnapshot(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新求解快照")
    @PreAuthorize("@ss.hasPermission('aps:solver-snapshot:update')")
    public CommonResult<Boolean> updateSolverSnapshot(@Valid @RequestBody SolverSnapshotSaveReqVO updateReqVO) {
        solverSnapshotService.updateSolverSnapshot(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除求解快照")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('aps:solver-snapshot:delete')")
    public CommonResult<Boolean> deleteSolverSnapshot(@RequestParam("id") Long id) {
        solverSnapshotService.deleteSolverSnapshot(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除求解快照")
                @PreAuthorize("@ss.hasPermission('aps:solver-snapshot:delete')")
    public CommonResult<Boolean> deleteSolverSnapshotList(@RequestParam("ids") List<Long> ids) {
        solverSnapshotService.deleteSolverSnapshotListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得求解快照")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('aps:solver-snapshot:query')")
    public CommonResult<SolverSnapshotRespVO> getSolverSnapshot(@RequestParam("id") Long id) {
        return success(solverSnapshotService.getSolverSnapshot(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得求解快照分页")
    @PreAuthorize("@ss.hasPermission('aps:solver-snapshot:query')")
    public CommonResult<PageResult<SolverSnapshotRespVO>> getSolverSnapshotPage(@Valid SolverSnapshotPageReqVO pageReqVO) {
        return success(solverSnapshotService.getSolverSnapshotPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出求解快照 Excel")
    @PreAuthorize("@ss.hasPermission('aps:solver-snapshot:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSolverSnapshotExcel(@Valid SolverSnapshotPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SolverSnapshotRespVO> list = solverSnapshotService.getSolverSnapshotPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "求解快照.xls", "数据", SolverSnapshotRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入求解快照模板")
    @PreAuthorize("@ss.hasPermission('aps:solver-snapshot:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "求解快照导入模板.xls", "数据",
                SolverSnapshotImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入求解快照")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('aps:solver-snapshot:import')")
    public CommonResult<SolverSnapshotImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<SolverSnapshotImportExcelVO> list = ExcelUtils.read(file, SolverSnapshotImportExcelVO.class);
        return success(solverSnapshotService.importSolverSnapshotList(list));
    }

}