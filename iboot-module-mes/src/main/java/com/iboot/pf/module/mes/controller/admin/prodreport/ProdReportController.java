package com.iboot.pf.module.mes.controller.admin.prodreport;

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

import com.iboot.pf.module.mes.controller.admin.prodreport.vo.*;
import com.iboot.pf.module.mes.service.prodreport.ProdReportService;

@Tag(name = "管理后台 - MES-生产报工记录")
@RestController
@RequestMapping("/mes/prod-report")
@Validated
public class ProdReportController {

    @Resource
    private ProdReportService prodReportService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-生产报工记录")
    @PreAuthorize("@ss.hasPermission('mes:prod-report:create')")
    public CommonResult<Long> createProdReport(@Valid @RequestBody ProdReportSaveReqVO createReqVO) {
        return success(prodReportService.createProdReport(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-生产报工记录")
    @PreAuthorize("@ss.hasPermission('mes:prod-report:update')")
    public CommonResult<Boolean> updateProdReport(@Valid @RequestBody ProdReportSaveReqVO updateReqVO) {
        prodReportService.updateProdReport(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-生产报工记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:prod-report:delete')")
    public CommonResult<Boolean> deleteProdReport(@RequestParam("id") Long id) {
        prodReportService.deleteProdReport(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-生产报工记录")
                @PreAuthorize("@ss.hasPermission('mes:prod-report:delete')")
    public CommonResult<Boolean> deleteProdReportList(@RequestParam("ids") List<Long> ids) {
        prodReportService.deleteProdReportListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-生产报工记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:prod-report:query')")
    public CommonResult<ProdReportRespVO> getProdReport(@RequestParam("id") Long id) {
        return success(prodReportService.getProdReport(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-生产报工记录分页")
    @PreAuthorize("@ss.hasPermission('mes:prod-report:query')")
    public CommonResult<PageResult<ProdReportRespVO>> getProdReportPage(@Valid ProdReportPageReqVO pageReqVO) {
        return success(prodReportService.getProdReportPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-生产报工记录 Excel")
    @PreAuthorize("@ss.hasPermission('mes:prod-report:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProdReportExcel(@Valid ProdReportPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProdReportRespVO> list = prodReportService.getProdReportPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-生产报工记录.xls", "数据", ProdReportRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-生产报工记录模板")
    @PreAuthorize("@ss.hasPermission('mes:prod-report:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-生产报工记录导入模板.xls", "数据",
                ProdReportImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-生产报工记录")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:prod-report:import')")
    public CommonResult<ProdReportImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ProdReportImportExcelVO> list = ExcelUtils.read(file, ProdReportImportExcelVO.class);
        return success(prodReportService.importProdReportList(list));
    }

}