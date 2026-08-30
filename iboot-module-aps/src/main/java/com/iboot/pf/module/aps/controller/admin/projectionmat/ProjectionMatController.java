package com.iboot.pf.module.aps.controller.admin.projectionmat;

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

import com.iboot.pf.module.aps.controller.admin.projectionmat.vo.*;
import com.iboot.pf.module.aps.service.projectionmat.ProjectionMatService;

@Tag(name = "管理后台 - APS-MDM物料投影")
@RestController
@RequestMapping("/aps/projection-mat")
@Validated
public class ProjectionMatController {

    @Resource
    private ProjectionMatService projectionMatService;

    @PostMapping("/create")
    @Operation(summary = "创建APS-MDM物料投影")
    @PreAuthorize("@ss.hasPermission('aps:projection-mat:create')")
    public CommonResult<Long> createProjectionMat(@Valid @RequestBody ProjectionMatSaveReqVO createReqVO) {
        return success(projectionMatService.createProjectionMat(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新APS-MDM物料投影")
    @PreAuthorize("@ss.hasPermission('aps:projection-mat:update')")
    public CommonResult<Boolean> updateProjectionMat(@Valid @RequestBody ProjectionMatSaveReqVO updateReqVO) {
        projectionMatService.updateProjectionMat(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除APS-MDM物料投影")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('aps:projection-mat:delete')")
    public CommonResult<Boolean> deleteProjectionMat(@RequestParam("id") Long id) {
        projectionMatService.deleteProjectionMat(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除APS-MDM物料投影")
                @PreAuthorize("@ss.hasPermission('aps:projection-mat:delete')")
    public CommonResult<Boolean> deleteProjectionMatList(@RequestParam("ids") List<Long> ids) {
        projectionMatService.deleteProjectionMatListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得APS-MDM物料投影")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('aps:projection-mat:query')")
    public CommonResult<ProjectionMatRespVO> getProjectionMat(@RequestParam("id") Long id) {
        return success(projectionMatService.getProjectionMat(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得APS-MDM物料投影分页")
    @PreAuthorize("@ss.hasPermission('aps:projection-mat:query')")
    public CommonResult<PageResult<ProjectionMatRespVO>> getProjectionMatPage(@Valid ProjectionMatPageReqVO pageReqVO) {
        return success(projectionMatService.getProjectionMatPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出APS-MDM物料投影 Excel")
    @PreAuthorize("@ss.hasPermission('aps:projection-mat:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProjectionMatExcel(@Valid ProjectionMatPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProjectionMatRespVO> list = projectionMatService.getProjectionMatPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "APS-MDM物料投影.xls", "数据", ProjectionMatRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入APS-MDM物料投影模板")
    @PreAuthorize("@ss.hasPermission('aps:projection-mat:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "APS-MDM物料投影导入模板.xls", "数据",
                ProjectionMatImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入APS-MDM物料投影")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('aps:projection-mat:import')")
    public CommonResult<ProjectionMatImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ProjectionMatImportExcelVO> list = ExcelUtils.read(file, ProjectionMatImportExcelVO.class);
        return success(projectionMatService.importProjectionMatList(list));
    }

}