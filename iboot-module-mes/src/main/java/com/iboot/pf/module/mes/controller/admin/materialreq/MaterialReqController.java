package com.iboot.pf.module.mes.controller.admin.materialreq;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLinePageReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLineRespVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqLineSaveReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqPageReqVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqRespVO;
import com.iboot.pf.module.mes.controller.admin.materialreq.vo.MaterialReqSaveReqVO;
import com.iboot.pf.module.mes.service.materialreq.MaterialReqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * 领退补料申请管理端接口。
 *
 * <p>明细子表挂在父资源路径下，六个接口齐全，权限沿用父资源的 query / create / update / delete。
 */
@Tag(name = "管理后台 - MES-领退补料申请")
@RestController
@RequestMapping("/mes/material-req")
@Validated
public class MaterialReqController {

    @Resource
    private MaterialReqService materialReqService;

    @PostMapping("/create")
    @Operation(summary = "创建领退补料申请")
    @PreAuthorize("@ss.hasPermission('mes:material-req:create')")
    public CommonResult<Long> createMaterialReq(@Valid @RequestBody MaterialReqSaveReqVO createReqVO) {
        return success(materialReqService.createMaterialReq(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新领退补料申请")
    @PreAuthorize("@ss.hasPermission('mes:material-req:update')")
    public CommonResult<Boolean> updateMaterialReq(@Valid @RequestBody MaterialReqSaveReqVO updateReqVO) {
        materialReqService.updateMaterialReq(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除领退补料申请")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:material-req:delete')")
    public CommonResult<Boolean> deleteMaterialReq(@RequestParam("id") Long id) {
        materialReqService.deleteMaterialReq(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除领退补料申请")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:material-req:delete')")
    public CommonResult<Boolean> deleteMaterialReqList(@RequestParam("ids") List<Long> ids) {
        materialReqService.deleteMaterialReqListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得领退补料申请")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:material-req:query')")
    public CommonResult<MaterialReqRespVO> getMaterialReq(@RequestParam("id") Long id) {
        return success(materialReqService.getMaterialReq(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得领退补料申请分页")
    @PreAuthorize("@ss.hasPermission('mes:material-req:query')")
    public CommonResult<PageResult<MaterialReqRespVO>> getMaterialReqPage(@Valid MaterialReqPageReqVO pageReqVO) {
        return success(materialReqService.getMaterialReqPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出领退补料申请 Excel")
    @PreAuthorize("@ss.hasPermission('mes:material-req:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportMaterialReqExcel(@Valid MaterialReqPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MaterialReqRespVO> list = materialReqService.getMaterialReqPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-领退补料申请.xls", "数据", MaterialReqRespVO.class, list);
    }

    // ==================== 子表（领退补料申请明细） ====================

    @GetMapping("/material-req-line/page")
    @Operation(summary = "获得领退补料申请明细分页")
    @PreAuthorize("@ss.hasPermission('mes:material-req:query')")
    public CommonResult<PageResult<MaterialReqLineRespVO>> getMaterialReqLinePage(
            @Valid MaterialReqLinePageReqVO pageReqVO) {
        return success(materialReqService.getMaterialReqLinePage(pageReqVO));
    }

    @GetMapping("/material-req-line/get")
    @Operation(summary = "获得领退补料申请明细")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:material-req:query')")
    public CommonResult<MaterialReqLineRespVO> getMaterialReqLine(@RequestParam("id") Long id) {
        return success(materialReqService.getMaterialReqLine(id));
    }

    @PostMapping("/material-req-line/create")
    @Operation(summary = "创建领退补料申请明细")
    @PreAuthorize("@ss.hasPermission('mes:material-req:create')")
    public CommonResult<Long> createMaterialReqLine(@Valid @RequestBody MaterialReqLineSaveReqVO createReqVO) {
        return success(materialReqService.createMaterialReqLine(createReqVO));
    }

    @PutMapping("/material-req-line/update")
    @Operation(summary = "更新领退补料申请明细")
    @PreAuthorize("@ss.hasPermission('mes:material-req:update')")
    public CommonResult<Boolean> updateMaterialReqLine(@Valid @RequestBody MaterialReqLineSaveReqVO updateReqVO) {
        materialReqService.updateMaterialReqLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/material-req-line/delete")
    @Operation(summary = "删除领退补料申请明细")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:material-req:delete')")
    public CommonResult<Boolean> deleteMaterialReqLine(@RequestParam("id") Long id) {
        materialReqService.deleteMaterialReqLine(id);
        return success(true);
    }

    @DeleteMapping("/material-req-line/delete-list")
    @Operation(summary = "批量删除领退补料申请明细")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:material-req:delete')")
    public CommonResult<Boolean> deleteMaterialReqLineList(@RequestParam("ids") List<Long> ids) {
        materialReqService.deleteMaterialReqLineListByIds(ids);
        return success(true);
    }

}
