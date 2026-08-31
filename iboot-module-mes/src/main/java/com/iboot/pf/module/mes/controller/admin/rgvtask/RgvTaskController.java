package com.iboot.pf.module.mes.controller.admin.rgvtask;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.RgvTaskSaveReqVO;
import com.iboot.pf.module.mes.service.rgvtask.RgvTaskService;
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
 * RGV 工位流转任务管理端接口。
 *
 * <p>任务由工序流转触发或外部调度系统下发，不提供 Excel 导入。
 */
@Tag(name = "管理后台 - MES-RGV 工位流转任务")
@RestController
@RequestMapping("/mes/rgv-task")
@Validated
public class RgvTaskController {

    @Resource
    private RgvTaskService rgvTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建 RGV 任务")
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:create')")
    public CommonResult<Long> createRgvTask(@Valid @RequestBody RgvTaskSaveReqVO createReqVO) {
        return success(rgvTaskService.createRgvTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 RGV 任务")
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:update')")
    public CommonResult<Boolean> updateRgvTask(@Valid @RequestBody RgvTaskSaveReqVO updateReqVO) {
        rgvTaskService.updateRgvTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 RGV 任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:delete')")
    public CommonResult<Boolean> deleteRgvTask(@RequestParam("id") Long id) {
        rgvTaskService.deleteRgvTask(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除 RGV 任务")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:delete')")
    public CommonResult<Boolean> deleteRgvTaskList(@RequestParam("ids") List<Long> ids) {
        rgvTaskService.deleteRgvTaskListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 RGV 任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:query')")
    public CommonResult<RgvTaskRespVO> getRgvTask(@RequestParam("id") Long id) {
        return success(rgvTaskService.getRgvTask(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 RGV 任务分页")
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:query')")
    public CommonResult<PageResult<RgvTaskRespVO>> getRgvTaskPage(@Valid RgvTaskPageReqVO pageReqVO) {
        return success(rgvTaskService.getRgvTaskPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出 RGV 任务 Excel")
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRgvTaskExcel(@Valid RgvTaskPageReqVO pageReqVO,
                                   HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<RgvTaskRespVO> list = rgvTaskService.getRgvTaskPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-RGV工位流转任务.xls", "数据", RgvTaskRespVO.class, list);
    }

}
