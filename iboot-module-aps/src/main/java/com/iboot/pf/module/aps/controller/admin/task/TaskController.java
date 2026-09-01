package com.iboot.pf.module.aps.controller.admin.task;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.framework.security.core.util.SecurityFrameworkUtils;
import com.iboot.pf.module.aps.api.task.dto.LockTaskReqDTO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskAdjustReqVO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskGanttRespVO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskPageReqVO;
import com.iboot.pf.module.aps.controller.admin.task.vo.TaskRespVO;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.enums.ApsTaskLockTypeEnum;
import com.iboot.pf.module.aps.service.task.ApsTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.UPDATE;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * APS-排程任务。
 *
 * <p>任务是求解结果：提供查询、导出、人工调整（换资源/改时间/改顺序）与锁定、解锁，
 * 不提供新增、删除、导入。任务必须来自工单投影，凭空建一条既没有工单也没有工序快照。
 */
@Tag(name = "管理后台 - APS-排程任务")
@RestController
@RequestMapping("/aps/task")
@Validated
public class TaskController {

    @Resource
    private ApsTaskService taskService;

    @GetMapping("/get")
    @Operation(summary = "获得排程任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:task:query')")
    public CommonResult<TaskRespVO> getTask(@RequestParam("id") Long id) {
        return success(taskService.getTask(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得排程任务分页")
    @PreAuthorize("@ss.hasPermission('aps:task:query')")
    public CommonResult<PageResult<TaskRespVO>> getTaskPage(@Valid TaskPageReqVO pageReqVO) {
        return success(taskService.getTaskPage(pageReqVO));
    }

    @GetMapping("/gantt")
    @Operation(summary = "获得排程甘特图数据")
    @Parameter(name = "versionId", description = "排程版本编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:task:query')")
    public CommonResult<TaskGanttRespVO> getTaskGantt(@RequestParam("versionId") Long versionId) {
        return success(taskService.getTaskGantt(versionId));
    }

    @PutMapping("/adjust")
    @Operation(summary = "调整排程任务的资源与计划时间")
    @PreAuthorize("@ss.hasPermission('aps:task:update')")
    public CommonResult<Boolean> adjustTask(@Valid @RequestBody TaskAdjustReqVO adjustReqVO) {
        taskService.adjustTask(adjustReqVO);
        return success(true);
    }

    @PutMapping("/lock")
    @Operation(summary = "锁定排程任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @Parameter(name = "reason", description = "锁定原因")
    @PreAuthorize("@ss.hasPermission('aps:task:update')")
    @ApiAccessLog(operateType = UPDATE)
    public CommonResult<Boolean> lockTask(@RequestParam("id") Long id,
                                         @RequestParam(value = "reason", required = false) String reason) {
        taskService.lockTask(buildCommand(ApsCommandTypeEnum.LOCK_TASK, id, reason));
        return success(true);
    }

    @PutMapping("/unlock")
    @Operation(summary = "解锁排程任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @Parameter(name = "reason", description = "解锁原因")
    @PreAuthorize("@ss.hasPermission('aps:task:update')")
    @ApiAccessLog(operateType = UPDATE)
    public CommonResult<Boolean> unlockTask(@RequestParam("id") Long id,
                                            @RequestParam(value = "reason", required = false) String reason) {
        taskService.unlockTask(buildCommand(ApsCommandTypeEnum.UNLOCK_TASK, id, reason));
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出排程任务 Excel")
    @PreAuthorize("@ss.hasPermission('aps:task:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTaskExcel(@Valid TaskPageReqVO pageReqVO,
                                HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TaskRespVO> list = taskService.getTaskPage(pageReqVO).getList();
        ExcelUtils.write(response, "APS-排程任务.xls", "数据", TaskRespVO.class, list);
    }

    /**
     * 把后台人工操作包装成跨模块命令。
     *
     * <p>幂等键带上时间戳：同一个任务可能反复锁定与解锁，键不能只用任务编号，否则第二次操作会被当成重复下发。
     */
    private LockTaskReqDTO buildCommand(ApsCommandTypeEnum commandType, Long taskId, String reason) {
        LockTaskReqDTO reqDTO = new LockTaskReqDTO();
        reqDTO.setTaskId(taskId);
        reqDTO.setLockType(ApsTaskLockTypeEnum.MANUAL.name());
        reqDTO.setReason(reason);
        reqDTO.setOperatorId(SecurityFrameworkUtils.getLoginUserId());
        reqDTO.setIdempotencyKey(ApsSourceModuleEnum.UI.name() + ":" + commandType.name() + ":" + taskId
                + ":" + System.currentTimeMillis());
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(taskId));
        return reqDTO;
    }

}
