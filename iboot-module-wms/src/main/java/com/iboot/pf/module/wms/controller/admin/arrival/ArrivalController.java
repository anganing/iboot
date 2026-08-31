package com.iboot.pf.module.wms.controller.admin.arrival;

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

import com.iboot.pf.module.wms.controller.admin.arrival.vo.*;
import com.iboot.pf.module.wms.service.arrival.ArrivalService;

@Tag(name = "管理后台 - WMS-到货登记")
@RestController
@RequestMapping("/wms/arrival")
@Validated
public class ArrivalController {

    @Resource
    private ArrivalService arrivalService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-到货登记")
    @PreAuthorize("@ss.hasPermission('wms:arrival:create')")
    public CommonResult<Long> createArrival(@Valid @RequestBody ArrivalSaveReqVO createReqVO) {
        return success(arrivalService.createArrival(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-到货登记")
    @PreAuthorize("@ss.hasPermission('wms:arrival:update')")
    public CommonResult<Boolean> updateArrival(@Valid @RequestBody ArrivalSaveReqVO updateReqVO) {
        arrivalService.updateArrival(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-到货登记")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:arrival:delete')")
    public CommonResult<Boolean> deleteArrival(@RequestParam("id") Long id) {
        arrivalService.deleteArrival(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-到货登记")
                @PreAuthorize("@ss.hasPermission('wms:arrival:delete')")
    public CommonResult<Boolean> deleteArrivalList(@RequestParam("ids") List<Long> ids) {
        arrivalService.deleteArrivalListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-到货登记")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:arrival:query')")
    public CommonResult<ArrivalRespVO> getArrival(@RequestParam("id") Long id) {
        return success(arrivalService.getArrival(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-到货登记分页")
    @PreAuthorize("@ss.hasPermission('wms:arrival:query')")
    public CommonResult<PageResult<ArrivalRespVO>> getArrivalPage(@Valid ArrivalPageReqVO pageReqVO) {
        return success(arrivalService.getArrivalPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-到货登记 Excel")
    @PreAuthorize("@ss.hasPermission('wms:arrival:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportArrivalExcel(@Valid ArrivalPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ArrivalRespVO> list = arrivalService.getArrivalPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-到货登记.xls", "数据", ArrivalRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-到货登记模板")
    @PreAuthorize("@ss.hasPermission('wms:arrival:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-到货登记导入模板.xls", "数据",
                ArrivalImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-到货登记")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:arrival:import')")
    public CommonResult<ArrivalImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ArrivalImportExcelVO> list = ExcelUtils.read(file, ArrivalImportExcelVO.class);
        return success(arrivalService.importArrivalList(list));
    }

    // ==================== 子表（WMS-到货登记明细） ====================

    @GetMapping("/arrival-line/page")
    @Operation(summary = "获得WMS-到货登记明细分页")
    @PreAuthorize("@ss.hasPermission('wms:arrival:query')")
    public CommonResult<PageResult<ArrivalLineRespVO>> getArrivalLinePage(@Valid ArrivalLinePageReqVO pageReqVO) {
        return success(arrivalService.getArrivalLinePage(pageReqVO));
    }

    @PostMapping("/arrival-line/create")
    @Operation(summary = "创建WMS-到货登记明细")
    @PreAuthorize("@ss.hasPermission('wms:arrival:create')")
    public CommonResult<Long> createArrivalLine(@Valid @RequestBody ArrivalLineSaveReqVO createReqVO) {
        return success(arrivalService.createArrivalLine(createReqVO));
    }

    @PutMapping("/arrival-line/update")
    @Operation(summary = "更新WMS-到货登记明细")
    @PreAuthorize("@ss.hasPermission('wms:arrival:update')")
    public CommonResult<Boolean> updateArrivalLine(@Valid @RequestBody ArrivalLineSaveReqVO updateReqVO) {
        arrivalService.updateArrivalLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/arrival-line/delete")
    @Parameter(name = "id", description = "编号", required = true)
    @Operation(summary = "删除WMS-到货登记明细")
    @PreAuthorize("@ss.hasPermission('wms:arrival:delete')")
    public CommonResult<Boolean> deleteArrivalLine(@RequestParam("id") Long id) {
        arrivalService.deleteArrivalLine(id);
        return success(true);
    }

    @DeleteMapping("/arrival-line/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-到货登记明细")
    @PreAuthorize("@ss.hasPermission('wms:arrival:delete')")
    public CommonResult<Boolean> deleteArrivalLineList(@RequestParam("ids") List<Long> ids) {
        arrivalService.deleteArrivalLineListByIds(ids);
        return success(true);
    }

	@GetMapping("/arrival-line/get")
	@Operation(summary = "获得WMS-到货登记明细")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:arrival:query')")
	public CommonResult<ArrivalLineRespVO> getArrivalLine(@RequestParam("id") Long id) {
	    return success(arrivalService.getArrivalLine(id));
	}

}
