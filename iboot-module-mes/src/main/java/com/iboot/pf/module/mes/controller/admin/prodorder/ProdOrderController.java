package com.iboot.pf.module.mes.controller.admin.prodorder;

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

import com.iboot.pf.module.mes.controller.admin.prodorder.vo.*;
import com.iboot.pf.module.mes.service.prodorder.ProdOrderService;

@Tag(name = "管理后台 - MES-生产工单")
@RestController
@RequestMapping("/mes/prod-order")
@Validated
public class ProdOrderController {

    @Resource
    private ProdOrderService prodOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-生产工单")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:create')")
    public CommonResult<Long> createProdOrder(@Valid @RequestBody ProdOrderSaveReqVO createReqVO) {
        return success(prodOrderService.createProdOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-生产工单")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:update')")
    public CommonResult<Boolean> updateProdOrder(@Valid @RequestBody ProdOrderSaveReqVO updateReqVO) {
        prodOrderService.updateProdOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-生产工单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:prod-order:delete')")
    public CommonResult<Boolean> deleteProdOrder(@RequestParam("id") Long id) {
        prodOrderService.deleteProdOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-生产工单")
                @PreAuthorize("@ss.hasPermission('mes:prod-order:delete')")
    public CommonResult<Boolean> deleteProdOrderList(@RequestParam("ids") List<Long> ids) {
        prodOrderService.deleteProdOrderListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-生产工单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:query')")
    public CommonResult<ProdOrderRespVO> getProdOrder(@RequestParam("id") Long id) {
        return success(prodOrderService.getProdOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-生产工单分页")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:query')")
    public CommonResult<PageResult<ProdOrderRespVO>> getProdOrderPage(@Valid ProdOrderPageReqVO pageReqVO) {
        return success(prodOrderService.getProdOrderPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-生产工单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProdOrderExcel(@Valid ProdOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProdOrderRespVO> list = prodOrderService.getProdOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-生产工单.xls", "数据", ProdOrderRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-生产工单模板")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-生产工单导入模板.xls", "数据",
                ProdOrderImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-生产工单")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:prod-order:import')")
    public CommonResult<ProdOrderImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ProdOrderImportExcelVO> list = ExcelUtils.read(file, ProdOrderImportExcelVO.class);
        return success(prodOrderService.importProdOrderList(list));
    }

}