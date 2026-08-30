package com.iboot.pf.module.mes.controller.admin.kitsnapshot;

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

import com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo.*;
import com.iboot.pf.module.mes.service.kitsnapshot.KitSnapshotService;

@Tag(name = "管理后台 - MES-齐套检查快照")
@RestController
@RequestMapping("/mes/kit-snapshot")
@Validated
public class KitSnapshotController {

    @Resource
    private KitSnapshotService kitSnapshotService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-齐套检查快照")
    @PreAuthorize("@ss.hasPermission('mes:kit-snapshot:create')")
    public CommonResult<Long> createKitSnapshot(@Valid @RequestBody KitSnapshotSaveReqVO createReqVO) {
        return success(kitSnapshotService.createKitSnapshot(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-齐套检查快照")
    @PreAuthorize("@ss.hasPermission('mes:kit-snapshot:update')")
    public CommonResult<Boolean> updateKitSnapshot(@Valid @RequestBody KitSnapshotSaveReqVO updateReqVO) {
        kitSnapshotService.updateKitSnapshot(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-齐套检查快照")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:kit-snapshot:delete')")
    public CommonResult<Boolean> deleteKitSnapshot(@RequestParam("id") Long id) {
        kitSnapshotService.deleteKitSnapshot(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-齐套检查快照")
                @PreAuthorize("@ss.hasPermission('mes:kit-snapshot:delete')")
    public CommonResult<Boolean> deleteKitSnapshotList(@RequestParam("ids") List<Long> ids) {
        kitSnapshotService.deleteKitSnapshotListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-齐套检查快照")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:kit-snapshot:query')")
    public CommonResult<KitSnapshotRespVO> getKitSnapshot(@RequestParam("id") Long id) {
        return success(kitSnapshotService.getKitSnapshot(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-齐套检查快照分页")
    @PreAuthorize("@ss.hasPermission('mes:kit-snapshot:query')")
    public CommonResult<PageResult<KitSnapshotRespVO>> getKitSnapshotPage(@Valid KitSnapshotPageReqVO pageReqVO) {
        return success(kitSnapshotService.getKitSnapshotPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-齐套检查快照 Excel")
    @PreAuthorize("@ss.hasPermission('mes:kit-snapshot:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportKitSnapshotExcel(@Valid KitSnapshotPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<KitSnapshotRespVO> list = kitSnapshotService.getKitSnapshotPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-齐套检查快照.xls", "数据", KitSnapshotRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-齐套检查快照模板")
    @PreAuthorize("@ss.hasPermission('mes:kit-snapshot:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-齐套检查快照导入模板.xls", "数据",
                KitSnapshotImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-齐套检查快照")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:kit-snapshot:import')")
    public CommonResult<KitSnapshotImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<KitSnapshotImportExcelVO> list = ExcelUtils.read(file, KitSnapshotImportExcelVO.class);
        return success(kitSnapshotService.importKitSnapshotList(list));
    }

}