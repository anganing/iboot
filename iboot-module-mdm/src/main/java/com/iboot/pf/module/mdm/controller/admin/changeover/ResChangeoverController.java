package com.iboot.pf.module.mdm.controller.admin.changeover;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mdm.controller.admin.changeover.vo.*;
import com.iboot.pf.module.mdm.service.changeover.ResChangeoverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 资源换型矩阵")
@RestController
@RequestMapping("/mdm/res-changeover")
@Validated
public class ResChangeoverController {

    @Resource
    private ResChangeoverService resChangeoverService;

    @PostMapping("/create")
    @Operation(summary = "创建换型矩阵")
    @PreAuthorize("@ss.hasPermission('mdm:res-changeover:create')")
    public CommonResult<Long> createResChangeover(@Valid @RequestBody ResChangeoverSaveReqVO createReqVO) {
        return success(resChangeoverService.createResChangeover(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新换型矩阵")
    @PreAuthorize("@ss.hasPermission('mdm:res-changeover:update')")
    public CommonResult<Boolean> updateResChangeover(@Valid @RequestBody ResChangeoverSaveReqVO updateReqVO) {
        resChangeoverService.updateResChangeover(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除换型矩阵")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:res-changeover:delete')")
    public CommonResult<Boolean> deleteResChangeover(@RequestParam("id") Long id) {
        resChangeoverService.deleteResChangeover(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除换型矩阵")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:res-changeover:delete')")
    public CommonResult<Boolean> deleteResChangeoverList(@RequestParam("ids") List<Long> ids) {
        resChangeoverService.deleteResChangeoverListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得换型矩阵")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:res-changeover:query')")
    public CommonResult<ResChangeoverRespVO> getResChangeover(@RequestParam("id") Long id) {
        return success(resChangeoverService.getResChangeover(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得换型矩阵分页")
    @PreAuthorize("@ss.hasPermission('mdm:res-changeover:query')")
    public CommonResult<PageResult<ResChangeoverRespVO>> getResChangeoverPage(@Valid ResChangeoverPageReqVO pageReqVO) {
        return success(resChangeoverService.getResChangeoverPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出换型矩阵 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:res-changeover:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportResChangeoverExcel(@Valid ResChangeoverPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ResChangeoverRespVO> list = resChangeoverService.getResChangeoverPage(pageReqVO).getList();
        ExcelUtils.write(response, "换型矩阵.xls", "数据", ResChangeoverRespVO.class, list);
    }

}
