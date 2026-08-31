package com.iboot.pf.module.mes.controller.admin.dispatchtask;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.dispatchtask.vo.DispatchTaskSaveReqVO;
import com.iboot.pf.module.mes.service.dispatchtask.DispatchTaskService;
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
 * 派工任务管理端接口。
 *
 * <p>派工是车间现场动作，不提供 Excel 导入。
 */
@Tag(name = "管理后台 - MES-派工任务")
@RestController
@RequestMapping("/mes/dispatch-task")
@Validated
public class DispatchTaskController {

    @Resource
    private DispatchTaskService dispatchTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建派工任务")
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:create')")
    public CommonResult<Long> createDispatchTask(@Valid @RequestBody DispatchTaskSaveReqVO createReqVO) {
        return success(dispatchTaskService.createDispatchTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新派工任务")
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:update')")
    public CommonResult<Boolean> updateDispatchTask(@Valid @RequestBody DispatchTaskSaveReqVO updateReqVO) {
        dispatchTaskService.updateDispatchTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除派工任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:delete')")
    public CommonResult<Boolean> deleteDispatchTask(@RequestParam("id") Long id) {
        dispatchTaskService.deleteDispatchTask(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除派工任务")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:delete')")
    public CommonResult<Boolean> deleteDispatchTaskList(@RequestParam("ids") List<Long> ids) {
        dispatchTaskService.deleteDispatchTaskListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得派工任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:query')")
    public CommonResult<DispatchTaskRespVO> getDispatchTask(@RequestParam("id") Long id) {
        return success(dispatchTaskService.getDispatchTask(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得派工任务分页")
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:query')")
    public CommonResult<PageResult<DispatchTaskRespVO>> getDispatchTaskPage(
            @Valid DispatchTaskPageReqVO pageReqVO) {
        return success(dispatchTaskService.getDispatchTaskPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出派工任务 Excel")
    @PreAuthorize("@ss.hasPermission('mes:dispatch-task:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDispatchTaskExcel(@Valid DispatchTaskPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DispatchTaskRespVO> list = dispatchTaskService.getDispatchTaskPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-派工任务.xls", "数据", DispatchTaskRespVO.class, list);
    }

}
