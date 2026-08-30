package com.iboot.pf.module.mes.controller.admin.qcrule;

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

import com.iboot.pf.module.mes.controller.admin.qcrule.vo.*;
import com.iboot.pf.module.mes.service.qcrule.QcRuleService;

@Tag(name = "管理后台 - MES-质检触发规则")
@RestController
@RequestMapping("/mes/qc-rule")
@Validated
public class QcRuleController {

    @Resource
    private QcRuleService qcRuleService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-质检触发规则")
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:create')")
    public CommonResult<Long> createQcRule(@Valid @RequestBody QcRuleSaveReqVO createReqVO) {
        return success(qcRuleService.createQcRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-质检触发规则")
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:update')")
    public CommonResult<Boolean> updateQcRule(@Valid @RequestBody QcRuleSaveReqVO updateReqVO) {
        qcRuleService.updateQcRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-质检触发规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:delete')")
    public CommonResult<Boolean> deleteQcRule(@RequestParam("id") Long id) {
        qcRuleService.deleteQcRule(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-质检触发规则")
                @PreAuthorize("@ss.hasPermission('mes:qc-rule:delete')")
    public CommonResult<Boolean> deleteQcRuleList(@RequestParam("ids") List<Long> ids) {
        qcRuleService.deleteQcRuleListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-质检触发规则")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:query')")
    public CommonResult<QcRuleRespVO> getQcRule(@RequestParam("id") Long id) {
        return success(qcRuleService.getQcRule(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-质检触发规则分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:query')")
    public CommonResult<PageResult<QcRuleRespVO>> getQcRulePage(@Valid QcRulePageReqVO pageReqVO) {
        return success(qcRuleService.getQcRulePage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-质检触发规则 Excel")
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportQcRuleExcel(@Valid QcRulePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<QcRuleRespVO> list = qcRuleService.getQcRulePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-质检触发规则.xls", "数据", QcRuleRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-质检触发规则模板")
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-质检触发规则导入模板.xls", "数据",
                QcRuleImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-质检触发规则")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:import')")
    public CommonResult<QcRuleImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<QcRuleImportExcelVO> list = ExcelUtils.read(file, QcRuleImportExcelVO.class);
        return success(qcRuleService.importQcRuleList(list));
    }

}