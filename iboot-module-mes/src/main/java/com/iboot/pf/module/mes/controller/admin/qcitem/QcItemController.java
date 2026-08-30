package com.iboot.pf.module.mes.controller.admin.qcitem;

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

import com.iboot.pf.module.mes.controller.admin.qcitem.vo.*;
import com.iboot.pf.module.mes.service.qcitem.QcItemService;

@Tag(name = "管理后台 - MES-检验项目")
@RestController
@RequestMapping("/mes/qc-item")
@Validated
public class QcItemController {

    @Resource
    private QcItemService qcItemService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-检验项目")
    @PreAuthorize("@ss.hasPermission('mes:qc-item:create')")
    public CommonResult<Long> createQcItem(@Valid @RequestBody QcItemSaveReqVO createReqVO) {
        return success(qcItemService.createQcItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-检验项目")
    @PreAuthorize("@ss.hasPermission('mes:qc-item:update')")
    public CommonResult<Boolean> updateQcItem(@Valid @RequestBody QcItemSaveReqVO updateReqVO) {
        qcItemService.updateQcItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-检验项目")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-item:delete')")
    public CommonResult<Boolean> deleteQcItem(@RequestParam("id") Long id) {
        qcItemService.deleteQcItem(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-检验项目")
                @PreAuthorize("@ss.hasPermission('mes:qc-item:delete')")
    public CommonResult<Boolean> deleteQcItemList(@RequestParam("ids") List<Long> ids) {
        qcItemService.deleteQcItemListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-检验项目")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:qc-item:query')")
    public CommonResult<QcItemRespVO> getQcItem(@RequestParam("id") Long id) {
        return success(qcItemService.getQcItem(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-检验项目分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-item:query')")
    public CommonResult<PageResult<QcItemRespVO>> getQcItemPage(@Valid QcItemPageReqVO pageReqVO) {
        return success(qcItemService.getQcItemPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-检验项目 Excel")
    @PreAuthorize("@ss.hasPermission('mes:qc-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportQcItemExcel(@Valid QcItemPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<QcItemRespVO> list = qcItemService.getQcItemPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-检验项目.xls", "数据", QcItemRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-检验项目模板")
    @PreAuthorize("@ss.hasPermission('mes:qc-item:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-检验项目导入模板.xls", "数据",
                QcItemImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-检验项目")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-item:import')")
    public CommonResult<QcItemImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<QcItemImportExcelVO> list = ExcelUtils.read(file, QcItemImportExcelVO.class);
        return success(qcItemService.importQcItemList(list));
    }

}