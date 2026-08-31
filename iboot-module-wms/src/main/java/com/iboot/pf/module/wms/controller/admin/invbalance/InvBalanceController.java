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

/**
 * WMS-多维库存余额只提供查询与导出。
 *
 * <p>余额是账表，只能由 {@code WmsInventoryService#post} 过账修改：
 * 放开人工新增/修改/删除会让账实不符，且绕过乐观锁与流水留痕。
 */
@Tag(name = "管理后台 - WMS-多维库存余额")
@RestController
@RequestMapping("/wms/inv-balance")
@Validated
public class InvBalanceController {

    @Resource
    private InvBalanceService invBalanceService;

    @GetMapping("/get")
    @Operation(summary = "获得WMS-多维库存余额")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
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

}
