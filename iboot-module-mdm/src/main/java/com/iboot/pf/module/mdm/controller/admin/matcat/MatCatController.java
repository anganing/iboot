package com.iboot.pf.module.mdm.controller.admin.matcat;

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

import com.iboot.pf.module.mdm.controller.admin.matcat.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.matcat.MatCatDO;
import com.iboot.pf.module.mdm.service.matcat.MatCatService;

@Tag(name = "管理后台 - 物料分类")
@RestController
@RequestMapping("/mdm/mat-cat")
@Validated
public class MatCatController {

    @Resource
    private MatCatService matCatService;

    @PostMapping("/create")
    @Operation(summary = "创建物料分类")
    @PreAuthorize("@ss.hasPermission('mdm:mat-cat:create')")
    public CommonResult<Long> createMatCat(@Valid @RequestBody MatCatSaveReqVO createReqVO) {
        return success(matCatService.createMatCat(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新物料分类")
    @PreAuthorize("@ss.hasPermission('mdm:mat-cat:update')")
    public CommonResult<Boolean> updateMatCat(@Valid @RequestBody MatCatSaveReqVO updateReqVO) {
        matCatService.updateMatCat(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除物料分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:mat-cat:delete')")
    public CommonResult<Boolean> deleteMatCat(@RequestParam("id") Long id) {
        matCatService.deleteMatCat(id);
        return success(true);
    }


    @GetMapping("/get")
    @Operation(summary = "获得物料分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:mat-cat:query')")
    public CommonResult<MatCatRespVO> getMatCat(@RequestParam("id") Long id) {
        MatCatDO matCat = matCatService.getMatCat(id);
        return success(BeanUtils.toBean(matCat, MatCatRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得物料分类列表")
    @PreAuthorize("@ss.hasPermission('mdm:mat-cat:query')")
    public CommonResult<List<MatCatRespVO>> getMatCatList(@Valid MatCatListReqVO listReqVO) {
        List<MatCatDO> list = matCatService.getMatCatList(listReqVO);
        return success(BeanUtils.toBean(list, MatCatRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出物料分类 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:mat-cat:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportMatCatExcel(@Valid MatCatListReqVO listReqVO,
              HttpServletResponse response) throws IOException {
        List<MatCatDO> list = matCatService.getMatCatList(listReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "物料分类.xls", "数据", MatCatRespVO.class,
                        BeanUtils.toBean(list, MatCatRespVO.class));
    }

}