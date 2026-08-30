package com.iboot.pf.module.mes.controller.admin.defect;

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

import com.iboot.pf.module.mes.controller.admin.defect.vo.*;
import com.iboot.pf.module.mes.service.defect.DefectService;

@Tag(name = "管理后台 - MES-缺陷记录")
@RestController
@RequestMapping("/mes/defect")
@Validated
public class DefectController {

    @Resource
    private DefectService defectService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-缺陷记录")
    @PreAuthorize("@ss.hasPermission('mes:defect:create')")
    public CommonResult<Long> createDefect(@Valid @RequestBody DefectSaveReqVO createReqVO) {
        return success(defectService.createDefect(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-缺陷记录")
    @PreAuthorize("@ss.hasPermission('mes:defect:update')")
    public CommonResult<Boolean> updateDefect(@Valid @RequestBody DefectSaveReqVO updateReqVO) {
        defectService.updateDefect(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-缺陷记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:defect:delete')")
    public CommonResult<Boolean> deleteDefect(@RequestParam("id") Long id) {
        defectService.deleteDefect(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-缺陷记录")
                @PreAuthorize("@ss.hasPermission('mes:defect:delete')")
    public CommonResult<Boolean> deleteDefectList(@RequestParam("ids") List<Long> ids) {
        defectService.deleteDefectListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-缺陷记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:defect:query')")
    public CommonResult<DefectRespVO> getDefect(@RequestParam("id") Long id) {
        return success(defectService.getDefect(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-缺陷记录分页")
    @PreAuthorize("@ss.hasPermission('mes:defect:query')")
    public CommonResult<PageResult<DefectRespVO>> getDefectPage(@Valid DefectPageReqVO pageReqVO) {
        return success(defectService.getDefectPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-缺陷记录 Excel")
    @PreAuthorize("@ss.hasPermission('mes:defect:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDefectExcel(@Valid DefectPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DefectRespVO> list = defectService.getDefectPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-缺陷记录.xls", "数据", DefectRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-缺陷记录模板")
    @PreAuthorize("@ss.hasPermission('mes:defect:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-缺陷记录导入模板.xls", "数据",
                DefectImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-缺陷记录")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:defect:import')")
    public CommonResult<DefectImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<DefectImportExcelVO> list = ExcelUtils.read(file, DefectImportExcelVO.class);
        return success(defectService.importDefectList(list));
    }

}