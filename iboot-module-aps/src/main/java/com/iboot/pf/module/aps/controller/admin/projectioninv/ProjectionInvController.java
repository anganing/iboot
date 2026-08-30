package com.iboot.pf.module.aps.controller.admin.projectioninv;

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

import com.iboot.pf.module.aps.controller.admin.projectioninv.vo.*;
import com.iboot.pf.module.aps.service.projectioninv.ProjectionInvService;

@Tag(name = "管理后台 - 库存投影")
@RestController
@RequestMapping("/aps/projection-inv")
@Validated
public class ProjectionInvController {

    @Resource
    private ProjectionInvService projectionInvService;

    @PostMapping("/create")
    @Operation(summary = "创建库存投影")
    @PreAuthorize("@ss.hasPermission('aps:projection-inv:create')")
    public CommonResult<Long> createProjectionInv(@Valid @RequestBody ProjectionInvSaveReqVO createReqVO) {
        return success(projectionInvService.createProjectionInv(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库存投影")
    @PreAuthorize("@ss.hasPermission('aps:projection-inv:update')")
    public CommonResult<Boolean> updateProjectionInv(@Valid @RequestBody ProjectionInvSaveReqVO updateReqVO) {
        projectionInvService.updateProjectionInv(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库存投影")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('aps:projection-inv:delete')")
    public CommonResult<Boolean> deleteProjectionInv(@RequestParam("id") Long id) {
        projectionInvService.deleteProjectionInv(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除库存投影")
                @PreAuthorize("@ss.hasPermission('aps:projection-inv:delete')")
    public CommonResult<Boolean> deleteProjectionInvList(@RequestParam("ids") List<Long> ids) {
        projectionInvService.deleteProjectionInvListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库存投影")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('aps:projection-inv:query')")
    public CommonResult<ProjectionInvRespVO> getProjectionInv(@RequestParam("id") Long id) {
        return success(projectionInvService.getProjectionInv(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库存投影分页")
    @PreAuthorize("@ss.hasPermission('aps:projection-inv:query')")
    public CommonResult<PageResult<ProjectionInvRespVO>> getProjectionInvPage(@Valid ProjectionInvPageReqVO pageReqVO) {
        return success(projectionInvService.getProjectionInvPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库存投影 Excel")
    @PreAuthorize("@ss.hasPermission('aps:projection-inv:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProjectionInvExcel(@Valid ProjectionInvPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProjectionInvRespVO> list = projectionInvService.getProjectionInvPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库存投影.xls", "数据", ProjectionInvRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入库存投影模板")
    @PreAuthorize("@ss.hasPermission('aps:projection-inv:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "库存投影导入模板.xls", "数据",
                ProjectionInvImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入库存投影")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('aps:projection-inv:import')")
    public CommonResult<ProjectionInvImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ProjectionInvImportExcelVO> list = ExcelUtils.read(file, ProjectionInvImportExcelVO.class);
        return success(projectionInvService.importProjectionInvList(list));
    }

}