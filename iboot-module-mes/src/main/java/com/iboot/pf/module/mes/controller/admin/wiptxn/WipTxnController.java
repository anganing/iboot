package com.iboot.pf.module.mes.controller.admin.wiptxn;

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

import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.*;
import com.iboot.pf.module.mes.service.wiptxn.WipTxnService;

@Tag(name = "管理后台 - MES-在制流转流水")
@RestController
@RequestMapping("/mes/wip-txn")
@Validated
public class WipTxnController {

    @Resource
    private WipTxnService wipTxnService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-在制流转流水")
    @PreAuthorize("@ss.hasPermission('mes:wip-txn:create')")
    public CommonResult<Long> createWipTxn(@Valid @RequestBody WipTxnSaveReqVO createReqVO) {
        return success(wipTxnService.createWipTxn(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-在制流转流水")
    @PreAuthorize("@ss.hasPermission('mes:wip-txn:update')")
    public CommonResult<Boolean> updateWipTxn(@Valid @RequestBody WipTxnSaveReqVO updateReqVO) {
        wipTxnService.updateWipTxn(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-在制流转流水")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wip-txn:delete')")
    public CommonResult<Boolean> deleteWipTxn(@RequestParam("id") Long id) {
        wipTxnService.deleteWipTxn(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-在制流转流水")
                @PreAuthorize("@ss.hasPermission('mes:wip-txn:delete')")
    public CommonResult<Boolean> deleteWipTxnList(@RequestParam("ids") List<Long> ids) {
        wipTxnService.deleteWipTxnListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-在制流转流水")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wip-txn:query')")
    public CommonResult<WipTxnRespVO> getWipTxn(@RequestParam("id") Long id) {
        return success(wipTxnService.getWipTxn(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-在制流转流水分页")
    @PreAuthorize("@ss.hasPermission('mes:wip-txn:query')")
    public CommonResult<PageResult<WipTxnRespVO>> getWipTxnPage(@Valid WipTxnPageReqVO pageReqVO) {
        return success(wipTxnService.getWipTxnPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-在制流转流水 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wip-txn:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWipTxnExcel(@Valid WipTxnPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WipTxnRespVO> list = wipTxnService.getWipTxnPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-在制流转流水.xls", "数据", WipTxnRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-在制流转流水模板")
    @PreAuthorize("@ss.hasPermission('mes:wip-txn:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-在制流转流水导入模板.xls", "数据",
                WipTxnImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-在制流转流水")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wip-txn:import')")
    public CommonResult<WipTxnImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<WipTxnImportExcelVO> list = ExcelUtils.read(file, WipTxnImportExcelVO.class);
        return success(wipTxnService.importWipTxnList(list));
    }

}