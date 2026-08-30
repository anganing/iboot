package com.iboot.pf.module.wms.controller.admin.transferorder;

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

import com.iboot.pf.module.wms.controller.admin.transferorder.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.transferorderline.TransferOrderLineDO;
import com.iboot.pf.module.wms.service.transferorder.TransferOrderService;

@Tag(name = "管理后台 - WMS-库存转移调拨单")
@RestController
@RequestMapping("/wms/transfer-order")
@Validated
public class TransferOrderController {

    @Resource
    private TransferOrderService transferOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-库存转移调拨单")
    @PreAuthorize("@ss.hasPermission('wms:transfer-order:create')")
    public CommonResult<Long> createTransferOrder(@Valid @RequestBody TransferOrderSaveReqVO createReqVO) {
        return success(transferOrderService.createTransferOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-库存转移调拨单")
    @PreAuthorize("@ss.hasPermission('wms:transfer-order:update')")
    public CommonResult<Boolean> updateTransferOrder(@Valid @RequestBody TransferOrderSaveReqVO updateReqVO) {
        transferOrderService.updateTransferOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-库存转移调拨单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:transfer-order:delete')")
    public CommonResult<Boolean> deleteTransferOrder(@RequestParam("id") Long id) {
        transferOrderService.deleteTransferOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-库存转移调拨单")
                @PreAuthorize("@ss.hasPermission('wms:transfer-order:delete')")
    public CommonResult<Boolean> deleteTransferOrderList(@RequestParam("ids") List<Long> ids) {
        transferOrderService.deleteTransferOrderListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-库存转移调拨单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:transfer-order:query')")
    public CommonResult<TransferOrderRespVO> getTransferOrder(@RequestParam("id") Long id) {
        return success(transferOrderService.getTransferOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-库存转移调拨单分页")
    @PreAuthorize("@ss.hasPermission('wms:transfer-order:query')")
    public CommonResult<PageResult<TransferOrderRespVO>> getTransferOrderPage(@Valid TransferOrderPageReqVO pageReqVO) {
        return success(transferOrderService.getTransferOrderPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-库存转移调拨单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:transfer-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTransferOrderExcel(@Valid TransferOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TransferOrderRespVO> list = transferOrderService.getTransferOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-库存转移调拨单.xls", "数据", TransferOrderRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-库存转移调拨单模板")
    @PreAuthorize("@ss.hasPermission('wms:transfer-order:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-库存转移调拨单导入模板.xls", "数据",
                TransferOrderImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-库存转移调拨单")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:transfer-order:import')")
    public CommonResult<TransferOrderImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<TransferOrderImportExcelVO> list = ExcelUtils.read(file, TransferOrderImportExcelVO.class);
        return success(transferOrderService.importTransferOrderList(list));
    }

    // ==================== 子表（WMS-库存转移调拨明细） ====================

    @GetMapping("/transfer-order-line/page")
    @Operation(summary = "获得WMS-库存转移调拨明细分页")
    @Parameter(name = "orderId", description = "转移调拨单编号")
    @PreAuthorize("@ss.hasPermission('wms:transfer-order:query')")
    public CommonResult<PageResult<TransferOrderLineDO>> getTransferOrderLinePage(PageParam pageReqVO,
                                                                                        @RequestParam("orderId") Long orderId) {
        return success(transferOrderService.getTransferOrderLinePage(pageReqVO, orderId));
    }

    @PostMapping("/transfer-order-line/create")
    @Operation(summary = "创建WMS-库存转移调拨明细")
    @PreAuthorize("@ss.hasPermission('wms:transfer-order:create')")
    public CommonResult<Long> createTransferOrderLine(@Valid @RequestBody TransferOrderLineDO transferOrderLine) {
        return success(transferOrderService.createTransferOrderLine(transferOrderLine));
    }

    @PutMapping("/transfer-order-line/update")
    @Operation(summary = "更新WMS-库存转移调拨明细")
    @PreAuthorize("@ss.hasPermission('wms:transfer-order:update')")
    public CommonResult<Boolean> updateTransferOrderLine(@Valid @RequestBody TransferOrderLineDO transferOrderLine) {
        transferOrderService.updateTransferOrderLine(transferOrderLine);
        return success(true);
    }

    @DeleteMapping("/transfer-order-line/delete")
    @Parameter(name = "id", description = "编号", required = true)
    @Operation(summary = "删除WMS-库存转移调拨明细")
    @PreAuthorize("@ss.hasPermission('wms:transfer-order:delete')")
    public CommonResult<Boolean> deleteTransferOrderLine(@RequestParam("id") Long id) {
        transferOrderService.deleteTransferOrderLine(id);
        return success(true);
    }

    @DeleteMapping("/transfer-order-line/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-库存转移调拨明细")
    @PreAuthorize("@ss.hasPermission('wms:transfer-order:delete')")
    public CommonResult<Boolean> deleteTransferOrderLineList(@RequestParam("ids") List<Long> ids) {
        transferOrderService.deleteTransferOrderLineListByIds(ids);
        return success(true);
    }

	@GetMapping("/transfer-order-line/get")
	@Operation(summary = "获得WMS-库存转移调拨明细")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:transfer-order:query')")
	public CommonResult<TransferOrderLineDO> getTransferOrderLine(@RequestParam("id") Long id) {
	    return success(transferOrderService.getTransferOrderLine(id));
	}

}