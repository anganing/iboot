package com.iboot.pf.module.wms.controller.admin.wh;

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

import com.iboot.pf.module.wms.controller.admin.wh.vo.*;
import com.iboot.pf.module.wms.service.wh.WhService;

@Tag(name = "管理后台 - WMS-仓库")
@RestController
@RequestMapping("/wms/wh")
@Validated
public class WhController {

    @Resource
    private WhService whService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-仓库")
    @PreAuthorize("@ss.hasPermission('wms:wh:create')")
    public CommonResult<Long> createWh(@Valid @RequestBody WhSaveReqVO createReqVO) {
        return success(whService.createWh(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-仓库")
    @PreAuthorize("@ss.hasPermission('wms:wh:update')")
    public CommonResult<Boolean> updateWh(@Valid @RequestBody WhSaveReqVO updateReqVO) {
        whService.updateWh(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-仓库")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:wh:delete')")
    public CommonResult<Boolean> deleteWh(@RequestParam("id") Long id) {
        whService.deleteWh(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-仓库")
                @PreAuthorize("@ss.hasPermission('wms:wh:delete')")
    public CommonResult<Boolean> deleteWhList(@RequestParam("ids") List<Long> ids) {
        whService.deleteWhListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-仓库")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:wh:query')")
    public CommonResult<WhRespVO> getWh(@RequestParam("id") Long id) {
        return success(whService.getWh(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-仓库分页")
    @PreAuthorize("@ss.hasPermission('wms:wh:query')")
    public CommonResult<PageResult<WhRespVO>> getWhPage(@Valid WhPageReqVO pageReqVO) {
        return success(whService.getWhPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-仓库 Excel")
    @PreAuthorize("@ss.hasPermission('wms:wh:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWhExcel(@Valid WhPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WhRespVO> list = whService.getWhPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-仓库.xls", "数据", WhRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-仓库模板")
    @PreAuthorize("@ss.hasPermission('wms:wh:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-仓库导入模板.xls", "数据",
                WhImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-仓库")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:wh:import')")
    public CommonResult<WhImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<WhImportExcelVO> list = ExcelUtils.read(file, WhImportExcelVO.class);
        return success(whService.importWhList(list));
    }

}