package com.iboot.pf.module.mdm.controller.admin.res;

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

import com.iboot.pf.module.mdm.controller.admin.res.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import com.iboot.pf.module.mdm.service.res.ResService;

@Tag(name = "管理后台 - 生产资源")
@RestController
@RequestMapping("/mdm/res")
@Validated
public class ResController {

    @Resource
    private ResService resService;

    @PostMapping("/create")
    @Operation(summary = "创建生产资源")
    @PreAuthorize("@ss.hasPermission('mdm:res:create')")
    public CommonResult<Long> createRes(@Valid @RequestBody ResSaveReqVO createReqVO) {
        return success(resService.createRes(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产资源")
    @PreAuthorize("@ss.hasPermission('mdm:res:update')")
    public CommonResult<Boolean> updateRes(@Valid @RequestBody ResSaveReqVO updateReqVO) {
        resService.updateRes(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产资源")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:res:delete')")
    public CommonResult<Boolean> deleteRes(@RequestParam("id") Long id) {
        resService.deleteRes(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除生产资源")
                @PreAuthorize("@ss.hasPermission('mdm:res:delete')")
    public CommonResult<Boolean> deleteResList(@RequestParam("ids") List<Long> ids) {
        resService.deleteResListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产资源")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:res:query')")
    public CommonResult<ResRespVO> getRes(@RequestParam("id") Long id) {
        return success(resService.getRes(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得生产资源分页")
    @PreAuthorize("@ss.hasPermission('mdm:res:query')")
    public CommonResult<PageResult<ResRespVO>> getResPage(@Valid ResPageReqVO pageReqVO) {
        return success(resService.getResPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出生产资源 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:res:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportResExcel(@Valid ResPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ResRespVO> list = resService.getResPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "生产资源.xls", "数据", ResRespVO.class, list);
    }

}
