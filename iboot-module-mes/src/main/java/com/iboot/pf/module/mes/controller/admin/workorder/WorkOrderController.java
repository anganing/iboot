package com.iboot.pf.module.mes.controller.admin.workorder;

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

import com.iboot.pf.module.mes.controller.admin.workorder.vo.*;
import com.iboot.pf.module.mes.service.workorder.WorkOrderService;

@Tag(name = "管理后台 - MES-作业工单")
@RestController
@RequestMapping("/mes/work-order")
@Validated
public class WorkOrderController {

    @Resource
    private WorkOrderService workOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-作业工单")
    @PreAuthorize("@ss.hasPermission('mes:work-order:create')")
    public CommonResult<Long> createWorkOrder(@Valid @RequestBody WorkOrderSaveReqVO createReqVO) {
        return success(workOrderService.createWorkOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-作业工单")
    @PreAuthorize("@ss.hasPermission('mes:work-order:update')")
    public CommonResult<Boolean> updateWorkOrder(@Valid @RequestBody WorkOrderSaveReqVO updateReqVO) {
        workOrderService.updateWorkOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-作业工单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:work-order:delete')")
    public CommonResult<Boolean> deleteWorkOrder(@RequestParam("id") Long id) {
        workOrderService.deleteWorkOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-作业工单")
                @PreAuthorize("@ss.hasPermission('mes:work-order:delete')")
    public CommonResult<Boolean> deleteWorkOrderList(@RequestParam("ids") List<Long> ids) {
        workOrderService.deleteWorkOrderListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-作业工单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:work-order:query')")
    public CommonResult<WorkOrderRespVO> getWorkOrder(@RequestParam("id") Long id) {
        return success(workOrderService.getWorkOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-作业工单分页")
    @PreAuthorize("@ss.hasPermission('mes:work-order:query')")
    public CommonResult<PageResult<WorkOrderRespVO>> getWorkOrderPage(@Valid WorkOrderPageReqVO pageReqVO) {
        return success(workOrderService.getWorkOrderPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-作业工单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:work-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWorkOrderExcel(@Valid WorkOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WorkOrderRespVO> list = workOrderService.getWorkOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-作业工单.xls", "数据", WorkOrderRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-作业工单模板")
    @PreAuthorize("@ss.hasPermission('mes:work-order:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-作业工单导入模板.xls", "数据",
                WorkOrderImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-作业工单")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:work-order:import')")
    public CommonResult<WorkOrderImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<WorkOrderImportExcelVO> list = ExcelUtils.read(file, WorkOrderImportExcelVO.class);
        return success(workOrderService.importWorkOrderList(list));
    }

}