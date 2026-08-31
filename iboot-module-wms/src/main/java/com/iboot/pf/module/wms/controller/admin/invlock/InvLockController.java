package com.iboot.pf.module.wms.controller.admin.invlock;

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

import com.iboot.pf.module.wms.controller.admin.invlock.vo.*;
import com.iboot.pf.module.wms.service.invlock.InvLockService;

@Tag(name = "管理后台 - WMS-库存冻结记录")
@RestController
@RequestMapping("/wms/inv-lock")
@Validated
public class InvLockController {

    @Resource
    private InvLockService invLockService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-库存冻结记录")
    @PreAuthorize("@ss.hasPermission('wms:inv-lock:create')")
    public CommonResult<Long> createInvLock(@Valid @RequestBody InvLockSaveReqVO createReqVO) {
        return success(invLockService.createInvLock(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-库存冻结记录")
    @PreAuthorize("@ss.hasPermission('wms:inv-lock:update')")
    public CommonResult<Boolean> updateInvLock(@Valid @RequestBody InvLockSaveReqVO updateReqVO) {
        invLockService.updateInvLock(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-库存冻结记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inv-lock:delete')")
    public CommonResult<Boolean> deleteInvLock(@RequestParam("id") Long id) {
        invLockService.deleteInvLock(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-库存冻结记录")
                @PreAuthorize("@ss.hasPermission('wms:inv-lock:delete')")
    public CommonResult<Boolean> deleteInvLockList(@RequestParam("ids") List<Long> ids) {
        invLockService.deleteInvLockListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-库存冻结记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:inv-lock:query')")
    public CommonResult<InvLockRespVO> getInvLock(@RequestParam("id") Long id) {
        return success(invLockService.getInvLock(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-库存冻结记录分页")
    @PreAuthorize("@ss.hasPermission('wms:inv-lock:query')")
    public CommonResult<PageResult<InvLockRespVO>> getInvLockPage(@Valid InvLockPageReqVO pageReqVO) {
        return success(invLockService.getInvLockPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-库存冻结记录 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inv-lock:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInvLockExcel(@Valid InvLockPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<InvLockRespVO> list = invLockService.getInvLockPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-库存冻结记录.xls", "数据", InvLockRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-库存冻结记录模板")
    @PreAuthorize("@ss.hasPermission('wms:inv-lock:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-库存冻结记录导入模板.xls", "数据",
                InvLockImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-库存冻结记录")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inv-lock:import')")
    public CommonResult<InvLockImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<InvLockImportExcelVO> list = ExcelUtils.read(file, InvLockImportExcelVO.class);
        return success(invLockService.importInvLockList(list));
    }

}