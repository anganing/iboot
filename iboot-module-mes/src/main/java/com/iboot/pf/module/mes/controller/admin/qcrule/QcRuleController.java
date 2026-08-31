package com.iboot.pf.module.mes.controller.admin.qcrule;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRulePageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRuleRespVO;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRuleSaveReqVO;
import com.iboot.pf.module.mes.service.qcrule.QcRuleService;
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
 * 质检触发规则管理端接口。
 *
 * <p>规则是质量主数据，不提供 Excel 导入：匹配维度与检验方案都要逐条校验存在性与发布状态。
 */
@Tag(name = "管理后台 - MES-质检触发规则")
@RestController
@RequestMapping("/mes/qc-rule")
@Validated
public class QcRuleController {

    @Resource
    private QcRuleService qcRuleService;

    @PostMapping("/create")
    @Operation(summary = "创建质检触发规则")
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:create')")
    public CommonResult<Long> createQcRule(@Valid @RequestBody QcRuleSaveReqVO createReqVO) {
        return success(qcRuleService.createQcRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新质检触发规则")
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:update')")
    public CommonResult<Boolean> updateQcRule(@Valid @RequestBody QcRuleSaveReqVO updateReqVO) {
        qcRuleService.updateQcRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除质检触发规则")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:delete')")
    public CommonResult<Boolean> deleteQcRule(@RequestParam("id") Long id) {
        qcRuleService.deleteQcRule(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除质检触发规则")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:delete')")
    public CommonResult<Boolean> deleteQcRuleList(@RequestParam("ids") List<Long> ids) {
        qcRuleService.deleteQcRuleListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得质检触发规则")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:query')")
    public CommonResult<QcRuleRespVO> getQcRule(@RequestParam("id") Long id) {
        return success(qcRuleService.getQcRule(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得质检触发规则分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:query')")
    public CommonResult<PageResult<QcRuleRespVO>> getQcRulePage(@Valid QcRulePageReqVO pageReqVO) {
        return success(qcRuleService.getQcRulePage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出质检触发规则 Excel")
    @PreAuthorize("@ss.hasPermission('mes:qc-rule:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportQcRuleExcel(@Valid QcRulePageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<QcRuleRespVO> list = qcRuleService.getQcRulePage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-质检触发规则.xls", "数据", QcRuleRespVO.class, list);
    }

}
