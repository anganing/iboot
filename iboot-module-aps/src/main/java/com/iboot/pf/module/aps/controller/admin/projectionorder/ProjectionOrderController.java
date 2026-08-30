package com.iboot.pf.module.aps.controller.admin.projectionorder;

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

import com.iboot.pf.module.aps.controller.admin.projectionorder.vo.*;
import com.iboot.pf.module.aps.service.projectionorder.ProjectionOrderService;

@Tag(name = "管理后台 - APS-MES需求工单任务投影")
@RestController
@RequestMapping("/aps/projection-order")
@Validated
public class ProjectionOrderController {

    @Resource
    private ProjectionOrderService projectionOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建APS-MES需求工单任务投影")
    @PreAuthorize("@ss.hasPermission('aps:projection-order:create')")
    public CommonResult<Long> createProjectionOrder(@Valid @RequestBody ProjectionOrderSaveReqVO createReqVO) {
        return success(projectionOrderService.createProjectionOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新APS-MES需求工单任务投影")
    @PreAuthorize("@ss.hasPermission('aps:projection-order:update')")
    public CommonResult<Boolean> updateProjectionOrder(@Valid @RequestBody ProjectionOrderSaveReqVO updateReqVO) {
        projectionOrderService.updateProjectionOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除APS-MES需求工单任务投影")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('aps:projection-order:delete')")
    public CommonResult<Boolean> deleteProjectionOrder(@RequestParam("id") Long id) {
        projectionOrderService.deleteProjectionOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除APS-MES需求工单任务投影")
                @PreAuthorize("@ss.hasPermission('aps:projection-order:delete')")
    public CommonResult<Boolean> deleteProjectionOrderList(@RequestParam("ids") List<Long> ids) {
        projectionOrderService.deleteProjectionOrderListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得APS-MES需求工单任务投影")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('aps:projection-order:query')")
    public CommonResult<ProjectionOrderRespVO> getProjectionOrder(@RequestParam("id") Long id) {
        return success(projectionOrderService.getProjectionOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得APS-MES需求工单任务投影分页")
    @PreAuthorize("@ss.hasPermission('aps:projection-order:query')")
    public CommonResult<PageResult<ProjectionOrderRespVO>> getProjectionOrderPage(@Valid ProjectionOrderPageReqVO pageReqVO) {
        return success(projectionOrderService.getProjectionOrderPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出APS-MES需求工单任务投影 Excel")
    @PreAuthorize("@ss.hasPermission('aps:projection-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProjectionOrderExcel(@Valid ProjectionOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProjectionOrderRespVO> list = projectionOrderService.getProjectionOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "APS-MES需求工单任务投影.xls", "数据", ProjectionOrderRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入APS-MES需求工单任务投影模板")
    @PreAuthorize("@ss.hasPermission('aps:projection-order:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "APS-MES需求工单任务投影导入模板.xls", "数据",
                ProjectionOrderImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入APS-MES需求工单任务投影")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('aps:projection-order:import')")
    public CommonResult<ProjectionOrderImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ProjectionOrderImportExcelVO> list = ExcelUtils.read(file, ProjectionOrderImportExcelVO.class);
        return success(projectionOrderService.importProjectionOrderList(list));
    }

}