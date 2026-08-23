package com.iboot.pf.module.mdm.controller.admin.routeopmat;

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

import com.iboot.pf.module.mdm.controller.admin.routeopmat.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.routeopmat.RouteOpMatDO;
import com.iboot.pf.module.mdm.service.routeopmat.RouteOpMatService;

@Tag(name = "管理后台 - 工序用料")
@RestController
@RequestMapping("/mdm/route-op-mat")
@Validated
public class RouteOpMatController {

    @Resource
    private RouteOpMatService routeOpMatService;

    @PostMapping("/create")
    @Operation(summary = "创建工序用料")
    @PreAuthorize("@ss.hasPermission('mdm:route-op-mat:create')")
    public CommonResult<Long> createRouteOpMat(@Valid @RequestBody RouteOpMatSaveReqVO createReqVO) {
        return success(routeOpMatService.createRouteOpMat(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工序用料")
    @PreAuthorize("@ss.hasPermission('mdm:route-op-mat:update')")
    public CommonResult<Boolean> updateRouteOpMat(@Valid @RequestBody RouteOpMatSaveReqVO updateReqVO) {
        routeOpMatService.updateRouteOpMat(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工序用料")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:route-op-mat:delete')")
    public CommonResult<Boolean> deleteRouteOpMat(@RequestParam("id") Long id) {
        routeOpMatService.deleteRouteOpMat(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除工序用料")
                @PreAuthorize("@ss.hasPermission('mdm:route-op-mat:delete')")
    public CommonResult<Boolean> deleteRouteOpMatList(@RequestParam("ids") List<Long> ids) {
        routeOpMatService.deleteRouteOpMatListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工序用料")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:route-op-mat:query')")
    public CommonResult<RouteOpMatRespVO> getRouteOpMat(@RequestParam("id") Long id) {
        RouteOpMatDO routeOpMat = routeOpMatService.getRouteOpMat(id);
        return success(BeanUtils.toBean(routeOpMat, RouteOpMatRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工序用料分页")
    @PreAuthorize("@ss.hasPermission('mdm:route-op-mat:query')")
    public CommonResult<PageResult<RouteOpMatRespVO>> getRouteOpMatPage(@Valid RouteOpMatPageReqVO pageReqVO) {
        PageResult<RouteOpMatDO> pageResult = routeOpMatService.getRouteOpMatPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RouteOpMatRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出工序用料 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:route-op-mat:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRouteOpMatExcel(@Valid RouteOpMatPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<RouteOpMatDO> list = routeOpMatService.getRouteOpMatPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "工序用料.xls", "数据", RouteOpMatRespVO.class,
                        BeanUtils.toBean(list, RouteOpMatRespVO.class));
    }

}