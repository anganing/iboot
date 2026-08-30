package com.iboot.pf.module.wms.controller.admin.batchmgr;

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

import com.iboot.pf.module.wms.controller.admin.batchmgr.vo.*;
import com.iboot.pf.module.wms.service.batchmgr.BatchService;

@Tag(name = "管理后台 - WMS-批次主档")
@RestController
@RequestMapping("/wms/batch")
@Validated
public class BatchController {

    @Resource
    private BatchService batchService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-批次主档")
    @PreAuthorize("@ss.hasPermission('wms:batch:create')")
    public CommonResult<Long> createBatch(@Valid @RequestBody BatchSaveReqVO createReqVO) {
        return success(batchService.createBatch(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-批次主档")
    @PreAuthorize("@ss.hasPermission('wms:batch:update')")
    public CommonResult<Boolean> updateBatch(@Valid @RequestBody BatchSaveReqVO updateReqVO) {
        batchService.updateBatch(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-批次主档")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:batch:delete')")
    public CommonResult<Boolean> deleteBatch(@RequestParam("id") Long id) {
        batchService.deleteBatch(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-批次主档")
                @PreAuthorize("@ss.hasPermission('wms:batch:delete')")
    public CommonResult<Boolean> deleteBatchList(@RequestParam("ids") List<Long> ids) {
        batchService.deleteBatchListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-批次主档")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:batch:query')")
    public CommonResult<BatchRespVO> getBatch(@RequestParam("id") Long id) {
        return success(batchService.getBatch(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-批次主档分页")
    @PreAuthorize("@ss.hasPermission('wms:batch:query')")
    public CommonResult<PageResult<BatchRespVO>> getBatchPage(@Valid BatchPageReqVO pageReqVO) {
        return success(batchService.getBatchPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-批次主档 Excel")
    @PreAuthorize("@ss.hasPermission('wms:batch:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportBatchExcel(@Valid BatchPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<BatchRespVO> list = batchService.getBatchPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-批次主档.xls", "数据", BatchRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-批次主档模板")
    @PreAuthorize("@ss.hasPermission('wms:batch:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-批次主档导入模板.xls", "数据",
                BatchImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-批次主档")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:batch:import')")
    public CommonResult<BatchImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<BatchImportExcelVO> list = ExcelUtils.read(file, BatchImportExcelVO.class);
        return success(batchService.importBatchList(list));
    }

}