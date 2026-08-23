package com.iboot.pf.module.mdm.controller.admin.uomconv;

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

import com.iboot.pf.module.mdm.controller.admin.uomconv.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.uomconv.UomConvDO;
import com.iboot.pf.module.mdm.service.uomconv.UomConvService;

@Tag(name = "管理后台 - 单位换算")
@RestController
@RequestMapping("/mdm/uom-conv")
@Validated
public class UomConvController {

    @Resource
    private UomConvService uomConvService;

    @PostMapping("/create")
    @Operation(summary = "创建单位换算")
    @PreAuthorize("@ss.hasPermission('mdm:uom-conv:create')")
    public CommonResult<Long> createUomConv(@Valid @RequestBody UomConvSaveReqVO createReqVO) {
        return success(uomConvService.createUomConv(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新单位换算")
    @PreAuthorize("@ss.hasPermission('mdm:uom-conv:update')")
    public CommonResult<Boolean> updateUomConv(@Valid @RequestBody UomConvSaveReqVO updateReqVO) {
        uomConvService.updateUomConv(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除单位换算")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:uom-conv:delete')")
    public CommonResult<Boolean> deleteUomConv(@RequestParam("id") Long id) {
        uomConvService.deleteUomConv(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除单位换算")
                @PreAuthorize("@ss.hasPermission('mdm:uom-conv:delete')")
    public CommonResult<Boolean> deleteUomConvList(@RequestParam("ids") List<Long> ids) {
        uomConvService.deleteUomConvListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得单位换算")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:uom-conv:query')")
    public CommonResult<UomConvRespVO> getUomConv(@RequestParam("id") Long id) {
        UomConvDO uomConv = uomConvService.getUomConv(id);
        return success(BeanUtils.toBean(uomConv, UomConvRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得单位换算分页")
    @PreAuthorize("@ss.hasPermission('mdm:uom-conv:query')")
    public CommonResult<PageResult<UomConvRespVO>> getUomConvPage(@Valid UomConvPageReqVO pageReqVO) {
        PageResult<UomConvDO> pageResult = uomConvService.getUomConvPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UomConvRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出单位换算 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:uom-conv:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportUomConvExcel(@Valid UomConvPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<UomConvDO> list = uomConvService.getUomConvPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "单位换算.xls", "数据", UomConvRespVO.class,
                        BeanUtils.toBean(list, UomConvRespVO.class));
    }

}