package com.iboot.pf.module.aps.controller.admin.load;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

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

import com.iboot.pf.module.aps.controller.admin.load.vo.*;
import com.iboot.pf.module.aps.service.load.ApsLoadService;

@Tag(name = "管理后台 - 资源负荷")
@RestController
@RequestMapping("/aps/load")
@Validated
public class LoadController {

    @Resource
    private ApsLoadService apsLoadService;

    @PostMapping("/create")
    @Operation(summary = "创建资源负荷")
    @PreAuthorize("@ss.hasPermission('aps:load:create')")
    public CommonResult<Long> createLoad(@Valid @RequestBody LoadSaveReqVO createReqVO) {
        return success(apsLoadService.createLoad(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新资源负荷")
    @PreAuthorize("@ss.hasPermission('aps:load:update')")
    public CommonResult<Boolean> updateLoad(@Valid @RequestBody LoadSaveReqVO updateReqVO) {
        apsLoadService.updateLoad(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除资源负荷")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('aps:load:delete')")
    public CommonResult<Boolean> deleteLoad(@RequestParam("id") Long id) {
        apsLoadService.deleteLoad(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除资源负荷")
                @PreAuthorize("@ss.hasPermission('aps:load:delete')")
    public CommonResult<Boolean> deleteLoadList(@RequestParam("ids") List<Long> ids) {
        apsLoadService.deleteLoadListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得资源负荷")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('aps:load:query')")
    public CommonResult<LoadRespVO> getLoad(@RequestParam("id") Long id) {
        return success(apsLoadService.getLoad(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得资源负荷分页")
    @PreAuthorize("@ss.hasPermission('aps:load:query')")
    public CommonResult<PageResult<LoadRespVO>> getLoadPage(@Valid LoadPageReqVO pageReqVO) {
        return success(apsLoadService.getLoadPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出资源负荷 Excel")
    @PreAuthorize("@ss.hasPermission('aps:load:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportLoadExcel(@Valid LoadPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<LoadRespVO> list = apsLoadService.getLoadPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "资源负荷.xls", "数据", LoadRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入资源负荷模板")
    @PreAuthorize("@ss.hasPermission('aps:load:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "资源负荷导入模板.xls", "数据",
                LoadImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入资源负荷")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('aps:load:import')")
    public CommonResult<LoadImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<LoadImportExcelVO> list = ExcelUtils.read(file, LoadImportExcelVO.class);
        return success(apsLoadService.importLoadList(list));
    }

}