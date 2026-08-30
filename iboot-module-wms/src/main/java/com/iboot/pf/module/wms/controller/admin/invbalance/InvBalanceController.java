package com.iboot.pf.module.wms.controller.admin.invbalance;

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

import com.iboot.pf.module.wms.controller.admin.invbalance.vo.*;
import com.iboot.pf.module.wms.service.invbalance.InvBalanceService;

@Tag(name = "管理后台 - WMS-多维库存余额")
@RestController
@RequestMapping("/wms/inv-balance")
@Validated
public class InvBalanceController {

    @Resource
    private InvBalanceService invBalanceService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-多维库存余额")
    @PreAuthorize("@ss.hasPermission('wms:inv-balance:create')")
    public CommonResult<Long> createInvBalance(@Valid @RequestBody InvBalanceSaveReqVO createReqVO) {
        return success(invBalanceService.createInvBalance(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-多维库存余额")
    @PreAuthorize("@ss.hasPermission('wms:inv-balance:update')")
    public CommonResult<Boolean> updateInvBalance(@Valid @RequestBody InvBalanceSaveReqVO updateReqVO) {
        invBalanceService.updateInvBalance(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-多维库存余额")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inv-balance:delete')")
    public CommonResult<Boolean> deleteInvBalance(@RequestParam("id") Long id) {
        invBalanceService.deleteInvBalance(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-多维库存余额")
                @PreAuthorize("@ss.hasPermission('wms:inv-balance:delete')")
    public CommonResult<Boolean> deleteInvBalanceList(@RequestParam("ids") List<Long> ids) {
        invBalanceService.deleteInvBalanceListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-多维库存余额")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inv-balance:query')")
    public CommonResult<InvBalanceRespVO> getInvBalance(@RequestParam("id") Long id) {
        return success(invBalanceService.getInvBalance(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-多维库存余额分页")
    @PreAuthorize("@ss.hasPermission('wms:inv-balance:query')")
    public CommonResult<PageResult<InvBalanceRespVO>> getInvBalancePage(@Valid InvBalancePageReqVO pageReqVO) {
        return success(invBalanceService.getInvBalancePage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-多维库存余额 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inv-balance:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInvBalanceExcel(@Valid InvBalancePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<InvBalanceRespVO> list = invBalanceService.getInvBalancePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-多维库存余额.xls", "数据", InvBalanceRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-多维库存余额模板")
    @PreAuthorize("@ss.hasPermission('wms:inv-balance:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-多维库存余额导入模板.xls", "数据",
                InvBalanceImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-多维库存余额")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inv-balance:import')")
    public CommonResult<InvBalanceImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<InvBalanceImportExcelVO> list = ExcelUtils.read(file, InvBalanceImportExcelVO.class);
        return success(invBalanceService.importInvBalanceList(list));
    }

}