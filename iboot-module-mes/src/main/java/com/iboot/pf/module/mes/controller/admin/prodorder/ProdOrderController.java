package com.iboot.pf.module.mes.controller.admin.prodorder;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderImportExcelVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderImportRespVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderPageReqVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderRespVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderSaveReqVO;
import com.iboot.pf.module.mes.service.prodorder.ProdOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES-生产工单")
@RestController
@RequestMapping("/mes/prod-order")
@Validated
public class ProdOrderController {

    @Resource
    private ProdOrderService prodOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建生产工单")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:create')")
    public CommonResult<Long> createProdOrder(@Valid @RequestBody ProdOrderSaveReqVO createReqVO) {
        return success(prodOrderService.createProdOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产工单")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:update')")
    public CommonResult<Boolean> updateProdOrder(@Valid @RequestBody ProdOrderSaveReqVO updateReqVO) {
        prodOrderService.updateProdOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产工单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:delete')")
    public CommonResult<Boolean> deleteProdOrder(@RequestParam("id") Long id) {
        prodOrderService.deleteProdOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除生产工单")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:prod-order:delete')")
    public CommonResult<Boolean> deleteProdOrderList(@RequestParam("ids") List<Long> ids) {
        prodOrderService.deleteProdOrderListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产工单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:query')")
    public CommonResult<ProdOrderRespVO> getProdOrder(@RequestParam("id") Long id) {
        return success(prodOrderService.getProdOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产工单分页")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:query')")
    public CommonResult<PageResult<ProdOrderRespVO>> getProdOrderPage(@Valid ProdOrderPageReqVO pageReqVO) {
        return success(prodOrderService.getProdOrderPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出生产工单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProdOrderExcel(@Valid ProdOrderPageReqVO pageReqVO,
                                     HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProdOrderRespVO> list = prodOrderService.getProdOrderPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-生产工单.xls", "数据", ProdOrderRespVO.class, list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得生产工单导入模板")
    @PreAuthorize("@ss.hasPermission('mes:prod-order:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-生产工单导入模板.xls", "数据",
                ProdOrderImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入生产工单")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:prod-order:import')")
    public CommonResult<ProdOrderImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ProdOrderImportExcelVO> list = ExcelUtils.read(file, ProdOrderImportExcelVO.class);
        return success(prodOrderService.importProdOrderList(list));
    }

}
