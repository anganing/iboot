package com.iboot.pf.module.aps.controller.admin.version;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.framework.security.core.util.SecurityFrameworkUtils;
import com.iboot.pf.module.aps.api.version.dto.ChangeVersionStatusReqDTO;
import com.iboot.pf.module.aps.api.version.dto.GenerateVersionReqDTO;
import com.iboot.pf.module.aps.controller.admin.version.vo.GenerateVersionReqVO;
import com.iboot.pf.module.aps.controller.admin.version.vo.SolveProgressRespVO;
import com.iboot.pf.module.aps.controller.admin.version.vo.VersionPageReqVO;
import com.iboot.pf.module.aps.controller.admin.version.vo.VersionRespVO;
import com.iboot.pf.module.aps.enums.ApsCommandTypeEnum;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.service.solve.ApsSolveService;
import com.iboot.pf.module.aps.service.solve.dto.ApsSolveProgress;
import com.iboot.pf.module.aps.service.version.ApsVersionService;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
 * APS-排程版本。
 *
 * <p>版本是求解产出的结果表，只提供查询、导出与“确认 / 发布”状态流转，不提供新增、修改、删除、导入：
 * 版本号、得分、发布记录都是排程过程的证据链，放开人工写入就没法追溯计划是怎么来的。
 */
@Tag(name = "管理后台 - APS-排程版本")
@RestController
@RequestMapping("/aps/version")
@Validated
public class VersionController {

    @Resource
    private ApsVersionService versionService;
    @Resource
    private ApsSolveService solveService;

    @PostMapping("/generate")
    @Operation(summary = "生成排程版本并提交求解")
    @PreAuthorize("@ss.hasPermission('aps:version:update')")
    @ApiAccessLog(operateType = UPDATE)
    public CommonResult<Long> generateVersion(@Valid @RequestBody GenerateVersionReqVO reqVO) {
        return success(solveService.generateAndSolve(buildGenerateCommand(reqVO)));
    }

    @PutMapping("/cancel-solve")
    @Operation(summary = "取消排程求解")
    @Parameter(name = "versionId", description = "版本编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:version:update')")
    @ApiAccessLog(operateType = UPDATE)
    public CommonResult<Boolean> cancelSolve(@RequestParam("versionId") Long versionId) {
        solveService.cancelSolve(versionId);
        return success(true);
    }

    @GetMapping("/solve-progress")
    @Operation(summary = "获得求解进度")
    @Parameter(name = "versionId", description = "版本编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:version:query')")
    public CommonResult<SolveProgressRespVO> getSolveProgress(@RequestParam("versionId") Long versionId) {
        ApsSolveProgress progress = solveService.getProgress(versionId);
        return success(BeanUtils.toBean(progress, SolveProgressRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得排程版本")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:version:query')")
    public CommonResult<VersionRespVO> getVersion(@RequestParam("id") Long id) {
        return success(versionService.getVersion(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得排程版本分页")
    @PreAuthorize("@ss.hasPermission('aps:version:query')")
    public CommonResult<PageResult<VersionRespVO>> getVersionPage(@Valid VersionPageReqVO pageReqVO) {
        return success(versionService.getVersionPage(pageReqVO));
    }

    /**
     * 场景当前生效的排程版本。
     *
     * <p>权限挂 {@code aps:task:query} 而不是 {@code aps:version:query}：调用方是车间侧的"看生效计划"，
     * 他们只该看到已发布的计划，不应为了定位版本而拿到整个版本管理的查询权限。
     */
    @GetMapping("/get-published")
    @Operation(summary = "获得场景当前已发布的排程版本")
    @Parameter(name = "scenarioId", description = "排程场景编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:task:query')")
    public CommonResult<VersionRespVO> getPublishedVersion(@RequestParam("scenarioId") Long scenarioId) {
        return success(versionService.getPublishedVersion(scenarioId));
    }

    /**
     * 场景下曾经生效过的版本列表。
     *
     * <p>同样挂 {@code aps:task:query}：车间侧要回看上一版计划做对比，但不该为此拿到版本管理的查询权限。
     * 只返回已发布与已被替换，求解失败与待确认的版本从没下过车间。
     */
    @GetMapping("/list-effective")
    @Operation(summary = "获得场景下曾经生效过的排程版本列表")
    @Parameter(name = "scenarioId", description = "排程场景编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:task:query')")
    public CommonResult<List<VersionRespVO>> getEffectiveVersionList(@RequestParam("scenarioId") Long scenarioId) {
        return success(versionService.getEffectiveVersionList(scenarioId));
    }

    @PutMapping("/confirm")
    @Operation(summary = "确认排程版本")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:version:update')")
    @ApiAccessLog(operateType = UPDATE)
    public CommonResult<Boolean> confirmVersion(@RequestParam("id") Long id) {
        versionService.confirmVersion(buildCommand(ApsCommandTypeEnum.CONFIRM_VERSION, id));
        return success(true);
    }

    @PutMapping("/publish")
    @Operation(summary = "发布排程版本")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:version:update')")
    @ApiAccessLog(operateType = UPDATE)
    public CommonResult<Boolean> publishVersion(@RequestParam("id") Long id) {
        versionService.publishVersion(buildCommand(ApsCommandTypeEnum.PUBLISH_VERSION, id));
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出排程版本 Excel")
    @PreAuthorize("@ss.hasPermission('aps:version:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportVersionExcel(@Valid VersionPageReqVO pageReqVO,
                                   HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<VersionRespVO> list = versionService.getVersionPage(pageReqVO).getList();
        ExcelUtils.write(response, "APS-排程版本.xls", "数据", VersionRespVO.class, list);
    }

    /**
     * 把后台人工操作包装成跨模块命令。
     *
     * <p>幂等键用“命令类型 + 版本编号”，同一个版本重复点确认或发布不会重复写命令记录，
     * 状态流转本身也做了幂等判断。
     */
    private ChangeVersionStatusReqDTO buildCommand(ApsCommandTypeEnum commandType, Long versionId) {
        ChangeVersionStatusReqDTO reqDTO = new ChangeVersionStatusReqDTO();
        reqDTO.setVersionId(versionId);
        reqDTO.setOperatorId(SecurityFrameworkUtils.getLoginUserId());
        reqDTO.setIdempotencyKey(ApsSourceModuleEnum.UI.name() + ":" + commandType.name() + ":" + versionId);
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(versionId));
        return reqDTO;
    }

    /**
     * 生成排程命令。
     *
     * <p>幂等键带上时间戳：确认、发布是对同一个版本的一次性动作，重复点击应当被拦；
     * 而“生成排程”是每次都要产出一个新版本的动作，用固定键会让第二次生成直接返回第一次的版本编号。
     * 同场景并发由 Redis 场景锁负责拦截，不靠幂等键。
     */
    private GenerateVersionReqDTO buildGenerateCommand(GenerateVersionReqVO reqVO) {
        GenerateVersionReqDTO reqDTO = new GenerateVersionReqDTO();
        reqDTO.setScenarioId(reqVO.getScenarioId());
        reqDTO.setStrategyId(reqVO.getStrategyId());
        reqDTO.setRemark(reqVO.getRemark());
        reqDTO.setOperatorId(SecurityFrameworkUtils.getLoginUserId());
        reqDTO.setIdempotencyKey(ApsSourceModuleEnum.UI.name() + ":" + ApsCommandTypeEnum.GENERATE_VERSION.name()
                + ":" + reqVO.getScenarioId() + ":" + System.currentTimeMillis());
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(reqVO.getScenarioId()));
        return reqDTO;
    }

}
