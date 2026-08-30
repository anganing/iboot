package com.iboot.pf.module.wms.controller.admin.zone;

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

import com.iboot.pf.module.wms.controller.admin.zone.vo.*;
import com.iboot.pf.module.wms.service.zone.ZoneService;

@Tag(name = "管理后台 - WMS-库区")
@RestController
@RequestMapping("/wms/zone")
@Validated
public class ZoneController {

    @Resource
    private ZoneService zoneService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-库区")
    @PreAuthorize("@ss.hasPermission('wms:zone:create')")
    public CommonResult<Long> createZone(@Valid @RequestBody ZoneSaveReqVO createReqVO) {
        return success(zoneService.createZone(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-库区")
    @PreAuthorize("@ss.hasPermission('wms:zone:update')")
    public CommonResult<Boolean> updateZone(@Valid @RequestBody ZoneSaveReqVO updateReqVO) {
        zoneService.updateZone(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-库区")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:zone:delete')")
    public CommonResult<Boolean> deleteZone(@RequestParam("id") Long id) {
        zoneService.deleteZone(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-库区")
                @PreAuthorize("@ss.hasPermission('wms:zone:delete')")
    public CommonResult<Boolean> deleteZoneList(@RequestParam("ids") List<Long> ids) {
        zoneService.deleteZoneListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-库区")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:zone:query')")
    public CommonResult<ZoneRespVO> getZone(@RequestParam("id") Long id) {
        return success(zoneService.getZone(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-库区分页")
    @PreAuthorize("@ss.hasPermission('wms:zone:query')")
    public CommonResult<PageResult<ZoneRespVO>> getZonePage(@Valid ZonePageReqVO pageReqVO) {
        return success(zoneService.getZonePage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-库区 Excel")
    @PreAuthorize("@ss.hasPermission('wms:zone:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportZoneExcel(@Valid ZonePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ZoneRespVO> list = zoneService.getZonePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-库区.xls", "数据", ZoneRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-库区模板")
    @PreAuthorize("@ss.hasPermission('wms:zone:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-库区导入模板.xls", "数据",
                ZoneImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-库区")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:zone:import')")
    public CommonResult<ZoneImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ZoneImportExcelVO> list = ExcelUtils.read(file, ZoneImportExcelVO.class);
        return success(zoneService.importZoneList(list));
    }

}