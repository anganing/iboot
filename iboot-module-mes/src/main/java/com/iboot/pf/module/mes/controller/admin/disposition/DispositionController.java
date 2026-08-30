package com.iboot.pf.module.mes.controller.admin.disposition;

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

import com.iboot.pf.module.mes.controller.admin.disposition.vo.*;
import com.iboot.pf.module.mes.service.disposition.DispositionService;

@Tag(name = "管理后台 - MES-不合格品处置单")
@RestController
@RequestMapping("/mes/disposition")
@Validated
public class DispositionController {

    @Resource
    private DispositionService dispositionService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-不合格品处置单")
    @PreAuthorize("@ss.hasPermission('mes:disposition:create')")
    public CommonResult<Long> createDisposition(@Valid @RequestBody DispositionSaveReqVO createReqVO) {
        return success(dispositionService.createDisposition(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-不合格品处置单")
    @PreAuthorize("@ss.hasPermission('mes:disposition:update')")
    public CommonResult<Boolean> updateDisposition(@Valid @RequestBody DispositionSaveReqVO updateReqVO) {
        dispositionService.updateDisposition(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-不合格品处置单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:disposition:delete')")
    public CommonResult<Boolean> deleteDisposition(@RequestParam("id") Long id) {
        dispositionService.deleteDisposition(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-不合格品处置单")
                @PreAuthorize("@ss.hasPermission('mes:disposition:delete')")
    public CommonResult<Boolean> deleteDispositionList(@RequestParam("ids") List<Long> ids) {
        dispositionService.deleteDispositionListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-不合格品处置单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:disposition:query')")
    public CommonResult<DispositionRespVO> getDisposition(@RequestParam("id") Long id) {
        return success(dispositionService.getDisposition(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-不合格品处置单分页")
    @PreAuthorize("@ss.hasPermission('mes:disposition:query')")
    public CommonResult<PageResult<DispositionRespVO>> getDispositionPage(@Valid DispositionPageReqVO pageReqVO) {
        return success(dispositionService.getDispositionPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-不合格品处置单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:disposition:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDispositionExcel(@Valid DispositionPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DispositionRespVO> list = dispositionService.getDispositionPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-不合格品处置单.xls", "数据", DispositionRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-不合格品处置单模板")
    @PreAuthorize("@ss.hasPermission('mes:disposition:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-不合格品处置单导入模板.xls", "数据",
                DispositionImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-不合格品处置单")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:disposition:import')")
    public CommonResult<DispositionImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<DispositionImportExcelVO> list = ExcelUtils.read(file, DispositionImportExcelVO.class);
        return success(dispositionService.importDispositionList(list));
    }

}