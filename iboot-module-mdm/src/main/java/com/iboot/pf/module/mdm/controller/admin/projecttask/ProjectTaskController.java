package com.iboot.pf.module.mdm.controller.admin.projecttask;

import org.springframework.web.bind.annotation.*;
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
import com.iboot.pf.framework.common.util.object.BeanUtils;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

import com.iboot.pf.framework.excel.core.util.ExcelUtils;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.*;

import com.iboot.pf.module.mdm.controller.admin.projecttask.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.projecttask.ProjectTaskDO;
import com.iboot.pf.module.mdm.service.projecttask.ProjectTaskService;

@Tag(name = "管理后台 - 项目任务")
@RestController
@RequestMapping("/mdm/project-task")
@Validated
public class ProjectTaskController {

    @Resource
    private ProjectTaskService projectTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建项目任务")
    @PreAuthorize("@ss.hasPermission('mdm:project-task:create')")
    public CommonResult<Long> createProjectTask(@Valid @RequestBody ProjectTaskSaveReqVO createReqVO) {
        return success(projectTaskService.createProjectTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新项目任务")
    @PreAuthorize("@ss.hasPermission('mdm:project-task:update')")
    public CommonResult<Boolean> updateProjectTask(@Valid @RequestBody ProjectTaskSaveReqVO updateReqVO) {
        projectTaskService.updateProjectTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除项目任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:project-task:delete')")
    public CommonResult<Boolean> deleteProjectTask(@RequestParam("id") Long id) {
        projectTaskService.deleteProjectTask(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除项目任务")
                @PreAuthorize("@ss.hasPermission('mdm:project-task:delete')")
    public CommonResult<Boolean> deleteProjectTaskList(@RequestParam("ids") List<Long> ids) {
        projectTaskService.deleteProjectTaskListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得项目任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:project-task:query')")
    public CommonResult<ProjectTaskRespVO> getProjectTask(@RequestParam("id") Long id) {
        ProjectTaskDO projectTask = projectTaskService.getProjectTask(id);
        return success(BeanUtils.toBean(projectTask, ProjectTaskRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得项目任务分页")
    @PreAuthorize("@ss.hasPermission('mdm:project-task:query')")
    public CommonResult<PageResult<ProjectTaskRespVO>> getProjectTaskPage(@Valid ProjectTaskPageReqVO pageReqVO) {
        PageResult<ProjectTaskDO> pageResult = projectTaskService.getProjectTaskPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ProjectTaskRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出项目任务 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:project-task:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProjectTaskExcel(@Valid ProjectTaskPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProjectTaskDO> list = projectTaskService.getProjectTaskPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "项目任务.xls", "数据", ProjectTaskRespVO.class,
                        BeanUtils.toBean(list, ProjectTaskRespVO.class));
    }

}