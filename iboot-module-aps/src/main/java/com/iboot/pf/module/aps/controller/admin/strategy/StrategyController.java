package com.iboot.pf.module.aps.controller.admin.strategy;

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

import com.iboot.pf.module.aps.controller.admin.strategy.vo.*;
import com.iboot.pf.module.aps.service.strategy.ApsStrategyService;

@Tag(name = "管理后台 - APS-排程策略")
@RestController("apsStrategyController")
@RequestMapping("/aps/strategy")
@Validated
public class StrategyController {

    @Resource
    private ApsStrategyService apsStrategyService;

    @PostMapping("/create")
    @Operation(summary = "创建APS-排程策略")
    @PreAuthorize("@ss.hasPermission('aps:strategy:create')")
    public CommonResult<Long> createStrategy(@Valid @RequestBody StrategySaveReqVO createReqVO) {
        return success(apsStrategyService.createStrategy(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新APS-排程策略")
    @PreAuthorize("@ss.hasPermission('aps:strategy:update')")
    public CommonResult<Boolean> updateStrategy(@Valid @RequestBody StrategySaveReqVO updateReqVO) {
        apsStrategyService.updateStrategy(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除APS-排程策略")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('aps:strategy:delete')")
    public CommonResult<Boolean> deleteStrategy(@RequestParam("id") Long id) {
        apsStrategyService.deleteStrategy(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除APS-排程策略")
                @PreAuthorize("@ss.hasPermission('aps:strategy:delete')")
    public CommonResult<Boolean> deleteStrategyList(@RequestParam("ids") List<Long> ids) {
        apsStrategyService.deleteStrategyListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得APS-排程策略")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('aps:strategy:query')")
    public CommonResult<StrategyRespVO> getStrategy(@RequestParam("id") Long id) {
        return success(apsStrategyService.getStrategy(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得APS-排程策略分页")
    @PreAuthorize("@ss.hasPermission('aps:strategy:query')")
    public CommonResult<PageResult<StrategyRespVO>> getStrategyPage(@Valid StrategyPageReqVO pageReqVO) {
        return success(apsStrategyService.getStrategyPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出APS-排程策略 Excel")
    @PreAuthorize("@ss.hasPermission('aps:strategy:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStrategyExcel(@Valid StrategyPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<StrategyRespVO> list = apsStrategyService.getStrategyPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "APS-排程策略.xls", "数据", StrategyRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入APS-排程策略模板")
    @PreAuthorize("@ss.hasPermission('aps:strategy:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "APS-排程策略导入模板.xls", "数据",
                StrategyImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入APS-排程策略")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('aps:strategy:import')")
    public CommonResult<StrategyImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<StrategyImportExcelVO> list = ExcelUtils.read(file, StrategyImportExcelVO.class);
        return success(apsStrategyService.importStrategyList(list));
    }

}
