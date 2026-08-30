package com.iboot.pf.module.aps.controller.admin.projectionresource;

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

import com.iboot.pf.module.aps.controller.admin.projectionresource.vo.*;
import com.iboot.pf.module.aps.service.projectionresource.ProjectionResourceService;

@Tag(name = "管理后台 - 资源投影")
@RestController
@RequestMapping("/aps/projection-resource")
@Validated
public class ProjectionResourceController {

    @Resource
    private ProjectionResourceService projectionResourceService;

    @PostMapping("/create")
    @Operation(summary = "创建资源投影")
    @PreAuthorize("@ss.hasPermission('aps:projection-resource:create')")
    public CommonResult<Long> createProjectionResource(@Valid @RequestBody ProjectionResourceSaveReqVO createReqVO) {
        return success(projectionResourceService.createProjectionResource(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新资源投影")
    @PreAuthorize("@ss.hasPermission('aps:projection-resource:update')")
    public CommonResult<Boolean> updateProjectionResource(@Valid @RequestBody ProjectionResourceSaveReqVO updateReqVO) {
        projectionResourceService.updateProjectionResource(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除资源投影")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('aps:projection-resource:delete')")
    public CommonResult<Boolean> deleteProjectionResource(@RequestParam("id") Long id) {
        projectionResourceService.deleteProjectionResource(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除资源投影")
                @PreAuthorize("@ss.hasPermission('aps:projection-resource:delete')")
    public CommonResult<Boolean> deleteProjectionResourceList(@RequestParam("ids") List<Long> ids) {
        projectionResourceService.deleteProjectionResourceListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得资源投影")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('aps:projection-resource:query')")
    public CommonResult<ProjectionResourceRespVO> getProjectionResource(@RequestParam("id") Long id) {
        return success(projectionResourceService.getProjectionResource(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得资源投影分页")
    @PreAuthorize("@ss.hasPermission('aps:projection-resource:query')")
    public CommonResult<PageResult<ProjectionResourceRespVO>> getProjectionResourcePage(@Valid ProjectionResourcePageReqVO pageReqVO) {
        return success(projectionResourceService.getProjectionResourcePage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出资源投影 Excel")
    @PreAuthorize("@ss.hasPermission('aps:projection-resource:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProjectionResourceExcel(@Valid ProjectionResourcePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProjectionResourceRespVO> list = projectionResourceService.getProjectionResourcePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "资源投影.xls", "数据", ProjectionResourceRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入资源投影模板")
    @PreAuthorize("@ss.hasPermission('aps:projection-resource:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "资源投影导入模板.xls", "数据",
                ProjectionResourceImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入资源投影")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('aps:projection-resource:import')")
    public CommonResult<ProjectionResourceImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ProjectionResourceImportExcelVO> list = ExcelUtils.read(file, ProjectionResourceImportExcelVO.class);
        return success(projectionResourceService.importProjectionResourceList(list));
    }

}