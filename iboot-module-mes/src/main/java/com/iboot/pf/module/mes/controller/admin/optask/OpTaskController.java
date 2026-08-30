package com.iboot.pf.module.mes.controller.admin.optask;

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

import com.iboot.pf.module.mes.controller.admin.optask.vo.*;
import com.iboot.pf.module.mes.service.optask.OpTaskService;

@Tag(name = "管理后台 - MES-工序任务")
@RestController
@RequestMapping("/mes/op-task")
@Validated
public class OpTaskController {

    @Resource
    private OpTaskService opTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-工序任务")
    @PreAuthorize("@ss.hasPermission('mes:op-task:create')")
    public CommonResult<Long> createOpTask(@Valid @RequestBody OpTaskSaveReqVO createReqVO) {
        return success(opTaskService.createOpTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-工序任务")
    @PreAuthorize("@ss.hasPermission('mes:op-task:update')")
    public CommonResult<Boolean> updateOpTask(@Valid @RequestBody OpTaskSaveReqVO updateReqVO) {
        opTaskService.updateOpTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-工序任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:op-task:delete')")
    public CommonResult<Boolean> deleteOpTask(@RequestParam("id") Long id) {
        opTaskService.deleteOpTask(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-工序任务")
                @PreAuthorize("@ss.hasPermission('mes:op-task:delete')")
    public CommonResult<Boolean> deleteOpTaskList(@RequestParam("ids") List<Long> ids) {
        opTaskService.deleteOpTaskListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-工序任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:op-task:query')")
    public CommonResult<OpTaskRespVO> getOpTask(@RequestParam("id") Long id) {
        return success(opTaskService.getOpTask(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-工序任务分页")
    @PreAuthorize("@ss.hasPermission('mes:op-task:query')")
    public CommonResult<PageResult<OpTaskRespVO>> getOpTaskPage(@Valid OpTaskPageReqVO pageReqVO) {
        return success(opTaskService.getOpTaskPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-工序任务 Excel")
    @PreAuthorize("@ss.hasPermission('mes:op-task:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOpTaskExcel(@Valid OpTaskPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OpTaskRespVO> list = opTaskService.getOpTaskPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-工序任务.xls", "数据", OpTaskRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-工序任务模板")
    @PreAuthorize("@ss.hasPermission('mes:op-task:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-工序任务导入模板.xls", "数据",
                OpTaskImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-工序任务")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:op-task:import')")
    public CommonResult<OpTaskImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<OpTaskImportExcelVO> list = ExcelUtils.read(file, OpTaskImportExcelVO.class);
        return success(opTaskService.importOpTaskList(list));
    }

}