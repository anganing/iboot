package com.iboot.pf.module.mdm.controller.admin.rescapability;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mdm.controller.admin.rescapability.vo.*;
import com.iboot.pf.module.mdm.service.rescapability.ResCapabilityService;
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

@Tag(name = "管理后台 - 资源能力")
@RestController
@RequestMapping("/mdm/res-capability")
@Validated
public class ResCapabilityController {

    @Resource
    private ResCapabilityService resCapabilityService;

    @PostMapping("/create")
    @Operation(summary = "创建资源能力")
    @PreAuthorize("@ss.hasPermission('mdm:res-capability:create')")
    public CommonResult<Long> createResCapability(@Valid @RequestBody ResCapabilitySaveReqVO createReqVO) {
        return success(resCapabilityService.createResCapability(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新资源能力")
    @PreAuthorize("@ss.hasPermission('mdm:res-capability:update')")
    public CommonResult<Boolean> updateResCapability(@Valid @RequestBody ResCapabilitySaveReqVO updateReqVO) {
        resCapabilityService.updateResCapability(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除资源能力")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:res-capability:delete')")
    public CommonResult<Boolean> deleteResCapability(@RequestParam("id") Long id) {
        resCapabilityService.deleteResCapability(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除资源能力")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:res-capability:delete')")
    public CommonResult<Boolean> deleteResCapabilityList(@RequestParam("ids") List<Long> ids) {
        resCapabilityService.deleteResCapabilityListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得资源能力")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:res-capability:query')")
    public CommonResult<ResCapabilityRespVO> getResCapability(@RequestParam("id") Long id) {
        return success(resCapabilityService.getResCapability(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得资源能力分页")
    @PreAuthorize("@ss.hasPermission('mdm:res-capability:query')")
    public CommonResult<PageResult<ResCapabilityRespVO>> getResCapabilityPage(@Valid ResCapabilityPageReqVO pageReqVO) {
        return success(resCapabilityService.getResCapabilityPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出资源能力 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:res-capability:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportResCapabilityExcel(@Valid ResCapabilityPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ResCapabilityRespVO> list = resCapabilityService.getResCapabilityPage(pageReqVO).getList();
        ExcelUtils.write(response, "资源能力.xls", "数据", ResCapabilityRespVO.class, list);
    }

}
