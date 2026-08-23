package com.iboot.pf.module.infra.controller.admin.coderule;

import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.infra.controller.admin.coderule.vo.CodeRulePageReqVO;
import com.iboot.pf.module.infra.controller.admin.coderule.vo.CodeRuleRespVO;
import com.iboot.pf.module.infra.controller.admin.coderule.vo.CodeRuleSaveReqVO;
import com.iboot.pf.module.infra.convert.coderule.CodeRuleConvert;
import com.iboot.pf.module.infra.dal.dataobject.coderule.CodeRuleDO;
import com.iboot.pf.module.infra.service.coderule.CodeRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
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

import static com.iboot.pf.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 编码规则")
@RestController
@RequestMapping("/infra/code-rule")
@Validated
public class CodeRuleController {

    @Resource
    private CodeRuleService codeRuleService;

    @PostMapping("/create")
    @Operation(summary = "创建编码规则")
    @PreAuthorize("@ss.hasPermission('infra:code-rule:create')")
    public CommonResult<Long> createCodeRule(@Valid @RequestBody CodeRuleSaveReqVO createReqVO) {
        return success(codeRuleService.createCodeRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改编码规则")
    @PreAuthorize("@ss.hasPermission('infra:code-rule:update')")
    public CommonResult<Boolean> updateCodeRule(@Valid @RequestBody CodeRuleSaveReqVO updateReqVO) {
        codeRuleService.updateCodeRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除编码规则")
    @PreAuthorize("@ss.hasPermission('infra:code-rule:delete')")
    public CommonResult<Boolean> deleteCodeRule(@RequestParam("id") Long id) {
        codeRuleService.deleteCodeRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得编码规则")
    @Parameter(name = "id", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:code-rule:query')")
    public CommonResult<CodeRuleRespVO> getCodeRule(@RequestParam("id") Long id) {
        return success(CodeRuleConvert.INSTANCE.convert(codeRuleService.getCodeRule(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得编码规则分页")
    @PreAuthorize("@ss.hasPermission('infra:code-rule:query')")
    public CommonResult<PageResult<CodeRuleRespVO>> getCodeRulePage(@Valid CodeRulePageReqVO pageReqVO) {
        PageResult<CodeRuleDO> page = codeRuleService.getCodeRulePage(pageReqVO);
        return success(CodeRuleConvert.INSTANCE.convertPage(page));
    }

    @GetMapping("/preview")
    @Operation(summary = "预览下一个编码")
    @PreAuthorize("@ss.hasPermission('infra:code-rule:query')")
    public CommonResult<String> previewCode(@RequestParam("ruleCode") String ruleCode) {
        return success(codeRuleService.previewCode(ruleCode));
    }

    @PostMapping("/generate")
    @Operation(summary = "分配一个编码")
    @PreAuthorize("@ss.hasPermission('infra:code-rule:query')")
    public CommonResult<String> generateCode(@RequestParam("ruleCode") String ruleCode) {
        return success(codeRuleService.generateCode(ruleCode));
    }

}
