package com.iboot.pf.module.mes.controller.admin.materialreq;

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

import com.iboot.pf.module.mes.controller.admin.materialreq.vo.*;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.service.materialreq.MaterialReqService;

@Tag(name = "管理后台 - MES-领退补料申请")
@RestController
@RequestMapping("/mes/material-req")
@Validated
public class MaterialReqController {

    @Resource
    private MaterialReqService materialReqService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-领退补料申请")
    @PreAuthorize("@ss.hasPermission('mes:material-req:create')")
    public CommonResult<Long> createMaterialReq(@Valid @RequestBody MaterialReqSaveReqVO createReqVO) {
        return success(materialReqService.createMaterialReq(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-领退补料申请")
    @PreAuthorize("@ss.hasPermission('mes:material-req:update')")
    public CommonResult<Boolean> updateMaterialReq(@Valid @RequestBody MaterialReqSaveReqVO updateReqVO) {
        materialReqService.updateMaterialReq(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-领退补料申请")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:material-req:delete')")
    public CommonResult<Boolean> deleteMaterialReq(@RequestParam("id") Long id) {
        materialReqService.deleteMaterialReq(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-领退补料申请")
                @PreAuthorize("@ss.hasPermission('mes:material-req:delete')")
    public CommonResult<Boolean> deleteMaterialReqList(@RequestParam("ids") List<Long> ids) {
        materialReqService.deleteMaterialReqListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-领退补料申请")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:material-req:query')")
    public CommonResult<MaterialReqRespVO> getMaterialReq(@RequestParam("id") Long id) {
        return success(materialReqService.getMaterialReq(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-领退补料申请分页")
    @PreAuthorize("@ss.hasPermission('mes:material-req:query')")
    public CommonResult<PageResult<MaterialReqRespVO>> getMaterialReqPage(@Valid MaterialReqPageReqVO pageReqVO) {
        return success(materialReqService.getMaterialReqPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-领退补料申请 Excel")
    @PreAuthorize("@ss.hasPermission('mes:material-req:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportMaterialReqExcel(@Valid MaterialReqPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MaterialReqRespVO> list = materialReqService.getMaterialReqPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-领退补料申请.xls", "数据", MaterialReqRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-领退补料申请模板")
    @PreAuthorize("@ss.hasPermission('mes:material-req:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-领退补料申请导入模板.xls", "数据",
                MaterialReqImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-领退补料申请")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:material-req:import')")
    public CommonResult<MaterialReqImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<MaterialReqImportExcelVO> list = ExcelUtils.read(file, MaterialReqImportExcelVO.class);
        return success(materialReqService.importMaterialReqList(list));
    }

    // ==================== 子表（MES-领退补料申请明细） ====================

    @GetMapping("/material-req-line/page")
    @Operation(summary = "获得MES-领退补料申请明细分页")
    @Parameter(name = "reqId", description = "领退补料申请编号")
    @PreAuthorize("@ss.hasPermission('mes:material-req:query')")
    public CommonResult<PageResult<MaterialReqLineDO>> getMaterialReqLinePage(PageParam pageReqVO,
                                                                                        @RequestParam("reqId") Long reqId) {
        return success(materialReqService.getMaterialReqLinePage(pageReqVO, reqId));
    }

    @PostMapping("/material-req-line/create")
    @Operation(summary = "创建MES-领退补料申请明细")
    @PreAuthorize("@ss.hasPermission('mes:material-req:create')")
    public CommonResult<Long> createMaterialReqLine(@Valid @RequestBody MaterialReqLineDO materialReqLine) {
        return success(materialReqService.createMaterialReqLine(materialReqLine));
    }

    @PutMapping("/material-req-line/update")
    @Operation(summary = "更新MES-领退补料申请明细")
    @PreAuthorize("@ss.hasPermission('mes:material-req:update')")
    public CommonResult<Boolean> updateMaterialReqLine(@Valid @RequestBody MaterialReqLineDO materialReqLine) {
        materialReqService.updateMaterialReqLine(materialReqLine);
        return success(true);
    }

    @DeleteMapping("/material-req-line/delete")
    @Parameter(name = "id", description = "编号", required = true)
    @Operation(summary = "删除MES-领退补料申请明细")
    @PreAuthorize("@ss.hasPermission('mes:material-req:delete')")
    public CommonResult<Boolean> deleteMaterialReqLine(@RequestParam("id") Long id) {
        materialReqService.deleteMaterialReqLine(id);
        return success(true);
    }

    @DeleteMapping("/material-req-line/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-领退补料申请明细")
    @PreAuthorize("@ss.hasPermission('mes:material-req:delete')")
    public CommonResult<Boolean> deleteMaterialReqLineList(@RequestParam("ids") List<Long> ids) {
        materialReqService.deleteMaterialReqLineListByIds(ids);
        return success(true);
    }

	@GetMapping("/material-req-line/get")
	@Operation(summary = "获得MES-领退补料申请明细")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:material-req:query')")
	public CommonResult<MaterialReqLineDO> getMaterialReqLine(@RequestParam("id") Long id) {
	    return success(materialReqService.getMaterialReqLine(id));
	}

}