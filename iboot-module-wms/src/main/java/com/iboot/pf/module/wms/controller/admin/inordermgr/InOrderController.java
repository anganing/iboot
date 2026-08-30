package com.iboot.pf.module.wms.controller.admin.inordermgr;

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

import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.inorderline.InOrderLineDO;
import com.iboot.pf.module.wms.service.inordermgr.InOrderService;

@Tag(name = "管理后台 - WMS-入库单")
@RestController
@RequestMapping("/wms/in-order")
@Validated
public class InOrderController {

    @Resource
    private InOrderService inOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-入库单")
    @PreAuthorize("@ss.hasPermission('wms:in-order:create')")
    public CommonResult<Long> createInOrder(@Valid @RequestBody InOrderSaveReqVO createReqVO) {
        return success(inOrderService.createInOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-入库单")
    @PreAuthorize("@ss.hasPermission('wms:in-order:update')")
    public CommonResult<Boolean> updateInOrder(@Valid @RequestBody InOrderSaveReqVO updateReqVO) {
        inOrderService.updateInOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-入库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:in-order:delete')")
    public CommonResult<Boolean> deleteInOrder(@RequestParam("id") Long id) {
        inOrderService.deleteInOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-入库单")
                @PreAuthorize("@ss.hasPermission('wms:in-order:delete')")
    public CommonResult<Boolean> deleteInOrderList(@RequestParam("ids") List<Long> ids) {
        inOrderService.deleteInOrderListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-入库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:in-order:query')")
    public CommonResult<InOrderRespVO> getInOrder(@RequestParam("id") Long id) {
        return success(inOrderService.getInOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-入库单分页")
    @PreAuthorize("@ss.hasPermission('wms:in-order:query')")
    public CommonResult<PageResult<InOrderRespVO>> getInOrderPage(@Valid InOrderPageReqVO pageReqVO) {
        return success(inOrderService.getInOrderPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-入库单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:in-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInOrderExcel(@Valid InOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<InOrderRespVO> list = inOrderService.getInOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-入库单.xls", "数据", InOrderRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-入库单模板")
    @PreAuthorize("@ss.hasPermission('wms:in-order:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-入库单导入模板.xls", "数据",
                InOrderImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-入库单")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:in-order:import')")
    public CommonResult<InOrderImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<InOrderImportExcelVO> list = ExcelUtils.read(file, InOrderImportExcelVO.class);
        return success(inOrderService.importInOrderList(list));
    }

    // ==================== 子表（WMS-入库单明细） ====================

    @GetMapping("/in-order-line/page")
    @Operation(summary = "获得WMS-入库单明细分页")
    @Parameter(name = "orderId", description = "入库单编号")
    @PreAuthorize("@ss.hasPermission('wms:in-order:query')")
    public CommonResult<PageResult<InOrderLineDO>> getInOrderLinePage(PageParam pageReqVO,
                                                                                        @RequestParam("orderId") Long orderId) {
        return success(inOrderService.getInOrderLinePage(pageReqVO, orderId));
    }

    @PostMapping("/in-order-line/create")
    @Operation(summary = "创建WMS-入库单明细")
    @PreAuthorize("@ss.hasPermission('wms:in-order:create')")
    public CommonResult<Long> createInOrderLine(@Valid @RequestBody InOrderLineDO inOrderLine) {
        return success(inOrderService.createInOrderLine(inOrderLine));
    }

    @PutMapping("/in-order-line/update")
    @Operation(summary = "更新WMS-入库单明细")
    @PreAuthorize("@ss.hasPermission('wms:in-order:update')")
    public CommonResult<Boolean> updateInOrderLine(@Valid @RequestBody InOrderLineDO inOrderLine) {
        inOrderService.updateInOrderLine(inOrderLine);
        return success(true);
    }

    @DeleteMapping("/in-order-line/delete")
    @Parameter(name = "id", description = "编号", required = true)
    @Operation(summary = "删除WMS-入库单明细")
    @PreAuthorize("@ss.hasPermission('wms:in-order:delete')")
    public CommonResult<Boolean> deleteInOrderLine(@RequestParam("id") Long id) {
        inOrderService.deleteInOrderLine(id);
        return success(true);
    }

    @DeleteMapping("/in-order-line/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-入库单明细")
    @PreAuthorize("@ss.hasPermission('wms:in-order:delete')")
    public CommonResult<Boolean> deleteInOrderLineList(@RequestParam("ids") List<Long> ids) {
        inOrderService.deleteInOrderLineListByIds(ids);
        return success(true);
    }

	@GetMapping("/in-order-line/get")
	@Operation(summary = "获得WMS-入库单明细")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:in-order:query')")
	public CommonResult<InOrderLineDO> getInOrderLine(@RequestParam("id") Long id) {
	    return success(inOrderService.getInOrderLine(id));
	}

}