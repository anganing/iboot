package com.iboot.pf.module.aps.controller.admin.tasklock;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.aps.controller.admin.tasklock.vo.TaskLockPageReqVO;
import com.iboot.pf.module.aps.controller.admin.tasklock.vo.TaskLockRespVO;
import com.iboot.pf.module.aps.service.tasklock.ApsTaskLockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * APS-排程任务锁定记录。
 *
 * <p>锁定记录是留痕表，只提供查询与导出。锁定与解锁在排程任务页面操作，
 * 由 {@code /aps/task/lock} 与 {@code /aps/task/unlock} 同时维护任务标记与这里的记录。
 */
@Tag(name = "管理后台 - APS-排程任务锁定记录")
@RestController
@RequestMapping("/aps/task-lock")
@Validated
public class TaskLockController {

    @Resource
    private ApsTaskLockService taskLockService;

    @GetMapping("/get")
    @Operation(summary = "获得排程任务锁定记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:task-lock:query')")
    public CommonResult<TaskLockRespVO> getTaskLock(@RequestParam("id") Long id) {
        return success(taskLockService.getTaskLock(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得排程任务锁定记录分页")
    @PreAuthorize("@ss.hasPermission('aps:task-lock:query')")
    public CommonResult<PageResult<TaskLockRespVO>> getTaskLockPage(@Valid TaskLockPageReqVO pageReqVO) {
        return success(taskLockService.getTaskLockPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出排程任务锁定记录 Excel")
    @PreAuthorize("@ss.hasPermission('aps:task-lock:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTaskLockExcel(@Valid TaskLockPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TaskLockRespVO> list = taskLockService.getTaskLockPage(pageReqVO).getList();
        ExcelUtils.write(response, "APS-排程任务锁定记录.xls", "数据", TaskLockRespVO.class, list);
    }

}
