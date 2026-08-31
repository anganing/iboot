package com.iboot.pf.module.wms.controller.admin.loc;

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

import com.iboot.pf.module.wms.controller.admin.loc.vo.*;
import com.iboot.pf.module.wms.service.loc.LocService;

@Tag(name = "管理后台 - WMS-库位")
@RestController
@RequestMapping("/wms/loc")
@Validated
public class LocController {

    @Resource
    private LocService locService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-库位")
    @PreAuthorize("@ss.hasPermission('wms:loc:create')")
    public CommonResult<Long> createLoc(@Valid @RequestBody LocSaveReqVO createReqVO) {
        return success(locService.createLoc(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-库位")
    @PreAuthorize("@ss.hasPermission('wms:loc:update')")
    public CommonResult<Boolean> updateLoc(@Valid @RequestBody LocSaveReqVO updateReqVO) {
        locService.updateLoc(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-库位")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:loc:delete')")
    public CommonResult<Boolean> deleteLoc(@RequestParam("id") Long id) {
        locService.deleteLoc(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-库位")
                @PreAuthorize("@ss.hasPermission('wms:loc:delete')")
    public CommonResult<Boolean> deleteLocList(@RequestParam("ids") List<Long> ids) {
        locService.deleteLocListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-库位")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:loc:query')")
    public CommonResult<LocRespVO> getLoc(@RequestParam("id") Long id) {
        return success(locService.getLoc(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-库位分页")
    @PreAuthorize("@ss.hasPermission('wms:loc:query')")
    public CommonResult<PageResult<LocRespVO>> getLocPage(@Valid LocPageReqVO pageReqVO) {
        return success(locService.getLocPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-库位 Excel")
    @PreAuthorize("@ss.hasPermission('wms:loc:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportLocExcel(@Valid LocPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<LocRespVO> list = locService.getLocPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-库位.xls", "数据", LocRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-库位模板")
    @PreAuthorize("@ss.hasPermission('wms:loc:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-库位导入模板.xls", "数据",
                LocImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-库位")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:loc:import')")
    public CommonResult<LocImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<LocImportExcelVO> list = ExcelUtils.read(file, LocImportExcelVO.class);
        return success(locService.importLocList(list));
    }

}