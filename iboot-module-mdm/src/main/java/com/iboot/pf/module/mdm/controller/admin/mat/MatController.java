package com.iboot.pf.module.mdm.controller.admin.mat;

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

import com.iboot.pf.module.mdm.controller.admin.mat.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.mat.MatDO;
import com.iboot.pf.module.mdm.service.mat.MatService;

@Tag(name = "管理后台 - 物料主档")
@RestController
@RequestMapping("/mdm/mat")
@Validated
public class MatController {

    @Resource
    private MatService matService;

    @PostMapping("/create")
    @Operation(summary = "创建物料主档")
    @PreAuthorize("@ss.hasPermission('mdm:mat:create')")
    public CommonResult<Long> createMat(@Valid @RequestBody MatSaveReqVO createReqVO) {
        return success(matService.createMat(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新物料主档")
    @PreAuthorize("@ss.hasPermission('mdm:mat:update')")
    public CommonResult<Boolean> updateMat(@Valid @RequestBody MatSaveReqVO updateReqVO) {
        matService.updateMat(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除物料主档")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:mat:delete')")
    public CommonResult<Boolean> deleteMat(@RequestParam("id") Long id) {
        matService.deleteMat(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除物料主档")
                @PreAuthorize("@ss.hasPermission('mdm:mat:delete')")
    public CommonResult<Boolean> deleteMatList(@RequestParam("ids") List<Long> ids) {
        matService.deleteMatListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得物料主档")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:mat:query')")
    public CommonResult<MatRespVO> getMat(@RequestParam("id") Long id) {
        return success(matService.getMat(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得物料主档分页")
    @PreAuthorize("@ss.hasPermission('mdm:mat:query')")
    public CommonResult<PageResult<MatRespVO>> getMatPage(@Valid MatPageReqVO pageReqVO) {
        return success(matService.getMatPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出物料主档 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:mat:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportMatExcel(@Valid MatPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MatRespVO> list = matService.getMatPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "物料主档.xls", "数据", MatRespVO.class, list);
    }

}
