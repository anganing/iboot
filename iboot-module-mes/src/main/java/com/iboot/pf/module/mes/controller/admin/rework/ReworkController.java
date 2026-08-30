package com.iboot.pf.module.mes.controller.admin.rework;

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

import com.iboot.pf.module.mes.controller.admin.rework.vo.*;
import com.iboot.pf.module.mes.service.rework.ReworkService;

@Tag(name = "管理后台 - MES-返工任务")
@RestController
@RequestMapping("/mes/rework")
@Validated
public class ReworkController {

    @Resource
    private ReworkService reworkService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-返工任务")
    @PreAuthorize("@ss.hasPermission('mes:rework:create')")
    public CommonResult<Long> createRework(@Valid @RequestBody ReworkSaveReqVO createReqVO) {
        return success(reworkService.createRework(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-返工任务")
    @PreAuthorize("@ss.hasPermission('mes:rework:update')")
    public CommonResult<Boolean> updateRework(@Valid @RequestBody ReworkSaveReqVO updateReqVO) {
        reworkService.updateRework(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-返工任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:rework:delete')")
    public CommonResult<Boolean> deleteRework(@RequestParam("id") Long id) {
        reworkService.deleteRework(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-返工任务")
                @PreAuthorize("@ss.hasPermission('mes:rework:delete')")
    public CommonResult<Boolean> deleteReworkList(@RequestParam("ids") List<Long> ids) {
        reworkService.deleteReworkListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-返工任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:rework:query')")
    public CommonResult<ReworkRespVO> getRework(@RequestParam("id") Long id) {
        return success(reworkService.getRework(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-返工任务分页")
    @PreAuthorize("@ss.hasPermission('mes:rework:query')")
    public CommonResult<PageResult<ReworkRespVO>> getReworkPage(@Valid ReworkPageReqVO pageReqVO) {
        return success(reworkService.getReworkPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-返工任务 Excel")
    @PreAuthorize("@ss.hasPermission('mes:rework:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportReworkExcel(@Valid ReworkPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ReworkRespVO> list = reworkService.getReworkPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-返工任务.xls", "数据", ReworkRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-返工任务模板")
    @PreAuthorize("@ss.hasPermission('mes:rework:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-返工任务导入模板.xls", "数据",
                ReworkImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-返工任务")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:rework:import')")
    public CommonResult<ReworkImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ReworkImportExcelVO> list = ExcelUtils.read(file, ReworkImportExcelVO.class);
        return success(reworkService.importReworkList(list));
    }

}