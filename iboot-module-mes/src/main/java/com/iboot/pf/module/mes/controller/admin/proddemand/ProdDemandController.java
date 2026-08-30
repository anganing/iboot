package com.iboot.pf.module.mes.controller.admin.proddemand;

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

import com.iboot.pf.module.mes.controller.admin.proddemand.vo.*;
import com.iboot.pf.module.mes.service.proddemand.ProdDemandService;

@Tag(name = "管理后台 - MES-生产需求")
@RestController
@RequestMapping("/mes/prod-demand")
@Validated
public class ProdDemandController {

    @Resource
    private ProdDemandService prodDemandService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-生产需求")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:create')")
    public CommonResult<Long> createProdDemand(@Valid @RequestBody ProdDemandSaveReqVO createReqVO) {
        return success(prodDemandService.createProdDemand(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-生产需求")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:update')")
    public CommonResult<Boolean> updateProdDemand(@Valid @RequestBody ProdDemandSaveReqVO updateReqVO) {
        prodDemandService.updateProdDemand(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-生产需求")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:delete')")
    public CommonResult<Boolean> deleteProdDemand(@RequestParam("id") Long id) {
        prodDemandService.deleteProdDemand(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-生产需求")
                @PreAuthorize("@ss.hasPermission('mes:prod-demand:delete')")
    public CommonResult<Boolean> deleteProdDemandList(@RequestParam("ids") List<Long> ids) {
        prodDemandService.deleteProdDemandListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-生产需求")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:query')")
    public CommonResult<ProdDemandRespVO> getProdDemand(@RequestParam("id") Long id) {
        return success(prodDemandService.getProdDemand(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-生产需求分页")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:query')")
    public CommonResult<PageResult<ProdDemandRespVO>> getProdDemandPage(@Valid ProdDemandPageReqVO pageReqVO) {
        return success(prodDemandService.getProdDemandPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-生产需求 Excel")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProdDemandExcel(@Valid ProdDemandPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProdDemandRespVO> list = prodDemandService.getProdDemandPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-生产需求.xls", "数据", ProdDemandRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-生产需求模板")
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-生产需求导入模板.xls", "数据",
                ProdDemandImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-生产需求")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:prod-demand:import')")
    public CommonResult<ProdDemandImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ProdDemandImportExcelVO> list = ExcelUtils.read(file, ProdDemandImportExcelVO.class);
        return success(prodDemandService.importProdDemandList(list));
    }

}