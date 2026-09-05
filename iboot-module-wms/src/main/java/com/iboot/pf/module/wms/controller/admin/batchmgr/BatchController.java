package com.iboot.pf.module.wms.controller.admin.batchmgr;

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

import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.*;
import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.BATCH_NOT_EXISTS;

import com.iboot.pf.module.wms.controller.admin.batchmgr.vo.*;
import com.iboot.pf.module.wms.service.batchmgr.BatchService;

/**
 * WMS-批次主档只提供查询、导出与批属性维护。
 *
 * <p>批次由入库动作（到货确认、完工入库）经 {@code WmsBatchService#registerBatch} 落库：
 * 人工建批次会造出没有库存的孤儿批次，人工删批次会让引用它的余额与流水失去主档，
 * 所以新增/删除/导入端点一并撤除。修改降级为 {@code /update-attr}，只放开六个批属性字段。
 */
@Tag(name = "管理后台 - WMS-批次主档")
@RestController
@RequestMapping("/wms/batch")
@Validated
public class BatchController {

    @Resource
    private BatchService batchService;

    @GetMapping("/get")
    @Operation(summary = "获得WMS-批次主档")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:batch:query')")
    public CommonResult<BatchRespVO> getBatch(@RequestParam("id") Long id) {
        return success(batchService.getBatch(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-批次主档分页")
    @PreAuthorize("@ss.hasPermission('wms:batch:query')")
    public CommonResult<PageResult<BatchRespVO>> getBatchPage(@Valid BatchPageReqVO pageReqVO) {
        return success(batchService.getBatchPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-批次主档 Excel")
    @PreAuthorize("@ss.hasPermission('wms:batch:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportBatchExcel(@Valid BatchPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<BatchRespVO> list = batchService.getBatchPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-批次主档.xls", "数据", BatchRespVO.class,
                        list);
    }

    @PutMapping("/update-attr")
    @Operation(summary = "维护WMS-批次主档批属性")
    @PreAuthorize("@ss.hasPermission('wms:batch:update-attr')")
    public CommonResult<Boolean> updateBatchAttr(@Valid @RequestBody BatchAttrUpdateReqVO updateReqVO) {
        BatchRespVO batch = batchService.getBatch(updateReqVO.getId());
        if (batch == null) {
            throw exception(BATCH_NOT_EXISTS);
        }
        // 以库中现值为底，只覆盖六个批属性字段。物料、批次号、序列号、供应商是批次的身份，
        // 余额与流水按 batch_id 记账，身份改了就与账对不上，因此不接受界面传入。
        BatchSaveReqVO saveReqVO = BeanUtils.toBean(batch, BatchSaveReqVO.class);
        saveReqVO.setFurnaceLotNo(updateReqVO.getFurnaceLotNo());
        saveReqVO.setProduceDate(updateReqVO.getProduceDate());
        saveReqVO.setExpiryDate(updateReqVO.getExpiryDate());
        saveReqVO.setStatus(updateReqVO.getStatus());
        saveReqVO.setRemark(updateReqVO.getRemark());
        batchService.updateBatch(saveReqVO);
        return success(true);
    }

}
