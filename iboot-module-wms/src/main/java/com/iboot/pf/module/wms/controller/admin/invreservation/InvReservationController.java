package com.iboot.pf.module.wms.controller.admin.invreservation;

import org.springframework.web.bind.annotation.*;
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
import com.iboot.pf.module.wms.enums.WmsCommandTypeEnum;
import com.iboot.pf.module.wms.enums.WmsSourceModuleEnum;
import com.iboot.pf.module.wms.service.invreservation.InvReservationService;
import com.iboot.pf.module.wms.service.reservation.WmsInventoryReservationService;
import com.iboot.pf.module.wms.service.reservation.bo.WmsReservationHandleBO;

/**
 * WMS-库存预留只提供查询、导出与释放动作。
 *
 * <p>预留的创建方永远是业务动作（MES 领料提交、销售订单确认），不是人在界面上点：
 * 人工插一条预留记录并不会改 {@code wms_inv_balance.qty_reserved}，可用量因此虚高、超发无人拦，
 * 所以新增/修改/删除/导入端点一并撤除。释放是这个页面唯一正当的写入动作，转调领域 Service 完成
 * {@code RELEASE} 过账，把占用量还回可用量。
 */
@Tag(name = "管理后台 - WMS-库存预留")
@RestController
@RequestMapping("/wms/inv-reservation")
@Validated
public class InvReservationController {

    @Resource
    private InvReservationService invReservationService;
    @Resource
    private WmsInventoryReservationService inventoryReservationService;

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

    @PostMapping("/release")
    @Operation(summary = "释放WMS-库存预留")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:inv-reservation:release')")
    public CommonResult<Boolean> releaseInvReservation(@RequestParam("id") Long id) {
        WmsReservationHandleBO bo = new WmsReservationHandleBO();
        bo.setReservationId(id);
        // 幂等键四段格式：来源模块:来源单号:动作:来源行号。人工释放的来源对象就是预留记录本身，
        // 用主键充当单号段，省掉一次仅为拼键的查询；qty 留空表示释放全部剩余量，
        // 一条预留只会被人工释放到底一次，所以这个键天然稳定，连点两次由过账键判重挡住。
        bo.setIdempotencyKey(WmsSourceModuleEnum.UI.name() + ":" + id + ":"
                + WmsCommandTypeEnum.RELEASE_RESERVATION.name() + ":0");
        inventoryReservationService.release(bo);
        return success(true);
    }

}
