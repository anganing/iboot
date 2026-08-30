package com.iboot.pf.module.wms.controller.admin.countorder;

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

import com.iboot.pf.module.wms.controller.admin.countorder.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.countorderline.CountOrderLineDO;
import com.iboot.pf.module.wms.service.countorder.CountOrderService;

@Tag(name = "管理后台 - WMS-盘点单")
@RestController
@RequestMapping("/wms/count-order")
@Validated
public class CountOrderController {

    @Resource
    private CountOrderService countOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-盘点单")
    @PreAuthorize("@ss.hasPermission('wms:count-order:create')")
    public CommonResult<Long> createCountOrder(@Valid @RequestBody CountOrderSaveReqVO createReqVO) {
        return success(countOrderService.createCountOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-盘点单")
    @PreAuthorize("@ss.hasPermission('wms:count-order:update')")
    public CommonResult<Boolean> updateCountOrder(@Valid @RequestBody CountOrderSaveReqVO updateReqVO) {
        countOrderService.updateCountOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-盘点单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:count-order:delete')")
    public CommonResult<Boolean> deleteCountOrder(@RequestParam("id") Long id) {
        countOrderService.deleteCountOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-盘点单")
                @PreAuthorize("@ss.hasPermission('wms:count-order:delete')")
    public CommonResult<Boolean> deleteCountOrderList(@RequestParam("ids") List<Long> ids) {
        countOrderService.deleteCountOrderListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-盘点单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:count-order:query')")
    public CommonResult<CountOrderRespVO> getCountOrder(@RequestParam("id") Long id) {
        return success(countOrderService.getCountOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-盘点单分页")
    @PreAuthorize("@ss.hasPermission('wms:count-order:query')")
    public CommonResult<PageResult<CountOrderRespVO>> getCountOrderPage(@Valid CountOrderPageReqVO pageReqVO) {
        return success(countOrderService.getCountOrderPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-盘点单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:count-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCountOrderExcel(@Valid CountOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CountOrderRespVO> list = countOrderService.getCountOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-盘点单.xls", "数据", CountOrderRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-盘点单模板")
    @PreAuthorize("@ss.hasPermission('wms:count-order:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-盘点单导入模板.xls", "数据",
                CountOrderImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-盘点单")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:count-order:import')")
    public CommonResult<CountOrderImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<CountOrderImportExcelVO> list = ExcelUtils.read(file, CountOrderImportExcelVO.class);
        return success(countOrderService.importCountOrderList(list));
    }

    // ==================== 子表（WMS-盘点明细） ====================

    @GetMapping("/count-order-line/page")
    @Operation(summary = "获得WMS-盘点明细分页")
    @Parameter(name = "orderId", description = "盘点单编号")
    @PreAuthorize("@ss.hasPermission('wms:count-order:query')")
    public CommonResult<PageResult<CountOrderLineDO>> getCountOrderLinePage(PageParam pageReqVO,
                                                                                        @RequestParam("orderId") Long orderId) {
        return success(countOrderService.getCountOrderLinePage(pageReqVO, orderId));
    }

    @PostMapping("/count-order-line/create")
    @Operation(summary = "创建WMS-盘点明细")
    @PreAuthorize("@ss.hasPermission('wms:count-order:create')")
    public CommonResult<Long> createCountOrderLine(@Valid @RequestBody CountOrderLineDO countOrderLine) {
        return success(countOrderService.createCountOrderLine(countOrderLine));
    }

    @PutMapping("/count-order-line/update")
    @Operation(summary = "更新WMS-盘点明细")
    @PreAuthorize("@ss.hasPermission('wms:count-order:update')")
    public CommonResult<Boolean> updateCountOrderLine(@Valid @RequestBody CountOrderLineDO countOrderLine) {
        countOrderService.updateCountOrderLine(countOrderLine);
        return success(true);
    }

    @DeleteMapping("/count-order-line/delete")
    @Parameter(name = "id", description = "编号", required = true)
    @Operation(summary = "删除WMS-盘点明细")
    @PreAuthorize("@ss.hasPermission('wms:count-order:delete')")
    public CommonResult<Boolean> deleteCountOrderLine(@RequestParam("id") Long id) {
        countOrderService.deleteCountOrderLine(id);
        return success(true);
    }

    @DeleteMapping("/count-order-line/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-盘点明细")
    @PreAuthorize("@ss.hasPermission('wms:count-order:delete')")
    public CommonResult<Boolean> deleteCountOrderLineList(@RequestParam("ids") List<Long> ids) {
        countOrderService.deleteCountOrderLineListByIds(ids);
        return success(true);
    }

	@GetMapping("/count-order-line/get")
	@Operation(summary = "获得WMS-盘点明细")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:count-order:query')")
	public CommonResult<CountOrderLineDO> getCountOrderLine(@RequestParam("id") Long id) {
	    return success(countOrderService.getCountOrderLine(id));
	}

}