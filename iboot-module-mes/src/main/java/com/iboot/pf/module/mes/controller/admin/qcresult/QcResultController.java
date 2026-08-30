package com.iboot.pf.module.mes.controller.admin.qcresult;

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

import com.iboot.pf.module.mes.controller.admin.qcresult.vo.*;
import com.iboot.pf.module.mes.service.qcresult.QcResultService;

@Tag(name = "管理后台 - MES-检验结果")
@RestController
@RequestMapping("/mes/qc-result")
@Validated
public class QcResultController {

    @Resource
    private QcResultService qcResultService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-检验结果")
    @PreAuthorize("@ss.hasPermission('mes:qc-result:create')")
    public CommonResult<Long> createQcResult(@Valid @RequestBody QcResultSaveReqVO createReqVO) {
        return success(qcResultService.createQcResult(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-检验结果")
    @PreAuthorize("@ss.hasPermission('mes:qc-result:update')")
    public CommonResult<Boolean> updateQcResult(@Valid @RequestBody QcResultSaveReqVO updateReqVO) {
        qcResultService.updateQcResult(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-检验结果")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-result:delete')")
    public CommonResult<Boolean> deleteQcResult(@RequestParam("id") Long id) {
        qcResultService.deleteQcResult(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-检验结果")
                @PreAuthorize("@ss.hasPermission('mes:qc-result:delete')")
    public CommonResult<Boolean> deleteQcResultList(@RequestParam("ids") List<Long> ids) {
        qcResultService.deleteQcResultListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-检验结果")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:qc-result:query')")
    public CommonResult<QcResultRespVO> getQcResult(@RequestParam("id") Long id) {
        return success(qcResultService.getQcResult(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-检验结果分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-result:query')")
    public CommonResult<PageResult<QcResultRespVO>> getQcResultPage(@Valid QcResultPageReqVO pageReqVO) {
        return success(qcResultService.getQcResultPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-检验结果 Excel")
    @PreAuthorize("@ss.hasPermission('mes:qc-result:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportQcResultExcel(@Valid QcResultPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<QcResultRespVO> list = qcResultService.getQcResultPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-检验结果.xls", "数据", QcResultRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-检验结果模板")
    @PreAuthorize("@ss.hasPermission('mes:qc-result:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-检验结果导入模板.xls", "数据",
                QcResultImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-检验结果")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-result:import')")
    public CommonResult<QcResultImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<QcResultImportExcelVO> list = ExcelUtils.read(file, QcResultImportExcelVO.class);
        return success(qcResultService.importQcResultList(list));
    }

}