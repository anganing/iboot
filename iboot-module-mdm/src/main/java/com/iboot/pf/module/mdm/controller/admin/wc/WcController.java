package com.iboot.pf.module.mdm.controller.admin.wc;

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

import com.iboot.pf.module.mdm.controller.admin.wc.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.service.wc.WcService;

@Tag(name = "管理后台 - 工作中心")
@RestController
@RequestMapping("/mdm/wc")
@Validated
public class WcController {

    @Resource
    private WcService wcService;

    @PostMapping("/create")
    @Operation(summary = "创建工作中心")
    @PreAuthorize("@ss.hasPermission('mdm:wc:create')")
    public CommonResult<Long> createWc(@Valid @RequestBody WcSaveReqVO createReqVO) {
        return success(wcService.createWc(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工作中心")
    @PreAuthorize("@ss.hasPermission('mdm:wc:update')")
    public CommonResult<Boolean> updateWc(@Valid @RequestBody WcSaveReqVO updateReqVO) {
        wcService.updateWc(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工作中心")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:wc:delete')")
    public CommonResult<Boolean> deleteWc(@RequestParam("id") Long id) {
        wcService.deleteWc(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除工作中心")
                @PreAuthorize("@ss.hasPermission('mdm:wc:delete')")
    public CommonResult<Boolean> deleteWcList(@RequestParam("ids") List<Long> ids) {
        wcService.deleteWcListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工作中心")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:wc:query')")
    public CommonResult<WcRespVO> getWc(@RequestParam("id") Long id) {
        WcDO wc = wcService.getWc(id);
        return success(BeanUtils.toBean(wc, WcRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工作中心分页")
    @PreAuthorize("@ss.hasPermission('mdm:wc:query')")
    public CommonResult<PageResult<WcRespVO>> getWcPage(@Valid WcPageReqVO pageReqVO) {
        PageResult<WcDO> pageResult = wcService.getWcPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WcRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出工作中心 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:wc:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWcExcel(@Valid WcPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WcDO> list = wcService.getWcPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "工作中心.xls", "数据", WcRespVO.class,
                        BeanUtils.toBean(list, WcRespVO.class));
    }

}