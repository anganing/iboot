package com.iboot.pf.module.aps.controller.admin.shortage;

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

import com.iboot.pf.module.aps.controller.admin.shortage.vo.*;
import com.iboot.pf.module.aps.service.shortage.ShortageService;

@Tag(name = "管理后台 - APS-排程缺料清单")
@RestController
@RequestMapping("/aps/shortage")
@Validated
public class ShortageController {

    @Resource
    private ShortageService shortageService;

    @PostMapping("/create")
    @Operation(summary = "创建APS-排程缺料清单")
    @PreAuthorize("@ss.hasPermission('aps:shortage:create')")
    public CommonResult<Long> createShortage(@Valid @RequestBody ShortageSaveReqVO createReqVO) {
        return success(shortageService.createShortage(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新APS-排程缺料清单")
    @PreAuthorize("@ss.hasPermission('aps:shortage:update')")
    public CommonResult<Boolean> updateShortage(@Valid @RequestBody ShortageSaveReqVO updateReqVO) {
        shortageService.updateShortage(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除APS-排程缺料清单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('aps:shortage:delete')")
    public CommonResult<Boolean> deleteShortage(@RequestParam("id") Long id) {
        shortageService.deleteShortage(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除APS-排程缺料清单")
                @PreAuthorize("@ss.hasPermission('aps:shortage:delete')")
    public CommonResult<Boolean> deleteShortageList(@RequestParam("ids") List<Long> ids) {
        shortageService.deleteShortageListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得APS-排程缺料清单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('aps:shortage:query')")
    public CommonResult<ShortageRespVO> getShortage(@RequestParam("id") Long id) {
        return success(shortageService.getShortage(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得APS-排程缺料清单分页")
    @PreAuthorize("@ss.hasPermission('aps:shortage:query')")
    public CommonResult<PageResult<ShortageRespVO>> getShortagePage(@Valid ShortagePageReqVO pageReqVO) {
        return success(shortageService.getShortagePage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出APS-排程缺料清单 Excel")
    @PreAuthorize("@ss.hasPermission('aps:shortage:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportShortageExcel(@Valid ShortagePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ShortageRespVO> list = shortageService.getShortagePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "APS-排程缺料清单.xls", "数据", ShortageRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入APS-排程缺料清单模板")
    @PreAuthorize("@ss.hasPermission('aps:shortage:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "APS-排程缺料清单导入模板.xls", "数据",
                ShortageImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入APS-排程缺料清单")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('aps:shortage:import')")
    public CommonResult<ShortageImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ShortageImportExcelVO> list = ExcelUtils.read(file, ShortageImportExcelVO.class);
        return success(shortageService.importShortageList(list));
    }

}