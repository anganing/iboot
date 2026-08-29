package com.iboot.pf.module.mdm.controller.admin.tool;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mdm.controller.admin.tool.vo.*;
import com.iboot.pf.module.mdm.service.tool.ToolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 工装模具刀具")
@RestController
@RequestMapping("/mdm/tool")
@Validated
public class ToolController {

    @Resource
    private ToolService toolService;

    // ==================== 工装 ====================

    @PostMapping("/create")
    @Operation(summary = "创建工装")
    @PreAuthorize("@ss.hasPermission('mdm:tool:create')")
    public CommonResult<Long> createTool(@Valid @RequestBody ToolSaveReqVO createReqVO) {
        return success(toolService.createTool(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工装")
    @PreAuthorize("@ss.hasPermission('mdm:tool:update')")
    public CommonResult<Boolean> updateTool(@Valid @RequestBody ToolSaveReqVO updateReqVO) {
        toolService.updateTool(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工装")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:tool:delete')")
    public CommonResult<Boolean> deleteTool(@RequestParam("id") Long id) {
        toolService.deleteTool(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除工装")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:tool:delete')")
    public CommonResult<Boolean> deleteToolList(@RequestParam("ids") List<Long> ids) {
        toolService.deleteToolListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工装")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:tool:query')")
    public CommonResult<ToolRespVO> getTool(@RequestParam("id") Long id) {
        return success(toolService.getTool(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工装分页")
    @PreAuthorize("@ss.hasPermission('mdm:tool:query')")
    public CommonResult<PageResult<ToolRespVO>> getToolPage(@Valid ToolPageReqVO pageReqVO) {
        return success(toolService.getToolPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出工装 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:tool:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportToolExcel(@Valid ToolPageReqVO pageReqVO,
                                HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ToolRespVO> list = toolService.getToolPage(pageReqVO).getList();
        ExcelUtils.write(response, "工装.xls", "数据", ToolRespVO.class, list);
    }

    // ==================== 工序工装 ====================

    @PostMapping("/route-op/create")
    @Operation(summary = "创建工序工装")
    @PreAuthorize("@ss.hasPermission('mdm:tool:update')")
    public CommonResult<Long> createRouteOpTool(@Valid @RequestBody RouteOpToolSaveReqVO createReqVO) {
        return success(toolService.createRouteOpTool(createReqVO));
    }

    @PutMapping("/route-op/update")
    @Operation(summary = "更新工序工装")
    @PreAuthorize("@ss.hasPermission('mdm:tool:update')")
    public CommonResult<Boolean> updateRouteOpTool(@Valid @RequestBody RouteOpToolSaveReqVO updateReqVO) {
        toolService.updateRouteOpTool(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/route-op/delete")
    @Operation(summary = "删除工序工装")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:tool:update')")
    public CommonResult<Boolean> deleteRouteOpTool(@RequestParam("id") Long id) {
        toolService.deleteRouteOpTool(id);
        return success(true);
    }

    @DeleteMapping("/route-op/delete-list")
    @Operation(summary = "批量删除工序工装")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:tool:update')")
    public CommonResult<Boolean> deleteRouteOpToolList(@RequestParam("ids") List<Long> ids) {
        toolService.deleteRouteOpToolListByIds(ids);
        return success(true);
    }

    @GetMapping("/route-op/get")
    @Operation(summary = "获得工序工装")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:tool:query')")
    public CommonResult<RouteOpToolRespVO> getRouteOpTool(@RequestParam("id") Long id) {
        return success(toolService.getRouteOpTool(id));
    }

    @GetMapping("/route-op/page")
    @Operation(summary = "获得工序工装分页")
    @PreAuthorize("@ss.hasPermission('mdm:tool:query')")
    public CommonResult<PageResult<RouteOpToolRespVO>> getRouteOpToolPage(@Valid PageParam pageParam,
                                                                          @RequestParam("routeOpId") Long routeOpId) {
        return success(toolService.getRouteOpToolPage(pageParam, routeOpId));
    }

}
