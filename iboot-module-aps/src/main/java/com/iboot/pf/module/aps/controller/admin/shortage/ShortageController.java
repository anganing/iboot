package com.iboot.pf.module.aps.controller.admin.shortage;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.aps.api.shortage.dto.RefreshShortageReqDTO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortageHandleReqVO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortagePageReqVO;
import com.iboot.pf.module.aps.controller.admin.shortage.vo.ShortageRespVO;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.service.shortage.ApsShortageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.UPDATE;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * APS-排程缺料清单。
 *
 * <p>缺料由排程与刷新动作写入：提供查询、导出、刷新、补充预计供给时间与忽略，
 * 不提供新增、删除、导入。人工造缺料或改数量会让缺口和库存投影对不上。
 */
@Tag(name = "管理后台 - APS-排程缺料清单")
@RestController
@RequestMapping("/aps/shortage")
@Validated
public class ShortageController {

    @Resource
    private ApsShortageService shortageService;

    @GetMapping("/get")
    @Operation(summary = "获得缺料")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:shortage:query')")
    public CommonResult<ShortageRespVO> getShortage(@RequestParam("id") Long id) {
        return success(shortageService.getShortage(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得缺料分页")
    @PreAuthorize("@ss.hasPermission('aps:shortage:query')")
    public CommonResult<PageResult<ShortageRespVO>> getShortagePage(@Valid ShortagePageReqVO pageReqVO) {
        return success(shortageService.getShortagePage(pageReqVO));
    }

    @PutMapping("/handle")
    @Operation(summary = "补充缺料的预计供给时间")
    @PreAuthorize("@ss.hasPermission('aps:shortage:update')")
    public CommonResult<Boolean> handleShortage(@Valid @RequestBody ShortageHandleReqVO handleReqVO) {
        shortageService.handleShortage(handleReqVO);
        return success(true);
    }

    @PutMapping("/ignore")
    @Operation(summary = "忽略缺料")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:shortage:update')")
    @ApiAccessLog(operateType = UPDATE)
    public CommonResult<Boolean> ignoreShortage(@RequestParam("id") Long id) {
        shortageService.ignoreShortage(id);
        return success(true);
    }

    @PutMapping("/refresh")
    @Operation(summary = "按库存投影刷新缺料")
    @Parameter(name = "versionId", description = "排程版本编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:shortage:update')")
    @ApiAccessLog(operateType = UPDATE)
    public CommonResult<Boolean> refreshShortage(@RequestParam("versionId") Long versionId) {
        RefreshShortageReqDTO reqDTO = new RefreshShortageReqDTO();
        reqDTO.setVersionId(versionId);
        // 刷新可以反复触发，幂等键带时间戳，不能只用版本编号
        reqDTO.setIdempotencyKey(ApsSourceModuleEnum.UI.name() + ":" + ApsCommandTypeEnum.REFRESH_SHORTAGE.name()
                + ":" + versionId + ":" + System.currentTimeMillis());
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(versionId));
        shortageService.refreshShortage(reqDTO);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出缺料 Excel")
    @PreAuthorize("@ss.hasPermission('aps:shortage:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportShortageExcel(@Valid ShortagePageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ShortageRespVO> list = shortageService.getShortagePage(pageReqVO).getList();
        ExcelUtils.write(response, "APS-排程缺料清单.xls", "数据", ShortageRespVO.class, list);
    }

}
