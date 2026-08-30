package com.iboot.pf.module.aps.controller.admin.scenario;

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

import com.iboot.pf.module.aps.controller.admin.scenario.vo.*;
import com.iboot.pf.module.aps.service.scenario.ScenarioService;

@Tag(name = "管理后台 - APS-排程场景")
@RestController
@RequestMapping("/aps/scenario")
@Validated
public class ScenarioController {

    @Resource
    private ScenarioService scenarioService;

    @PostMapping("/create")
    @Operation(summary = "创建APS-排程场景")
    @PreAuthorize("@ss.hasPermission('aps:scenario:create')")
    public CommonResult<Long> createScenario(@Valid @RequestBody ScenarioSaveReqVO createReqVO) {
        return success(scenarioService.createScenario(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新APS-排程场景")
    @PreAuthorize("@ss.hasPermission('aps:scenario:update')")
    public CommonResult<Boolean> updateScenario(@Valid @RequestBody ScenarioSaveReqVO updateReqVO) {
        scenarioService.updateScenario(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除APS-排程场景")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('aps:scenario:delete')")
    public CommonResult<Boolean> deleteScenario(@RequestParam("id") Long id) {
        scenarioService.deleteScenario(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除APS-排程场景")
                @PreAuthorize("@ss.hasPermission('aps:scenario:delete')")
    public CommonResult<Boolean> deleteScenarioList(@RequestParam("ids") List<Long> ids) {
        scenarioService.deleteScenarioListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得APS-排程场景")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('aps:scenario:query')")
    public CommonResult<ScenarioRespVO> getScenario(@RequestParam("id") Long id) {
        return success(scenarioService.getScenario(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得APS-排程场景分页")
    @PreAuthorize("@ss.hasPermission('aps:scenario:query')")
    public CommonResult<PageResult<ScenarioRespVO>> getScenarioPage(@Valid ScenarioPageReqVO pageReqVO) {
        return success(scenarioService.getScenarioPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出APS-排程场景 Excel")
    @PreAuthorize("@ss.hasPermission('aps:scenario:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportScenarioExcel(@Valid ScenarioPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ScenarioRespVO> list = scenarioService.getScenarioPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "APS-排程场景.xls", "数据", ScenarioRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入APS-排程场景模板")
    @PreAuthorize("@ss.hasPermission('aps:scenario:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "APS-排程场景导入模板.xls", "数据",
                ScenarioImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入APS-排程场景")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('aps:scenario:import')")
    public CommonResult<ScenarioImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ScenarioImportExcelVO> list = ExcelUtils.read(file, ScenarioImportExcelVO.class);
        return success(scenarioService.importScenarioList(list));
    }

}