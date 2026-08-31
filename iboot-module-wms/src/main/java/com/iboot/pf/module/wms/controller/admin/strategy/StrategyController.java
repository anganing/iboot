package com.iboot.pf.module.wms.controller.admin.strategy;

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

import com.iboot.pf.module.wms.controller.admin.strategy.vo.*;
import com.iboot.pf.module.wms.service.strategy.WmsStrategyService;

@Tag(name = "管理后台 - WMS-仓储策略")
@RestController("wmsStrategyController")
@RequestMapping("/wms/strategy")
@Validated
public class StrategyController {

    @Resource
    private WmsStrategyService wmsStrategyService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-仓储策略")
    @PreAuthorize("@ss.hasPermission('wms:strategy:create')")
    public CommonResult<Long> createStrategy(@Valid @RequestBody StrategySaveReqVO createReqVO) {
        return success(wmsStrategyService.createStrategy(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-仓储策略")
    @PreAuthorize("@ss.hasPermission('wms:strategy:update')")
    public CommonResult<Boolean> updateStrategy(@Valid @RequestBody StrategySaveReqVO updateReqVO) {
        wmsStrategyService.updateStrategy(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-仓储策略")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:strategy:delete')")
    public CommonResult<Boolean> deleteStrategy(@RequestParam("id") Long id) {
        wmsStrategyService.deleteStrategy(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-仓储策略")
                @PreAuthorize("@ss.hasPermission('wms:strategy:delete')")
    public CommonResult<Boolean> deleteStrategyList(@RequestParam("ids") List<Long> ids) {
        wmsStrategyService.deleteStrategyListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-仓储策略")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:strategy:query')")
    public CommonResult<StrategyRespVO> getStrategy(@RequestParam("id") Long id) {
        return success(wmsStrategyService.getStrategy(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-仓储策略分页")
    @PreAuthorize("@ss.hasPermission('wms:strategy:query')")
    public CommonResult<PageResult<StrategyRespVO>> getStrategyPage(@Valid StrategyPageReqVO pageReqVO) {
        return success(wmsStrategyService.getStrategyPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-仓储策略 Excel")
    @PreAuthorize("@ss.hasPermission('wms:strategy:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStrategyExcel(@Valid StrategyPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<StrategyRespVO> list = wmsStrategyService.getStrategyPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-仓储策略.xls", "数据", StrategyRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-仓储策略模板")
    @PreAuthorize("@ss.hasPermission('wms:strategy:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-仓储策略导入模板.xls", "数据",
                StrategyImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-仓储策略")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:strategy:import')")
    public CommonResult<StrategyImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<StrategyImportExcelVO> list = ExcelUtils.read(file, StrategyImportExcelVO.class);
        return success(wmsStrategyService.importStrategyList(list));
    }

}
