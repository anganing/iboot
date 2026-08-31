package com.iboot.pf.module.aps.controller.admin.scenario;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.aps.api.scenario.dto.CloseScenarioReqDTO;
import com.iboot.pf.module.aps.api.projection.dto.RefreshProjectionReqDTO;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.ScenarioPageReqVO;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.ScenarioRespVO;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.ScenarioSaveReqVO;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.service.projection.ApsProjectionService;
import com.iboot.pf.module.aps.service.scenario.ApsScenarioService;
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
import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.UPDATE;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * APS-排程场景。
 *
 * <p>提供 CRUD、导出与“关闭场景”业务动作；不提供 Excel 导入：场景要绑定策略与计划窗口，
 * 靠表格批量建没有实际业务场景。
 */
@Tag(name = "管理后台 - APS-排程场景")
@RestController
@RequestMapping("/aps/scenario")
@Validated
public class ScenarioController {

    @Resource
    private ApsScenarioService scenarioService;
    @Resource
    private ApsProjectionService projectionService;

    @PostMapping("/create")
    @Operation(summary = "创建排程场景")
    @PreAuthorize("@ss.hasPermission('aps:scenario:create')")
    public CommonResult<Long> createScenario(@Valid @RequestBody ScenarioSaveReqVO createReqVO) {
        return success(scenarioService.createScenario(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新排程场景")
    @PreAuthorize("@ss.hasPermission('aps:scenario:update')")
    public CommonResult<Boolean> updateScenario(@Valid @RequestBody ScenarioSaveReqVO updateReqVO) {
        scenarioService.updateScenario(updateReqVO);
        return success(true);
    }

    @PutMapping("/close")
    @Operation(summary = "关闭排程场景")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:scenario:update')")
    @ApiAccessLog(operateType = UPDATE)
    public CommonResult<Boolean> closeScenario(@RequestParam("id") Long id) {
        CloseScenarioReqDTO reqDTO = new CloseScenarioReqDTO();
        reqDTO.setScenarioId(id);
        // 后台人工关闭也走同一条幂等命令，幂等键带上场景编号，重复点击不会重复写命令记录
        reqDTO.setIdempotencyKey("UI:CLOSE_SCENARIO:" + id);
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(id));
        scenarioService.closeScenario(reqDTO);
        return success(true);
    }

    @PutMapping("/refresh-projection")
    @Operation(summary = "刷新排程输入投影")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:scenario:update')")
    @ApiAccessLog(operateType = UPDATE)
    public CommonResult<Boolean> refreshProjection(@RequestParam("id") Long id) {
        // 四张投影一起刷：只刷一部分会得到半新半旧的输入，算出来的计划无法解释
        projectionService.refreshMaterialProjection(buildRefreshCommand(
                ApsCommandTypeEnum.REFRESH_MATERIAL_PROJECTION, id));
        projectionService.refreshInventoryProjection(buildRefreshCommand(
                ApsCommandTypeEnum.REFRESH_INVENTORY_PROJECTION, id));
        projectionService.refreshResourceProjection(buildRefreshCommand(
                ApsCommandTypeEnum.REFRESH_RESOURCE_PROJECTION, id));
        projectionService.refreshOrderProjection(buildRefreshCommand(
                ApsCommandTypeEnum.REFRESH_ORDER_PROJECTION, id));
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除排程场景")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:scenario:delete')")
    public CommonResult<Boolean> deleteScenario(@RequestParam("id") Long id) {
        scenarioService.deleteScenario(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除排程场景")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('aps:scenario:delete')")
    public CommonResult<Boolean> deleteScenarioList(@RequestParam("ids") List<Long> ids) {
        scenarioService.deleteScenarioListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得排程场景")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:scenario:query')")
    public CommonResult<ScenarioRespVO> getScenario(@RequestParam("id") Long id) {
        return success(scenarioService.getScenario(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得排程场景分页")
    @PreAuthorize("@ss.hasPermission('aps:scenario:query')")
    public CommonResult<PageResult<ScenarioRespVO>> getScenarioPage(@Valid ScenarioPageReqVO pageReqVO) {
        return success(scenarioService.getScenarioPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出排程场景 Excel")
    @PreAuthorize("@ss.hasPermission('aps:scenario:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportScenarioExcel(@Valid ScenarioPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ScenarioRespVO> list = scenarioService.getScenarioPage(pageReqVO).getList();
        ExcelUtils.write(response, "APS-排程场景.xls", "数据", ScenarioRespVO.class, list);
    }

    /**
     * 把后台人工刷新包装成跨模块命令。
     *
     * <p>刷新可以反复触发，幂等键带时间戳，不能只用场景编号；物料范围留空表示刷新全部物料。
     */
    private RefreshProjectionReqDTO buildRefreshCommand(ApsCommandTypeEnum commandType, Long scenarioId) {
        RefreshProjectionReqDTO reqDTO = new RefreshProjectionReqDTO();
        reqDTO.setScenarioId(scenarioId);
        reqDTO.setIdempotencyKey(ApsSourceModuleEnum.UI.name() + ":" + commandType.name() + ":" + scenarioId
                + ":" + System.currentTimeMillis());
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(scenarioId));
        return reqDTO;
    }

}
