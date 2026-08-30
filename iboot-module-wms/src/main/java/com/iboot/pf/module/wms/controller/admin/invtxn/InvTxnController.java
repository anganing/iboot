package com.iboot.pf.module.wms.controller.admin.invtxn;

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

import com.iboot.pf.module.wms.controller.admin.invtxn.vo.*;
import com.iboot.pf.module.wms.service.invtxn.InvTxnService;

@Tag(name = "管理后台 - WMS-库存流水")
@RestController
@RequestMapping("/wms/inv-txn")
@Validated
public class InvTxnController {

    @Resource
    private InvTxnService invTxnService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-库存流水")
    @PreAuthorize("@ss.hasPermission('wms:inv-txn:create')")
    public CommonResult<Long> createInvTxn(@Valid @RequestBody InvTxnSaveReqVO createReqVO) {
        return success(invTxnService.createInvTxn(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-库存流水")
    @PreAuthorize("@ss.hasPermission('wms:inv-txn:update')")
    public CommonResult<Boolean> updateInvTxn(@Valid @RequestBody InvTxnSaveReqVO updateReqVO) {
        invTxnService.updateInvTxn(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-库存流水")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inv-txn:delete')")
    public CommonResult<Boolean> deleteInvTxn(@RequestParam("id") Long id) {
        invTxnService.deleteInvTxn(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-库存流水")
                @PreAuthorize("@ss.hasPermission('wms:inv-txn:delete')")
    public CommonResult<Boolean> deleteInvTxnList(@RequestParam("ids") List<Long> ids) {
        invTxnService.deleteInvTxnListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-库存流水")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inv-txn:query')")
    public CommonResult<InvTxnRespVO> getInvTxn(@RequestParam("id") Long id) {
        return success(invTxnService.getInvTxn(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-库存流水分页")
    @PreAuthorize("@ss.hasPermission('wms:inv-txn:query')")
    public CommonResult<PageResult<InvTxnRespVO>> getInvTxnPage(@Valid InvTxnPageReqVO pageReqVO) {
        return success(invTxnService.getInvTxnPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-库存流水 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inv-txn:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInvTxnExcel(@Valid InvTxnPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<InvTxnRespVO> list = invTxnService.getInvTxnPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-库存流水.xls", "数据", InvTxnRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-库存流水模板")
    @PreAuthorize("@ss.hasPermission('wms:inv-txn:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-库存流水导入模板.xls", "数据",
                InvTxnImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-库存流水")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inv-txn:import')")
    public CommonResult<InvTxnImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<InvTxnImportExcelVO> list = ExcelUtils.read(file, InvTxnImportExcelVO.class);
        return success(invTxnService.importInvTxnList(list));
    }

}