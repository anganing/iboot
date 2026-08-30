package com.iboot.pf.module.aps.controller.admin.tasklock;

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

import com.iboot.pf.module.aps.controller.admin.tasklock.vo.*;
import com.iboot.pf.module.aps.service.tasklock.TaskLockService;

@Tag(name = "管理后台 - APS-排程任务锁定记录")
@RestController
@RequestMapping("/aps/task-lock")
@Validated
public class TaskLockController {

    @Resource
    private TaskLockService taskLockService;

    @PostMapping("/create")
    @Operation(summary = "创建APS-排程任务锁定记录")
    @PreAuthorize("@ss.hasPermission('aps:task-lock:create')")
    public CommonResult<Long> createTaskLock(@Valid @RequestBody TaskLockSaveReqVO createReqVO) {
        return success(taskLockService.createTaskLock(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新APS-排程任务锁定记录")
    @PreAuthorize("@ss.hasPermission('aps:task-lock:update')")
    public CommonResult<Boolean> updateTaskLock(@Valid @RequestBody TaskLockSaveReqVO updateReqVO) {
        taskLockService.updateTaskLock(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除APS-排程任务锁定记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('aps:task-lock:delete')")
    public CommonResult<Boolean> deleteTaskLock(@RequestParam("id") Long id) {
        taskLockService.deleteTaskLock(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除APS-排程任务锁定记录")
                @PreAuthorize("@ss.hasPermission('aps:task-lock:delete')")
    public CommonResult<Boolean> deleteTaskLockList(@RequestParam("ids") List<Long> ids) {
        taskLockService.deleteTaskLockListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得APS-排程任务锁定记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('aps:task-lock:query')")
    public CommonResult<TaskLockRespVO> getTaskLock(@RequestParam("id") Long id) {
        return success(taskLockService.getTaskLock(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得APS-排程任务锁定记录分页")
    @PreAuthorize("@ss.hasPermission('aps:task-lock:query')")
    public CommonResult<PageResult<TaskLockRespVO>> getTaskLockPage(@Valid TaskLockPageReqVO pageReqVO) {
        return success(taskLockService.getTaskLockPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出APS-排程任务锁定记录 Excel")
    @PreAuthorize("@ss.hasPermission('aps:task-lock:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTaskLockExcel(@Valid TaskLockPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TaskLockRespVO> list = taskLockService.getTaskLockPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "APS-排程任务锁定记录.xls", "数据", TaskLockRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入APS-排程任务锁定记录模板")
    @PreAuthorize("@ss.hasPermission('aps:task-lock:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "APS-排程任务锁定记录导入模板.xls", "数据",
                TaskLockImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入APS-排程任务锁定记录")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('aps:task-lock:import')")
    public CommonResult<TaskLockImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<TaskLockImportExcelVO> list = ExcelUtils.read(file, TaskLockImportExcelVO.class);
        return success(taskLockService.importTaskLockList(list));
    }

}