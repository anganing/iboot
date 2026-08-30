package com.iboot.pf.module.aps.controller.admin.taskdep;

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

import com.iboot.pf.module.aps.controller.admin.taskdep.vo.*;
import com.iboot.pf.module.aps.service.taskdep.TaskDepService;

@Tag(name = "管理后台 - 任务依赖")
@RestController
@RequestMapping("/aps/task-dep")
@Validated
public class TaskDepController {

    @Resource
    private TaskDepService taskDepService;

    @PostMapping("/create")
    @Operation(summary = "创建任务依赖")
    @PreAuthorize("@ss.hasPermission('aps:task-dep:create')")
    public CommonResult<Long> createTaskDep(@Valid @RequestBody TaskDepSaveReqVO createReqVO) {
        return success(taskDepService.createTaskDep(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新任务依赖")
    @PreAuthorize("@ss.hasPermission('aps:task-dep:update')")
    public CommonResult<Boolean> updateTaskDep(@Valid @RequestBody TaskDepSaveReqVO updateReqVO) {
        taskDepService.updateTaskDep(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除任务依赖")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('aps:task-dep:delete')")
    public CommonResult<Boolean> deleteTaskDep(@RequestParam("id") Long id) {
        taskDepService.deleteTaskDep(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除任务依赖")
                @PreAuthorize("@ss.hasPermission('aps:task-dep:delete')")
    public CommonResult<Boolean> deleteTaskDepList(@RequestParam("ids") List<Long> ids) {
        taskDepService.deleteTaskDepListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得任务依赖")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('aps:task-dep:query')")
    public CommonResult<TaskDepRespVO> getTaskDep(@RequestParam("id") Long id) {
        return success(taskDepService.getTaskDep(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得任务依赖分页")
    @PreAuthorize("@ss.hasPermission('aps:task-dep:query')")
    public CommonResult<PageResult<TaskDepRespVO>> getTaskDepPage(@Valid TaskDepPageReqVO pageReqVO) {
        return success(taskDepService.getTaskDepPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出任务依赖 Excel")
    @PreAuthorize("@ss.hasPermission('aps:task-dep:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTaskDepExcel(@Valid TaskDepPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TaskDepRespVO> list = taskDepService.getTaskDepPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "任务依赖.xls", "数据", TaskDepRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入任务依赖模板")
    @PreAuthorize("@ss.hasPermission('aps:task-dep:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "任务依赖导入模板.xls", "数据",
                TaskDepImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入任务依赖")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('aps:task-dep:import')")
    public CommonResult<TaskDepImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<TaskDepImportExcelVO> list = ExcelUtils.read(file, TaskDepImportExcelVO.class);
        return success(taskDepService.importTaskDepList(list));
    }

}