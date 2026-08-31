package com.iboot.pf.module.wms.controller.admin.outordermgr;

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

import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.*;
import com.iboot.pf.module.wms.service.outordermgr.OutOrderService;

@Tag(name = "管理后台 - WMS-出库单")
@RestController
@RequestMapping("/wms/out-order")
@Validated
public class OutOrderController {

    @Resource
    private OutOrderService outOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-出库单")
    @PreAuthorize("@ss.hasPermission('wms:out-order:create')")
    public CommonResult<Long> createOutOrder(@Valid @RequestBody OutOrderSaveReqVO createReqVO) {
        return success(outOrderService.createOutOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-出库单")
    @PreAuthorize("@ss.hasPermission('wms:out-order:update')")
    public CommonResult<Boolean> updateOutOrder(@Valid @RequestBody OutOrderSaveReqVO updateReqVO) {
        outOrderService.updateOutOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:out-order:delete')")
    public CommonResult<Boolean> deleteOutOrder(@RequestParam("id") Long id) {
        outOrderService.deleteOutOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-出库单")
                @PreAuthorize("@ss.hasPermission('wms:out-order:delete')")
    public CommonResult<Boolean> deleteOutOrderList(@RequestParam("ids") List<Long> ids) {
        outOrderService.deleteOutOrderListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-出库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:out-order:query')")
    public CommonResult<OutOrderRespVO> getOutOrder(@RequestParam("id") Long id) {
        return success(outOrderService.getOutOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-出库单分页")
    @PreAuthorize("@ss.hasPermission('wms:out-order:query')")
    public CommonResult<PageResult<OutOrderRespVO>> getOutOrderPage(@Valid OutOrderPageReqVO pageReqVO) {
        return success(outOrderService.getOutOrderPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-出库单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:out-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOutOrderExcel(@Valid OutOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OutOrderRespVO> list = outOrderService.getOutOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-出库单.xls", "数据", OutOrderRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-出库单模板")
    @PreAuthorize("@ss.hasPermission('wms:out-order:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-出库单导入模板.xls", "数据",
                OutOrderImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-出库单")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:out-order:import')")
    public CommonResult<OutOrderImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<OutOrderImportExcelVO> list = ExcelUtils.read(file, OutOrderImportExcelVO.class);
        return success(outOrderService.importOutOrderList(list));
    }

    // ==================== 子表（WMS-出库单明细） ====================

    @GetMapping("/out-order-line/page")
    @Operation(summary = "获得WMS-出库单明细分页")
    @PreAuthorize("@ss.hasPermission('wms:out-order:query')")
    public CommonResult<PageResult<OutOrderLineRespVO>> getOutOrderLinePage(@Valid OutOrderLinePageReqVO pageReqVO) {
        return success(outOrderService.getOutOrderLinePage(pageReqVO));
    }

    @PostMapping("/out-order-line/create")
    @Operation(summary = "创建WMS-出库单明细")
    @PreAuthorize("@ss.hasPermission('wms:out-order:create')")
    public CommonResult<Long> createOutOrderLine(@Valid @RequestBody OutOrderLineSaveReqVO createReqVO) {
        return success(outOrderService.createOutOrderLine(createReqVO));
    }

    @PutMapping("/out-order-line/update")
    @Operation(summary = "更新WMS-出库单明细")
    @PreAuthorize("@ss.hasPermission('wms:out-order:update')")
    public CommonResult<Boolean> updateOutOrderLine(@Valid @RequestBody OutOrderLineSaveReqVO updateReqVO) {
        outOrderService.updateOutOrderLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/out-order-line/delete")
    @Parameter(name = "id", description = "编号", required = true)
    @Operation(summary = "删除WMS-出库单明细")
    @PreAuthorize("@ss.hasPermission('wms:out-order:delete')")
    public CommonResult<Boolean> deleteOutOrderLine(@RequestParam("id") Long id) {
        outOrderService.deleteOutOrderLine(id);
        return success(true);
    }

    @DeleteMapping("/out-order-line/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-出库单明细")
    @PreAuthorize("@ss.hasPermission('wms:out-order:delete')")
    public CommonResult<Boolean> deleteOutOrderLineList(@RequestParam("ids") List<Long> ids) {
        outOrderService.deleteOutOrderLineListByIds(ids);
        return success(true);
    }

	@GetMapping("/out-order-line/get")
	@Operation(summary = "获得WMS-出库单明细")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:out-order:query')")
	public CommonResult<OutOrderLineRespVO> getOutOrderLine(@RequestParam("id") Long id) {
	    return success(outOrderService.getOutOrderLine(id));
	}

}