package com.iboot.pf.module.aps.controller.admin.projectionmat;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.aps.controller.admin.projectionmat.vo.ProjectionMatPageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectionmat.vo.ProjectionMatRespVO;
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
 * APS-物料投影。
 *
 * <p>投影是场景冻结的只读输入，只提供查询与导出。刷新入口在排程场景页面（{@code /aps/scenario/refresh-projection}），
 * 四张投影一起刷，避免出现物料刷了、库存没刷的半新半旧输入。
 */
@Tag(name = "管理后台 - APS-物料投影")
@RestController
@RequestMapping("/aps/projection-mat")
@Validated
public class ProjectionMatController {

    @Resource
    private ApsProjectionService projectionService;

    @GetMapping("/get")
    @Operation(summary = "获得物料投影")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:projection-mat:query')")
    public CommonResult<ProjectionMatRespVO> getProjectionMat(@RequestParam("id") Long id) {
        return success(projectionService.getMaterialProjection(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得物料投影分页")
    @PreAuthorize("@ss.hasPermission('aps:projection-mat:query')")
    public CommonResult<PageResult<ProjectionMatRespVO>> getProjectionMatPage(
            @Valid ProjectionMatPageReqVO pageReqVO) {
        return success(projectionService.getMaterialProjectionPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出物料投影 Excel")
    @PreAuthorize("@ss.hasPermission('aps:projection-mat:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProjectionMatExcel(@Valid ProjectionMatPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProjectionMatRespVO> list = projectionService.getMaterialProjectionPage(pageReqVO).getList();
        ExcelUtils.write(response, "APS-物料投影.xls", "数据", ProjectionMatRespVO.class, list);
    }

}
