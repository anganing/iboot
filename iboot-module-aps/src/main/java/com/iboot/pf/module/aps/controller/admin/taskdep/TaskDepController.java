package com.iboot.pf.module.aps.controller.admin.taskdep;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.TaskDepPageReqVO;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.TaskDepRespVO;
import com.iboot.pf.module.aps.controller.admin.taskdep.vo.TaskDepSaveReqVO;
import com.iboot.pf.module.aps.service.taskdep.ApsTaskDepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * APS-排程任务依赖。
 *
 * <p>依赖是人工可维护的约束边，提供完整 CRUD 与导出；不提供导入：一条边要指定两个任务编号，
 * 靠表格填 ID 极易填错。
 */
@Tag(name = "管理后台 - APS-排程任务依赖")
@RestController
@RequestMapping("/aps/task-dep")
@Validated
public class TaskDepController {

    @Resource
    private ApsTaskDepService taskDepService;

    @PostMapping("/create")
    @Operation(summary = "创建排程任务依赖")
    @PreAuthorize("@ss.hasPermission('aps:task-dep:create')")
    public CommonResult<Long> createTaskDep(@Valid @RequestBody TaskDepSaveReqVO createReqVO) {
        return success(taskDepService.createTaskDep(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新排程任务依赖")
    @PreAuthorize("@ss.hasPermission('aps:task-dep:update')")
    public CommonResult<Boolean> updateTaskDep(@Valid @RequestBody TaskDepSaveReqVO updateReqVO) {
        taskDepService.updateTaskDep(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除排程任务依赖")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:task-dep:delete')")
    public CommonResult<Boolean> deleteTaskDep(@RequestParam("id") Long id) {
        taskDepService.deleteTaskDep(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除排程任务依赖")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('aps:task-dep:delete')")
    public CommonResult<Boolean> deleteTaskDepList(@RequestParam("ids") List<Long> ids) {
        taskDepService.deleteTaskDepListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得排程任务依赖")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:task-dep:query')")
    public CommonResult<TaskDepRespVO> getTaskDep(@RequestParam("id") Long id) {
        return success(taskDepService.getTaskDep(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得排程任务依赖分页")
    @PreAuthorize("@ss.hasPermission('aps:task-dep:query')")
    public CommonResult<PageResult<TaskDepRespVO>> getTaskDepPage(@Valid TaskDepPageReqVO pageReqVO) {
        return success(taskDepService.getTaskDepPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出排程任务依赖 Excel")
    @PreAuthorize("@ss.hasPermission('aps:task-dep:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTaskDepExcel(@Valid TaskDepPageReqVO pageReqVO,
                                   HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TaskDepRespVO> list = taskDepService.getTaskDepPage(pageReqVO).getList();
        ExcelUtils.write(response, "APS-排程任务依赖.xls", "数据", TaskDepRespVO.class, list);
    }

}
