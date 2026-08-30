package com.iboot.pf.module.aps.controller.admin.version;

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

import com.iboot.pf.module.aps.controller.admin.version.vo.*;
import com.iboot.pf.module.aps.service.version.VersionService;

@Tag(name = "管理后台 - 排程版本")
@RestController
@RequestMapping("/aps/version")
@Validated
public class VersionController {

    @Resource
    private VersionService versionService;

    @PostMapping("/create")
    @Operation(summary = "创建排程版本")
    @PreAuthorize("@ss.hasPermission('aps:version:create')")
    public CommonResult<Long> createVersion(@Valid @RequestBody VersionSaveReqVO createReqVO) {
        return success(versionService.createVersion(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新排程版本")
    @PreAuthorize("@ss.hasPermission('aps:version:update')")
    public CommonResult<Boolean> updateVersion(@Valid @RequestBody VersionSaveReqVO updateReqVO) {
        versionService.updateVersion(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除排程版本")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('aps:version:delete')")
    public CommonResult<Boolean> deleteVersion(@RequestParam("id") Long id) {
        versionService.deleteVersion(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除排程版本")
                @PreAuthorize("@ss.hasPermission('aps:version:delete')")
    public CommonResult<Boolean> deleteVersionList(@RequestParam("ids") List<Long> ids) {
        versionService.deleteVersionListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得排程版本")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('aps:version:query')")
    public CommonResult<VersionRespVO> getVersion(@RequestParam("id") Long id) {
        return success(versionService.getVersion(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得排程版本分页")
    @PreAuthorize("@ss.hasPermission('aps:version:query')")
    public CommonResult<PageResult<VersionRespVO>> getVersionPage(@Valid VersionPageReqVO pageReqVO) {
        return success(versionService.getVersionPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出排程版本 Excel")
    @PreAuthorize("@ss.hasPermission('aps:version:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportVersionExcel(@Valid VersionPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<VersionRespVO> list = versionService.getVersionPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "排程版本.xls", "数据", VersionRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入排程版本模板")
    @PreAuthorize("@ss.hasPermission('aps:version:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "排程版本导入模板.xls", "数据",
                VersionImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入排程版本")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('aps:version:import')")
    public CommonResult<VersionImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<VersionImportExcelVO> list = ExcelUtils.read(file, VersionImportExcelVO.class);
        return success(versionService.importVersionList(list));
    }

}