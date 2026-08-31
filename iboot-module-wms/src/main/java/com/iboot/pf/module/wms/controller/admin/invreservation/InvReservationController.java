package com.iboot.pf.module.wms.controller.admin.invreservation;

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

import com.iboot.pf.module.wms.controller.admin.invreservation.vo.*;
import com.iboot.pf.module.wms.service.invreservation.InvReservationService;

@Tag(name = "管理后台 - WMS-库存预留")
@RestController
@RequestMapping("/wms/inv-reservation")
@Validated
public class InvReservationController {

    @Resource
    private InvReservationService invReservationService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-库存预留")
    @PreAuthorize("@ss.hasPermission('wms:inv-reservation:create')")
    public CommonResult<Long> createInvReservation(@Valid @RequestBody InvReservationSaveReqVO createReqVO) {
        return success(invReservationService.createInvReservation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-库存预留")
    @PreAuthorize("@ss.hasPermission('wms:inv-reservation:update')")
    public CommonResult<Boolean> updateInvReservation(@Valid @RequestBody InvReservationSaveReqVO updateReqVO) {
        invReservationService.updateInvReservation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-库存预留")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inv-reservation:delete')")
    public CommonResult<Boolean> deleteInvReservation(@RequestParam("id") Long id) {
        invReservationService.deleteInvReservation(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-库存预留")
                @PreAuthorize("@ss.hasPermission('wms:inv-reservation:delete')")
    public CommonResult<Boolean> deleteInvReservationList(@RequestParam("ids") List<Long> ids) {
        invReservationService.deleteInvReservationListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-库存预留")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:inv-reservation:query')")
    public CommonResult<InvReservationRespVO> getInvReservation(@RequestParam("id") Long id) {
        return success(invReservationService.getInvReservation(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-库存预留分页")
    @PreAuthorize("@ss.hasPermission('wms:inv-reservation:query')")
    public CommonResult<PageResult<InvReservationRespVO>> getInvReservationPage(@Valid InvReservationPageReqVO pageReqVO) {
        return success(invReservationService.getInvReservationPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-库存预留 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inv-reservation:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInvReservationExcel(@Valid InvReservationPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<InvReservationRespVO> list = invReservationService.getInvReservationPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-库存预留.xls", "数据", InvReservationRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-库存预留模板")
    @PreAuthorize("@ss.hasPermission('wms:inv-reservation:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-库存预留导入模板.xls", "数据",
                InvReservationImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-库存预留")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inv-reservation:import')")
    public CommonResult<InvReservationImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<InvReservationImportExcelVO> list = ExcelUtils.read(file, InvReservationImportExcelVO.class);
        return success(invReservationService.importInvReservationList(list));
    }

}