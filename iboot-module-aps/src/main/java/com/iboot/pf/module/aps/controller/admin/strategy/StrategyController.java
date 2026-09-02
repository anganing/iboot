package com.iboot.pf.module.aps.controller.admin.strategy;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategyPageReqVO;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategyConstraintMetadataRespVO;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategyRespVO;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategySaveReqVO;
import com.iboot.pf.module.aps.service.strategy.ApsStrategyService;
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
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * APS-排程策略。
 *
 * <p>策略是人工维护的配置，提供完整 CRUD 与导出；不提供 Excel 导入：
 * 约束权重是"约束名 -> 硬软权重"的映射，靠表格填写既不可读也没法校验。
 */
@Tag(name = "管理后台 - APS-排程策略")
@RestController("apsStrategyController")
@RequestMapping("/aps/strategy")
@Validated
public class StrategyController {

    @Resource
    private ApsStrategyService strategyService;

    @PostMapping("/create")
    @Operation(summary = "创建排程策略")
    @PreAuthorize("@ss.hasPermission('aps:strategy:create')")
    public CommonResult<Long> createStrategy(@Valid @RequestBody StrategySaveReqVO createReqVO) {
        return success(strategyService.createStrategy(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新排程策略")
    @PreAuthorize("@ss.hasPermission('aps:strategy:update')")
    public CommonResult<Boolean> updateStrategy(@Valid @RequestBody StrategySaveReqVO updateReqVO) {
        strategyService.updateStrategy(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除排程策略")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:strategy:delete')")
    public CommonResult<Boolean> deleteStrategy(@RequestParam("id") Long id) {
        strategyService.deleteStrategy(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除排程策略")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('aps:strategy:delete')")
    public CommonResult<Boolean> deleteStrategyList(@RequestParam("ids") List<Long> ids) {
        strategyService.deleteStrategyListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得排程策略")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('aps:strategy:query')")
    public CommonResult<StrategyRespVO> getStrategy(@RequestParam("id") Long id) {
        return success(strategyService.getStrategy(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得排程策略分页")
    @PreAuthorize("@ss.hasPermission('aps:strategy:query')")
    public CommonResult<PageResult<StrategyRespVO>> getStrategyPage(@Valid StrategyPageReqVO pageReqVO) {
        return success(strategyService.getStrategyPage(pageReqVO));
    }

    /**
     * 约束元数据。
     *
     * <p>权限沿用策略查询：它只是"有哪些约束可调、默认调到多少"的静态说明，
     * 打开策略表单就要用，不值得单独建一个权限项。
     */
    @GetMapping("/constraint-metadata")
    @Operation(summary = "获得排程约束元数据")
    @PreAuthorize("@ss.hasPermission('aps:strategy:query')")
    public CommonResult<StrategyConstraintMetadataRespVO> getConstraintMetadata() {
        return success(strategyService.getConstraintMetadata());
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出排程策略 Excel")
    @PreAuthorize("@ss.hasPermission('aps:strategy:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStrategyExcel(@Valid StrategyPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<StrategyRespVO> list = strategyService.getStrategyPage(pageReqVO).getList();
        ExcelUtils.write(response, "APS-排程策略.xls", "数据", StrategyRespVO.class, list);
    }

}
