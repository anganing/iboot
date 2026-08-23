package com.iboot.pf.module.mdm.controller.admin.shift;

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
import com.iboot.pf.framework.common.util.object.BeanUtils;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

import com.iboot.pf.framework.excel.core.util.ExcelUtils;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.*;

import com.iboot.pf.module.mdm.controller.admin.shift.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.shift.ShiftDO;
import com.iboot.pf.module.mdm.service.shift.ShiftService;

@Tag(name = "管理后台 - 班次")
@RestController
@RequestMapping("/mdm/shift")
@Validated
public class ShiftController {

    @Resource
    private ShiftService shiftService;

    @PostMapping("/create")
    @Operation(summary = "创建班次")
    @PreAuthorize("@ss.hasPermission('mdm:shift:create')")
    public CommonResult<Long> createShift(@Valid @RequestBody ShiftSaveReqVO createReqVO) {
        return success(shiftService.createShift(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新班次")
    @PreAuthorize("@ss.hasPermission('mdm:shift:update')")
    public CommonResult<Boolean> updateShift(@Valid @RequestBody ShiftSaveReqVO updateReqVO) {
        shiftService.updateShift(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除班次")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:shift:delete')")
    public CommonResult<Boolean> deleteShift(@RequestParam("id") Long id) {
        shiftService.deleteShift(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除班次")
                @PreAuthorize("@ss.hasPermission('mdm:shift:delete')")
    public CommonResult<Boolean> deleteShiftList(@RequestParam("ids") List<Long> ids) {
        shiftService.deleteShiftListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得班次")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:shift:query')")
    public CommonResult<ShiftRespVO> getShift(@RequestParam("id") Long id) {
        ShiftDO shift = shiftService.getShift(id);
        return success(BeanUtils.toBean(shift, ShiftRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得班次分页")
    @PreAuthorize("@ss.hasPermission('mdm:shift:query')")
    public CommonResult<PageResult<ShiftRespVO>> getShiftPage(@Valid ShiftPageReqVO pageReqVO) {
        PageResult<ShiftDO> pageResult = shiftService.getShiftPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ShiftRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出班次 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:shift:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportShiftExcel(@Valid ShiftPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ShiftDO> list = shiftService.getShiftPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "班次.xls", "数据", ShiftRespVO.class,
                        BeanUtils.toBean(list, ShiftRespVO.class));
    }

}