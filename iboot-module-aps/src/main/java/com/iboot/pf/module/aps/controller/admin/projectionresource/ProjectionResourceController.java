package com.iboot.pf.module.aps.controller.admin.projectionresource;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.aps.controller.admin.projectionresource.vo.ProjectionResourcePageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectionresource.vo.ProjectionResourceRespVO;
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
 * APS-资源投影。
 *
 * <p>投影是场景冻结的只读输入，只提供查询与导出；刷新入口在排程场景页面。
 */
@Tag(name = "管理后台 - APS-资源投影")
@RestController
@RequestMapping("/aps/projection-resource")
@Validated
public class ProjectionResourceController {

    @Resource
    private ApsProjectionService projectionService;

    @GetMapping("/get")
    @Operation(summary = "获得资源投影")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:projection-resource:query')")
    public CommonResult<ProjectionResourceRespVO> getProjectionResource(@RequestParam("id") Long id) {
        return success(projectionService.getResourceProjection(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得资源投影分页")
    @PreAuthorize("@ss.hasPermission('aps:projection-resource:query')")
    public CommonResult<PageResult<ProjectionResourceRespVO>> getProjectionResourcePage(
            @Valid ProjectionResourcePageReqVO pageReqVO) {
        return success(projectionService.getResourceProjectionPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出资源投影 Excel")
    @PreAuthorize("@ss.hasPermission('aps:projection-resource:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProjectionResourceExcel(@Valid ProjectionResourcePageReqVO pageReqVO,
                                              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProjectionResourceRespVO> list = projectionService.getResourceProjectionPage(pageReqVO).getList();
        ExcelUtils.write(response, "APS-资源投影.xls", "数据", ProjectionResourceRespVO.class, list);
    }

}
