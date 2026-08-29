package com.iboot.pf.module.mdm.controller.admin.esop;

import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.esop.vo.*;
import com.iboot.pf.module.mdm.service.esop.RouteOpEsopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.iboot.pf.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 工序 ESOP 作业步骤")
@RestController
@RequestMapping("/mdm/route-op-esop")
@Validated
public class RouteOpEsopController {

    @Resource
    private RouteOpEsopService routeOpEsopService;

    @PostMapping("/create")
    @Operation(summary = "创建 ESOP 作业步骤")
    @PreAuthorize("@ss.hasPermission('mdm:esop:create')")
    public CommonResult<Long> createRouteOpEsop(@Valid @RequestBody RouteOpEsopSaveReqVO createReqVO) {
        return success(routeOpEsopService.createRouteOpEsop(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 ESOP 作业步骤")
    @PreAuthorize("@ss.hasPermission('mdm:esop:update')")
    public CommonResult<Boolean> updateRouteOpEsop(@Valid @RequestBody RouteOpEsopSaveReqVO updateReqVO) {
        routeOpEsopService.updateRouteOpEsop(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 ESOP 作业步骤")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:esop:delete')")
    public CommonResult<Boolean> deleteRouteOpEsop(@RequestParam("id") Long id) {
        routeOpEsopService.deleteRouteOpEsop(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除 ESOP 作业步骤")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:esop:delete')")
    public CommonResult<Boolean> deleteRouteOpEsopList(@RequestParam("ids") List<Long> ids) {
        routeOpEsopService.deleteRouteOpEsopListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 ESOP 作业步骤")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:esop:query')")
    public CommonResult<RouteOpEsopRespVO> getRouteOpEsop(@RequestParam("id") Long id) {
        return success(routeOpEsopService.getRouteOpEsop(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 ESOP 作业步骤分页")
    @PreAuthorize("@ss.hasPermission('mdm:esop:query')")
    public CommonResult<PageResult<RouteOpEsopRespVO>> getRouteOpEsopPage(@Valid PageParam pageParam,
                                                                          @Valid RouteOpEsopPageReqVO pageReqVO) {
        return success(routeOpEsopService.getRouteOpEsopPage(pageParam, pageReqVO));
    }

    @GetMapping("/list-by-route-op")
    @Operation(summary = "按路线工序批量查询作业步骤")
    @Parameter(name = "routeOpId", description = "路线工序编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:esop:query')")
    public CommonResult<List<RouteOpEsopRespVO>> getRouteOpEsopListByRouteOpId(
            @RequestParam("routeOpId") Long routeOpId) {
        return success(routeOpEsopService.getRouteOpEsopListByRouteOpId(routeOpId));
    }

}
