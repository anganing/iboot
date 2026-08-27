package com.iboot.pf.module.mdm.controller.admin.bom;

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

import com.iboot.pf.module.mdm.controller.admin.bom.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.bom.BomDO;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import com.iboot.pf.module.mdm.service.bom.BomService;

@Tag(name = "管理后台 - 物料清单")
@RestController
@RequestMapping("/mdm/bom")
@Validated
public class BomController {

    @Resource
    private BomService bomService;

    @PostMapping("/create")
    @Operation(summary = "创建物料清单")
    @PreAuthorize("@ss.hasPermission('mdm:bom:create')")
    public CommonResult<Long> createBom(@Valid @RequestBody BomSaveReqVO createReqVO) {
        return success(bomService.createBom(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新物料清单")
    @PreAuthorize("@ss.hasPermission('mdm:bom:update')")
    public CommonResult<Boolean> updateBom(@Valid @RequestBody BomSaveReqVO updateReqVO) {
        bomService.updateBom(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除物料清单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:bom:delete')")
    public CommonResult<Boolean> deleteBom(@RequestParam("id") Long id) {
        bomService.deleteBom(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除物料清单")
                @PreAuthorize("@ss.hasPermission('mdm:bom:delete')")
    public CommonResult<Boolean> deleteBomList(@RequestParam("ids") List<Long> ids) {
        bomService.deleteBomListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得物料清单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:bom:query')")
    public CommonResult<BomRespVO> getBom(@RequestParam("id") Long id) {
        return success(bomService.getBom(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得物料清单分页")
    @PreAuthorize("@ss.hasPermission('mdm:bom:query')")
    public CommonResult<PageResult<BomRespVO>> getBomPage(@Valid BomPageReqVO pageReqVO) {
        return success(bomService.getBomPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出物料清单 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:bom:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportBomExcel(@Valid BomPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<BomRespVO> list = bomService.getBomPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "物料清单.xls", "数据", BomRespVO.class, list);
    }

    // ==================== 子表（物料清单明细） ====================

    @GetMapping("/bom-line/page")
    @Operation(summary = "获得物料清单明细分页")
    @Parameter(name = "bomId", description = "BOM编号")
    @PreAuthorize("@ss.hasPermission('mdm:bom:query')")
    public CommonResult<PageResult<BomLineRespVO>> getBomLinePage(PageParam pageReqVO,
                                                                                        @RequestParam("bomId") Long bomId) {
        return success(bomService.getBomLinePage(pageReqVO, bomId));
    }

    @PostMapping("/bom-line/create")
    @Operation(summary = "创建物料清单明细")
    @PreAuthorize("@ss.hasPermission('mdm:bom:create')")
    public CommonResult<Long> createBomLine(@Valid @RequestBody BomLineDO bomLine) {
        return success(bomService.createBomLine(bomLine));
    }

    @PutMapping("/bom-line/update")
    @Operation(summary = "更新物料清单明细")
    @PreAuthorize("@ss.hasPermission('mdm:bom:update')")
    public CommonResult<Boolean> updateBomLine(@Valid @RequestBody BomLineDO bomLine) {
        bomService.updateBomLine(bomLine);
        return success(true);
    }

    @DeleteMapping("/bom-line/delete")
    @Parameter(name = "id", description = "编号", required = true)
    @Operation(summary = "删除物料清单明细")
    @PreAuthorize("@ss.hasPermission('mdm:bom:delete')")
    public CommonResult<Boolean> deleteBomLine(@RequestParam("id") Long id) {
        bomService.deleteBomLine(id);
        return success(true);
    }

    @DeleteMapping("/bom-line/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除物料清单明细")
    @PreAuthorize("@ss.hasPermission('mdm:bom:delete')")
    public CommonResult<Boolean> deleteBomLineList(@RequestParam("ids") List<Long> ids) {
        bomService.deleteBomLineListByIds(ids);
        return success(true);
    }

	@GetMapping("/bom-line/get")
	@Operation(summary = "获得物料清单明细")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:bom:query')")
    public CommonResult<BomLineRespVO> getBomLine(@RequestParam("id") Long id) {
	    return success(bomService.getBomLine(id));
	}

}
