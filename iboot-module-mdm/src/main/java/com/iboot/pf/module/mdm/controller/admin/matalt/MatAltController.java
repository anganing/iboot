package com.iboot.pf.module.mdm.controller.admin.matalt;

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

import com.iboot.pf.module.mdm.controller.admin.matalt.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.matalt.MatAltDO;
import com.iboot.pf.module.mdm.service.matalt.MatAltService;

@Tag(name = "管理后台 - 替代料")
@RestController
@RequestMapping("/mdm/mat-alt")
@Validated
public class MatAltController {

    @Resource
    private MatAltService matAltService;

    @PostMapping("/create")
    @Operation(summary = "创建替代料")
    @PreAuthorize("@ss.hasPermission('mdm:matalt:create')")
    public CommonResult<Long> createMatAlt(@Valid @RequestBody MatAltSaveReqVO createReqVO) {
        return success(matAltService.createMatAlt(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新替代料")
    @PreAuthorize("@ss.hasPermission('mdm:matalt:update')")
    public CommonResult<Boolean> updateMatAlt(@Valid @RequestBody MatAltSaveReqVO updateReqVO) {
        matAltService.updateMatAlt(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除替代料")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:matalt:delete')")
    public CommonResult<Boolean> deleteMatAlt(@RequestParam("id") Long id) {
        matAltService.deleteMatAlt(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除替代料")
                @PreAuthorize("@ss.hasPermission('mdm:matalt:delete')")
    public CommonResult<Boolean> deleteMatAltList(@RequestParam("ids") List<Long> ids) {
        matAltService.deleteMatAltListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得替代料")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:matalt:query')")
    public CommonResult<MatAltRespVO> getMatAlt(@RequestParam("id") Long id) {
        MatAltDO matAlt = matAltService.getMatAlt(id);
        return success(BeanUtils.toBean(matAlt, MatAltRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得替代料分页")
    @PreAuthorize("@ss.hasPermission('mdm:matalt:query')")
    public CommonResult<PageResult<MatAltRespVO>> getMatAltPage(@Valid MatAltPageReqVO pageReqVO) {
        PageResult<MatAltDO> pageResult = matAltService.getMatAltPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MatAltRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出替代料 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:matalt:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportMatAltExcel(@Valid MatAltPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MatAltDO> list = matAltService.getMatAltPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "替代料.xls", "数据", MatAltRespVO.class,
                        BeanUtils.toBean(list, MatAltRespVO.class));
    }

}
