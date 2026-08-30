package com.iboot.pf.module.mes.controller.admin.qctask;

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

import com.iboot.pf.module.mes.controller.admin.qctask.vo.*;
import com.iboot.pf.module.mes.service.qctask.QcTaskService;

@Tag(name = "管理后台 - MES-质检任务")
@RestController
@RequestMapping("/mes/qc-task")
@Validated
public class QcTaskController {

    @Resource
    private QcTaskService qcTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-质检任务")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:create')")
    public CommonResult<Long> createQcTask(@Valid @RequestBody QcTaskSaveReqVO createReqVO) {
        return success(qcTaskService.createQcTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-质检任务")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:update')")
    public CommonResult<Boolean> updateQcTask(@Valid @RequestBody QcTaskSaveReqVO updateReqVO) {
        qcTaskService.updateQcTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-质检任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-task:delete')")
    public CommonResult<Boolean> deleteQcTask(@RequestParam("id") Long id) {
        qcTaskService.deleteQcTask(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-质检任务")
                @PreAuthorize("@ss.hasPermission('mes:qc-task:delete')")
    public CommonResult<Boolean> deleteQcTaskList(@RequestParam("ids") List<Long> ids) {
        qcTaskService.deleteQcTaskListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-质检任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:query')")
    public CommonResult<QcTaskRespVO> getQcTask(@RequestParam("id") Long id) {
        return success(qcTaskService.getQcTask(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-质检任务分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:query')")
    public CommonResult<PageResult<QcTaskRespVO>> getQcTaskPage(@Valid QcTaskPageReqVO pageReqVO) {
        return success(qcTaskService.getQcTaskPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-质检任务 Excel")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportQcTaskExcel(@Valid QcTaskPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<QcTaskRespVO> list = qcTaskService.getQcTaskPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-质检任务.xls", "数据", QcTaskRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-质检任务模板")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-质检任务导入模板.xls", "数据",
                QcTaskImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-质检任务")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-task:import')")
    public CommonResult<QcTaskImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<QcTaskImportExcelVO> list = ExcelUtils.read(file, QcTaskImportExcelVO.class);
        return success(qcTaskService.importQcTaskList(list));
    }

}