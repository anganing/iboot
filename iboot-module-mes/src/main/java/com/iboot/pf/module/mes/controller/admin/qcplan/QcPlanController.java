package com.iboot.pf.module.mes.controller.admin.qcplan;

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

import com.iboot.pf.module.mes.controller.admin.qcplan.vo.*;
import com.iboot.pf.module.mes.service.qcplan.QcPlanService;

@Tag(name = "管理后台 - MES-检验方案")
@RestController
@RequestMapping("/mes/qc-plan")
@Validated
public class QcPlanController {

    @Resource
    private QcPlanService qcPlanService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-检验方案")
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:create')")
    public CommonResult<Long> createQcPlan(@Valid @RequestBody QcPlanSaveReqVO createReqVO) {
        return success(qcPlanService.createQcPlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-检验方案")
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:update')")
    public CommonResult<Boolean> updateQcPlan(@Valid @RequestBody QcPlanSaveReqVO updateReqVO) {
        qcPlanService.updateQcPlan(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-检验方案")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:delete')")
    public CommonResult<Boolean> deleteQcPlan(@RequestParam("id") Long id) {
        qcPlanService.deleteQcPlan(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-检验方案")
                @PreAuthorize("@ss.hasPermission('mes:qc-plan:delete')")
    public CommonResult<Boolean> deleteQcPlanList(@RequestParam("ids") List<Long> ids) {
        qcPlanService.deleteQcPlanListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-检验方案")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:query')")
    public CommonResult<QcPlanRespVO> getQcPlan(@RequestParam("id") Long id) {
        return success(qcPlanService.getQcPlan(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-检验方案分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:query')")
    public CommonResult<PageResult<QcPlanRespVO>> getQcPlanPage(@Valid QcPlanPageReqVO pageReqVO) {
        return success(qcPlanService.getQcPlanPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-检验方案 Excel")
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportQcPlanExcel(@Valid QcPlanPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<QcPlanRespVO> list = qcPlanService.getQcPlanPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-检验方案.xls", "数据", QcPlanRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-检验方案模板")
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-检验方案导入模板.xls", "数据",
                QcPlanImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-检验方案")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:import')")
    public CommonResult<QcPlanImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<QcPlanImportExcelVO> list = ExcelUtils.read(file, QcPlanImportExcelVO.class);
        return success(qcPlanService.importQcPlanList(list));
    }

}