package com.iboot.pf.module.mes.controller.admin.rgvtask;

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

import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.*;
import com.iboot.pf.module.mes.service.rgvtask.RgvTaskService;

@Tag(name = "管理后台 - MES-RGV工位流转任务")
@RestController
@RequestMapping("/mes/rgv-task")
@Validated
public class RgvTaskController {

    @Resource
    private RgvTaskService rgvTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-RGV工位流转任务")
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:create')")
    public CommonResult<Long> createRgvTask(@Valid @RequestBody RgvTaskSaveReqVO createReqVO) {
        return success(rgvTaskService.createRgvTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-RGV工位流转任务")
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:update')")
    public CommonResult<Boolean> updateRgvTask(@Valid @RequestBody RgvTaskSaveReqVO updateReqVO) {
        rgvTaskService.updateRgvTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-RGV工位流转任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:delete')")
    public CommonResult<Boolean> deleteRgvTask(@RequestParam("id") Long id) {
        rgvTaskService.deleteRgvTask(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-RGV工位流转任务")
                @PreAuthorize("@ss.hasPermission('mes:rgv-task:delete')")
    public CommonResult<Boolean> deleteRgvTaskList(@RequestParam("ids") List<Long> ids) {
        rgvTaskService.deleteRgvTaskListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-RGV工位流转任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:query')")
    public CommonResult<RgvTaskRespVO> getRgvTask(@RequestParam("id") Long id) {
        return success(rgvTaskService.getRgvTask(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-RGV工位流转任务分页")
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:query')")
    public CommonResult<PageResult<RgvTaskRespVO>> getRgvTaskPage(@Valid RgvTaskPageReqVO pageReqVO) {
        return success(rgvTaskService.getRgvTaskPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-RGV工位流转任务 Excel")
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRgvTaskExcel(@Valid RgvTaskPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<RgvTaskRespVO> list = rgvTaskService.getRgvTaskPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-RGV工位流转任务.xls", "数据", RgvTaskRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-RGV工位流转任务模板")
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-RGV工位流转任务导入模板.xls", "数据",
                RgvTaskImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-RGV工位流转任务")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:rgv-task:import')")
    public CommonResult<RgvTaskImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<RgvTaskImportExcelVO> list = ExcelUtils.read(file, RgvTaskImportExcelVO.class);
        return success(rgvTaskService.importRgvTaskList(list));
    }

}