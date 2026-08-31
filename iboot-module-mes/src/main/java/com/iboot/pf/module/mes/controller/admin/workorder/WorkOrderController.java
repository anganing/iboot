package com.iboot.pf.module.mes.controller.admin.workorder;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderPageReqVO;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderRespVO;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderSaveReqVO;
import com.iboot.pf.module.mes.service.workorder.WorkOrderService;
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
 * 作业工单管理端接口。
 *
 * <p>作业工单由生产工单派生，不提供 Excel 导入：组织维度必须与生产工单一致，
 * 让用户从 Excel 里自由填一批作业工单只会制造对不上的主子单。
 */
@Tag(name = "管理后台 - MES-作业工单")
@RestController
@RequestMapping("/mes/work-order")
@Validated
public class WorkOrderController {

    @Resource
    private WorkOrderService workOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建作业工单")
    @PreAuthorize("@ss.hasPermission('mes:work-order:create')")
    public CommonResult<Long> createWorkOrder(@Valid @RequestBody WorkOrderSaveReqVO createReqVO) {
        return success(workOrderService.createWorkOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新作业工单")
    @PreAuthorize("@ss.hasPermission('mes:work-order:update')")
    public CommonResult<Boolean> updateWorkOrder(@Valid @RequestBody WorkOrderSaveReqVO updateReqVO) {
        workOrderService.updateWorkOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除作业工单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:work-order:delete')")
    public CommonResult<Boolean> deleteWorkOrder(@RequestParam("id") Long id) {
        workOrderService.deleteWorkOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除作业工单")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:work-order:delete')")
    public CommonResult<Boolean> deleteWorkOrderList(@RequestParam("ids") List<Long> ids) {
        workOrderService.deleteWorkOrderListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得作业工单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:work-order:query')")
    public CommonResult<WorkOrderRespVO> getWorkOrder(@RequestParam("id") Long id) {
        return success(workOrderService.getWorkOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得作业工单分页")
    @PreAuthorize("@ss.hasPermission('mes:work-order:query')")
    public CommonResult<PageResult<WorkOrderRespVO>> getWorkOrderPage(@Valid WorkOrderPageReqVO pageReqVO) {
        return success(workOrderService.getWorkOrderPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出作业工单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:work-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWorkOrderExcel(@Valid WorkOrderPageReqVO pageReqVO,
                                     HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WorkOrderRespVO> list = workOrderService.getWorkOrderPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-作业工单.xls", "数据", WorkOrderRespVO.class, list);
    }

}
