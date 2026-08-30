package com.iboot.pf.module.mes.controller.admin.dispatchtask;

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

import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.*;
import com.iboot.pf.module.mes.service.dispatchtask.DispatchTaskService;

@Tag(name = "管理后台 - MES-派工任务")
@RestController
@RequestMapping("/mes/dispatch-task")
@Validated
public class DispatchTaskController {

    @Resource
    private DispatchTaskService dispatchTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-派工任务")
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:create')")
    public CommonResult<Long> createDispatchTask(@Valid @RequestBody DispatchTaskSaveReqVO createReqVO) {
        return success(dispatchTaskService.createDispatchTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-派工任务")
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:update')")
    public CommonResult<Boolean> updateDispatchTask(@Valid @RequestBody DispatchTaskSaveReqVO updateReqVO) {
        dispatchTaskService.updateDispatchTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-派工任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:delete')")
    public CommonResult<Boolean> deleteDispatchTask(@RequestParam("id") Long id) {
        dispatchTaskService.deleteDispatchTask(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-派工任务")
                @PreAuthorize("@ss.hasPermission('mes:dispatch-task:delete')")
    public CommonResult<Boolean> deleteDispatchTaskList(@RequestParam("ids") List<Long> ids) {
        dispatchTaskService.deleteDispatchTaskListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-派工任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:query')")
    public CommonResult<DispatchTaskRespVO> getDispatchTask(@RequestParam("id") Long id) {
        return success(dispatchTaskService.getDispatchTask(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-派工任务分页")
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:query')")
    public CommonResult<PageResult<DispatchTaskRespVO>> getDispatchTaskPage(@Valid DispatchTaskPageReqVO pageReqVO) {
        return success(dispatchTaskService.getDispatchTaskPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-派工任务 Excel")
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDispatchTaskExcel(@Valid DispatchTaskPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DispatchTaskRespVO> list = dispatchTaskService.getDispatchTaskPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-派工任务.xls", "数据", DispatchTaskRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-派工任务模板")
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-派工任务导入模板.xls", "数据",
                DispatchTaskImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-派工任务")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:import')")
    public CommonResult<DispatchTaskImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<DispatchTaskImportExcelVO> list = ExcelUtils.read(file, DispatchTaskImportExcelVO.class);
        return success(dispatchTaskService.importDispatchTaskList(list));
    }

}