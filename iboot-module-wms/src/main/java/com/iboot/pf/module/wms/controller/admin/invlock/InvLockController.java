package com.iboot.pf.module.wms.controller.admin.invlock;

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

import com.iboot.pf.module.wms.controller.admin.invlock.vo.*;
import com.iboot.pf.module.wms.enums.WmsCommandTypeEnum;
import com.iboot.pf.module.wms.enums.WmsSourceModuleEnum;
import com.iboot.pf.module.wms.service.invlock.InvLockService;
import com.iboot.pf.module.wms.service.lock.WmsInventoryLockService;
import com.iboot.pf.module.wms.service.lock.bo.WmsUnlockBO;

/**
 * WMS-库存冻结记录只提供查询、导出与解冻动作。
 *
 * <p>冻结由业务动作创建（盘点冻结范围、质量冻结、待处置），人工插一条冻结记录并不会改
 * {@code wms_inv_balance.qty_frozen}，该动不了的货照样能被领走，所以新增/修改/删除/导入端点一并撤除。
 * 解冻是这个页面唯一正当的写入动作，转调领域 Service 完成 {@code UNFREEZE} 过账。
 */
@Tag(name = "管理后台 - WMS-库存冻结记录")
@RestController
@RequestMapping("/wms/inv-lock")
@Validated
public class InvLockController {

    @Resource
    private InvLockService invLockService;
    @Resource
    private WmsInventoryLockService inventoryLockService;

    @GetMapping("/get")
    @Operation(summary = "获得WMS-库存冻结记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:inv-lock:query')")
    public CommonResult<InvLockRespVO> getInvLock(@RequestParam("id") Long id) {
        return success(invLockService.getInvLock(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-库存冻结记录分页")
    @PreAuthorize("@ss.hasPermission('wms:inv-lock:query')")
    public CommonResult<PageResult<InvLockRespVO>> getInvLockPage(@Valid InvLockPageReqVO pageReqVO) {
        return success(invLockService.getInvLockPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-库存冻结记录 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inv-lock:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInvLockExcel(@Valid InvLockPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<InvLockRespVO> list = invLockService.getInvLockPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-库存冻结记录.xls", "数据", InvLockRespVO.class,
                        list);
    }

    @PostMapping("/unlock")
    @Operation(summary = "解冻WMS-库存冻结记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:inv-lock:unlock')")
    public CommonResult<Boolean> unlockInvLock(@RequestParam("id") Long id) {
        WmsUnlockBO bo = new WmsUnlockBO();
        bo.setLockId(id);
        // 幂等键四段格式：来源模块:来源单号:动作:来源行号。人工解冻的来源对象就是冻结记录本身，
        // 用主键充当单号段，省掉一次仅为拼键的查询；qty 留空表示解冻全部剩余量，
        // 一条冻结只会被人工解冻到底一次，所以这个键天然稳定，连点两次由过账键判重挡住。
        bo.setIdempotencyKey(WmsSourceModuleEnum.UI.name() + ":" + id + ":"
                + WmsCommandTypeEnum.UNLOCK.name() + ":0");
        inventoryLockService.unlock(bo);
        return success(true);
    }

}
