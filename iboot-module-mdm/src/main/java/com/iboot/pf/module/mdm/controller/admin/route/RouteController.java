package com.iboot.pf.module.mdm.controller.admin.route;

import org.springframework.web.bind.annotation.*;
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
import com.iboot.pf.framework.common.util.object.BeanUtils;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

import com.iboot.pf.framework.excel.core.util.ExcelUtils;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.*;

import com.iboot.pf.module.mdm.controller.admin.route.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.route.RouteDO;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;
import com.iboot.pf.module.mdm.service.route.RouteService;

@Tag(name = "管理后台 - 工艺路线")
@RestController
@RequestMapping("/mdm/route")
@Validated
public class RouteController {

    @Resource
    private RouteService routeService;

    @PostMapping("/create")
    @Operation(summary = "创建工艺路线")
    @PreAuthorize("@ss.hasPermission('mdm:route:create')")
    public CommonResult<Long> createRoute(@Valid @RequestBody RouteSaveReqVO createReqVO) {
        return success(routeService.createRoute(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工艺路线")
    @PreAuthorize("@ss.hasPermission('mdm:route:update')")
    public CommonResult<Boolean> updateRoute(@Valid @RequestBody RouteSaveReqVO updateReqVO) {
        routeService.updateRoute(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工艺路线")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:route:delete')")
    public CommonResult<Boolean> deleteRoute(@RequestParam("id") Long id) {
        routeService.deleteRoute(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除工艺路线")
                @PreAuthorize("@ss.hasPermission('mdm:route:delete')")
    public CommonResult<Boolean> deleteRouteList(@RequestParam("ids") List<Long> ids) {
        routeService.deleteRouteListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工艺路线")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:route:query')")
    public CommonResult<RouteRespVO> getRoute(@RequestParam("id") Long id) {
        RouteDO route = routeService.getRoute(id);
        return success(BeanUtils.toBean(route, RouteRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工艺路线分页")
    @PreAuthorize("@ss.hasPermission('mdm:route:query')")
    public CommonResult<PageResult<RouteRespVO>> getRoutePage(@Valid RoutePageReqVO pageReqVO) {
        PageResult<RouteDO> pageResult = routeService.getRoutePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RouteRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出工艺路线 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:route:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRouteExcel(@Valid RoutePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<RouteDO> list = routeService.getRoutePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "工艺路线.xls", "数据", RouteRespVO.class,
                        BeanUtils.toBean(list, RouteRespVO.class));
    }

    // ==================== 子表（工艺路线工序） ====================

    @GetMapping("/route-op/page")
    @Operation(summary = "获得工艺路线工序分页")
    @Parameter(name = "routeId", description = "工艺路线编号")
    @PreAuthorize("@ss.hasPermission('mdm:route:query')")
    public CommonResult<PageResult<RouteOpDO>> getRouteOpPage(PageParam pageReqVO,
                                                                                        @RequestParam("routeId") Long routeId) {
        return success(routeService.getRouteOpPage(pageReqVO, routeId));
    }

    @PostMapping("/route-op/create")
    @Operation(summary = "创建工艺路线工序")
    @PreAuthorize("@ss.hasPermission('mdm:route:create')")
    public CommonResult<Long> createRouteOp(@Valid @RequestBody RouteOpDO routeOp) {
        return success(routeService.createRouteOp(routeOp));
    }

    @PutMapping("/route-op/update")
    @Operation(summary = "更新工艺路线工序")
    @PreAuthorize("@ss.hasPermission('mdm:route:update')")
    public CommonResult<Boolean> updateRouteOp(@Valid @RequestBody RouteOpDO routeOp) {
        routeService.updateRouteOp(routeOp);
        return success(true);
    }

    @DeleteMapping("/route-op/delete")
    @Parameter(name = "id", description = "编号", required = true)
    @Operation(summary = "删除工艺路线工序")
    @PreAuthorize("@ss.hasPermission('mdm:route:delete')")
    public CommonResult<Boolean> deleteRouteOp(@RequestParam("id") Long id) {
        routeService.deleteRouteOp(id);
        return success(true);
    }

    @DeleteMapping("/route-op/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除工艺路线工序")
    @PreAuthorize("@ss.hasPermission('mdm:route:delete')")
    public CommonResult<Boolean> deleteRouteOpList(@RequestParam("ids") List<Long> ids) {
        routeService.deleteRouteOpListByIds(ids);
        return success(true);
    }

	@GetMapping("/route-op/get")
	@Operation(summary = "获得工艺路线工序")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:route:query')")
	public CommonResult<RouteOpDO> getRouteOp(@RequestParam("id") Long id) {
	    return success(routeService.getRouteOp(id));
	}

}