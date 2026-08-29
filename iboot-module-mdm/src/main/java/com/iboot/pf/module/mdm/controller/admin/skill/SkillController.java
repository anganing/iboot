package com.iboot.pf.module.mdm.controller.admin.skill;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mdm.controller.admin.skill.vo.*;
import com.iboot.pf.module.mdm.service.skill.SkillService;
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

@Tag(name = "管理后台 - 技能")
@RestController
@RequestMapping("/mdm/skill")
@Validated
public class SkillController {

    @Resource
    private SkillService skillService;

    // ==================== 技能 ====================

    @PostMapping("/create")
    @Operation(summary = "创建技能")
    @PreAuthorize("@ss.hasPermission('mdm:skill:create')")
    public CommonResult<Long> createSkill(@Valid @RequestBody SkillSaveReqVO createReqVO) {
        return success(skillService.createSkill(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新技能")
    @PreAuthorize("@ss.hasPermission('mdm:skill:update')")
    public CommonResult<Boolean> updateSkill(@Valid @RequestBody SkillSaveReqVO updateReqVO) {
        skillService.updateSkill(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除技能")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:skill:delete')")
    public CommonResult<Boolean> deleteSkill(@RequestParam("id") Long id) {
        skillService.deleteSkill(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除技能")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:skill:delete')")
    public CommonResult<Boolean> deleteSkillList(@RequestParam("ids") List<Long> ids) {
        skillService.deleteSkillListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得技能")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:skill:query')")
    public CommonResult<SkillRespVO> getSkill(@RequestParam("id") Long id) {
        return success(skillService.getSkill(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得技能分页")
    @PreAuthorize("@ss.hasPermission('mdm:skill:query')")
    public CommonResult<PageResult<SkillRespVO>> getSkillPage(@Valid SkillPageReqVO pageReqVO) {
        return success(skillService.getSkillPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出技能 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:skill:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSkillExcel(@Valid SkillPageReqVO pageReqVO,
                                 HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SkillRespVO> list = skillService.getSkillPage(pageReqVO).getList();
        ExcelUtils.write(response, "技能.xls", "数据", SkillRespVO.class, list);
    }

    // ==================== 工序技能要求 ====================

    @PostMapping("/op-req/create")
    @Operation(summary = "创建工序技能要求")
    @PreAuthorize("@ss.hasPermission('mdm:skill:update')")
    public CommonResult<Long> createOpSkillReq(@Valid @RequestBody OpSkillReqSaveReqVO createReqVO) {
        return success(skillService.createOpSkillReq(createReqVO));
    }

    @PutMapping("/op-req/update")
    @Operation(summary = "更新工序技能要求")
    @PreAuthorize("@ss.hasPermission('mdm:skill:update')")
    public CommonResult<Boolean> updateOpSkillReq(@Valid @RequestBody OpSkillReqSaveReqVO updateReqVO) {
        skillService.updateOpSkillReq(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/op-req/delete")
    @Operation(summary = "删除工序技能要求")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:skill:update')")
    public CommonResult<Boolean> deleteOpSkillReq(@RequestParam("id") Long id) {
        skillService.deleteOpSkillReq(id);
        return success(true);
    }

    @DeleteMapping("/op-req/delete-list")
    @Operation(summary = "批量删除工序技能要求")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:skill:update')")
    public CommonResult<Boolean> deleteOpSkillReqList(@RequestParam("ids") List<Long> ids) {
        skillService.deleteOpSkillReqListByIds(ids);
        return success(true);
    }

    @GetMapping("/op-req/get")
    @Operation(summary = "获得工序技能要求")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:skill:query')")
    public CommonResult<OpSkillReqRespVO> getOpSkillReq(@RequestParam("id") Long id) {
        return success(skillService.getOpSkillReq(id));
    }

    @GetMapping("/op-req/page")
    @Operation(summary = "获得工序技能要求分页")
    @PreAuthorize("@ss.hasPermission('mdm:skill:query')")
    public CommonResult<PageResult<OpSkillReqRespVO>> getOpSkillReqPage(@Valid PageParam pageParam,
                                                                        @RequestParam("opId") Long opId) {
        return success(skillService.getOpSkillReqPage(pageParam, opId));
    }

    // ==================== 人员技能 ====================

    @PostMapping("/member/create")
    @Operation(summary = "创建人员技能")
    @PreAuthorize("@ss.hasPermission('mdm:skill:update')")
    public CommonResult<Long> createMemberSkill(@Valid @RequestBody MemberSkillSaveReqVO createReqVO) {
        return success(skillService.createMemberSkill(createReqVO));
    }

    @PutMapping("/member/update")
    @Operation(summary = "更新人员技能")
    @PreAuthorize("@ss.hasPermission('mdm:skill:update')")
    public CommonResult<Boolean> updateMemberSkill(@Valid @RequestBody MemberSkillSaveReqVO updateReqVO) {
        skillService.updateMemberSkill(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/member/delete")
    @Operation(summary = "删除人员技能")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:skill:update')")
    public CommonResult<Boolean> deleteMemberSkill(@RequestParam("id") Long id) {
        skillService.deleteMemberSkill(id);
        return success(true);
    }

    @DeleteMapping("/member/delete-list")
    @Operation(summary = "批量删除人员技能")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:skill:update')")
    public CommonResult<Boolean> deleteMemberSkillList(@RequestParam("ids") List<Long> ids) {
        skillService.deleteMemberSkillListByIds(ids);
        return success(true);
    }

    @GetMapping("/member/get")
    @Operation(summary = "获得人员技能")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:skill:query')")
    public CommonResult<MemberSkillRespVO> getMemberSkill(@RequestParam("id") Long id) {
        return success(skillService.getMemberSkill(id));
    }

    @GetMapping("/member/page")
    @Operation(summary = "获得人员技能分页")
    @PreAuthorize("@ss.hasPermission('mdm:skill:query')")
    public CommonResult<PageResult<MemberSkillRespVO>> getMemberSkillPage(@Valid PageParam pageParam,
                                                                          @RequestParam(value = "userId", required = false) Long userId,
                                                                          @RequestParam(value = "skillId", required = false) Long skillId) {
        return success(skillService.getMemberSkillPage(pageParam, userId, skillId));
    }

}
