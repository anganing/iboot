package com.iboot.pf.module.aps.controller.admin.load;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.aps.api.load.dto.RefreshLoadReqDTO;
import com.iboot.pf.module.aps.controller.admin.load.vo.LoadPageReqVO;
import com.iboot.pf.module.aps.controller.admin.load.vo.LoadRespVO;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.service.load.ApsLoadService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.UPDATE;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * APS-资源负荷。
 *
 * <p>负荷是排程结果的派生视图，只提供查询、导出与刷新，不提供新增、修改、删除、导入：
 * 负荷分钟数必须等于当天排定任务的占用之和，人工写入会让产能分析失真。
 */
@Tag(name = "管理后台 - APS-资源负荷")
@RestController
@RequestMapping("/aps/load")
@Validated
public class LoadController {

    @Resource
    private ApsLoadService loadService;

    @GetMapping("/get")
    @Operation(summary = "获得资源负荷")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:load:query')")
    public CommonResult<LoadRespVO> getLoad(@RequestParam("id") Long id) {
        return success(loadService.getLoad(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得资源负荷分页")
    @PreAuthorize("@ss.hasPermission('aps:load:query')")
    public CommonResult<PageResult<LoadRespVO>> getLoadPage(@Valid LoadPageReqVO pageReqVO) {
        return success(loadService.getLoadPage(pageReqVO));
    }

    @PutMapping("/refresh")
    @Operation(summary = "按排程任务重算资源负荷")
    @Parameter(name = "versionId", description = "排程版本编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:load:update')")
    @ApiAccessLog(operateType = UPDATE)
    public CommonResult<Boolean> refreshLoad(@RequestParam("versionId") Long versionId) {
        RefreshLoadReqDTO reqDTO = new RefreshLoadReqDTO();
        reqDTO.setVersionId(versionId);
        // 刷新可以反复触发，幂等键带时间戳，不能只用版本编号
        reqDTO.setIdempotencyKey(ApsSourceModuleEnum.UI.name() + ":" + ApsCommandTypeEnum.REFRESH_LOAD.name()
                + ":" + versionId + ":" + System.currentTimeMillis());
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(versionId));
        loadService.refreshLoad(reqDTO);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出资源负荷 Excel")
    @PreAuthorize("@ss.hasPermission('aps:load:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportLoadExcel(@Valid LoadPageReqVO pageReqVO,
                                HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<LoadRespVO> list = loadService.getLoadPage(pageReqVO).getList();
        ExcelUtils.write(response, "APS-资源负荷.xls", "数据", LoadRespVO.class, list);
    }

}
