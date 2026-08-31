package com.iboot.pf.module.wms.controller.admin.invtxn;

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

import com.iboot.pf.module.wms.controller.admin.invtxn.vo.*;
import com.iboot.pf.module.wms.service.invtxn.InvTxnService;

/**
 * WMS-库存流水只提供查询与导出。
 *
 * <p>流水是过账留痕，由 {@code WmsInventoryService#post} 一次写入后不可变更：
 * 放开人工增删改等于允许篡改账实对照的证据链。
 */
@Tag(name = "管理后台 - WMS-库存流水")
@RestController
@RequestMapping("/wms/inv-txn")
@Validated
public class InvTxnController {

    @Resource
    private InvTxnService invTxnService;

    @GetMapping("/get")
    @Operation(summary = "获得WMS-库存流水")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:inv-txn:query')")
    public CommonResult<InvTxnRespVO> getInvTxn(@RequestParam("id") Long id) {
        return success(invTxnService.getInvTxn(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-库存流水分页")
    @PreAuthorize("@ss.hasPermission('wms:inv-txn:query')")
    public CommonResult<PageResult<InvTxnRespVO>> getInvTxnPage(@Valid InvTxnPageReqVO pageReqVO) {
        return success(invTxnService.getInvTxnPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-库存流水 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inv-txn:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInvTxnExcel(@Valid InvTxnPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<InvTxnRespVO> list = invTxnService.getInvTxnPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-库存流水.xls", "数据", InvTxnRespVO.class,
                        list);
    }

}