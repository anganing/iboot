package com.iboot.pf.module.aps.controller.admin.projectionorder;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.aps.controller.admin.projectionorder.vo.ProjectionOrderPageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectionorder.vo.ProjectionOrderRespVO;
import com.iboot.pf.module.aps.service.projection.ApsProjectionService;
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
 * APS-工单投影。
 *
 * <p>投影是场景冻结的只读输入，只提供查询与导出；刷新入口在排程场景页面。
 */
@Tag(name = "管理后台 - APS-工单投影")
@RestController
@RequestMapping("/aps/projection-order")
@Validated
public class ProjectionOrderController {

    @Resource
    private ApsProjectionService projectionService;

    @GetMapping("/get")
    @Operation(summary = "获得工单投影")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:projection-order:query')")
    public CommonResult<ProjectionOrderRespVO> getProjectionOrder(@RequestParam("id") Long id) {
        return success(projectionService.getOrderProjection(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工单投影分页")
    @PreAuthorize("@ss.hasPermission('aps:projection-order:query')")
    public CommonResult<PageResult<ProjectionOrderRespVO>> getProjectionOrderPage(
            @Valid ProjectionOrderPageReqVO pageReqVO) {
        return success(projectionService.getOrderProjectionPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出工单投影 Excel")
    @PreAuthorize("@ss.hasPermission('aps:projection-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProjectionOrderExcel(@Valid ProjectionOrderPageReqVO pageReqVO,
                                           HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProjectionOrderRespVO> list = projectionService.getOrderProjectionPage(pageReqVO).getList();
        ExcelUtils.write(response, "APS-工单投影.xls", "数据", ProjectionOrderRespVO.class, list);
    }

}
