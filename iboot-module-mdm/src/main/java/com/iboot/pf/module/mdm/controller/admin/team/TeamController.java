package com.iboot.pf.module.mdm.controller.admin.team;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mdm.controller.admin.team.vo.*;
import com.iboot.pf.module.mdm.service.team.TeamService;
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

@Tag(name = "管理后台 - 班组")
@RestController
@RequestMapping("/mdm/team")
@Validated
public class TeamController {

    @Resource
    private TeamService teamService;

    // ==================== 班组 ====================

    @PostMapping("/create")
    @Operation(summary = "创建班组")
    @PreAuthorize("@ss.hasPermission('mdm:team:create')")
    public CommonResult<Long> createTeam(@Valid @RequestBody TeamSaveReqVO createReqVO) {
        return success(teamService.createTeam(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新班组")
    @PreAuthorize("@ss.hasPermission('mdm:team:update')")
    public CommonResult<Boolean> updateTeam(@Valid @RequestBody TeamSaveReqVO updateReqVO) {
        teamService.updateTeam(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除班组")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:team:delete')")
    public CommonResult<Boolean> deleteTeam(@RequestParam("id") Long id) {
        teamService.deleteTeam(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除班组")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:team:delete')")
    public CommonResult<Boolean> deleteTeamList(@RequestParam("ids") List<Long> ids) {
        teamService.deleteTeamListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得班组")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:team:query')")
    public CommonResult<TeamRespVO> getTeam(@RequestParam("id") Long id) {
        return success(teamService.getTeam(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得班组分页")
    @PreAuthorize("@ss.hasPermission('mdm:team:query')")
    public CommonResult<PageResult<TeamRespVO>> getTeamPage(@Valid TeamPageReqVO pageReqVO) {
        return success(teamService.getTeamPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出班组 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:team:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTeamExcel(@Valid TeamPageReqVO pageReqVO,
                                HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TeamRespVO> list = teamService.getTeamPage(pageReqVO).getList();
        ExcelUtils.write(response, "班组.xls", "数据", TeamRespVO.class, list);
    }

    // ==================== 班组成员 ====================

    @PostMapping("/member/create")
    @Operation(summary = "创建班组成员")
    @PreAuthorize("@ss.hasPermission('mdm:team:update')")
    public CommonResult<Long> createTeamMember(@Valid @RequestBody TeamMemberSaveReqVO createReqVO) {
        return success(teamService.createTeamMember(createReqVO));
    }

    @DeleteMapping("/member/delete")
    @Operation(summary = "删除班组成员")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:team:update')")
    public CommonResult<Boolean> deleteTeamMember(@RequestParam("id") Long id) {
        teamService.deleteTeamMember(id);
        return success(true);
    }

    @GetMapping("/member/page")
    @Operation(summary = "获得班组成员分页")
    @PreAuthorize("@ss.hasPermission('mdm:team:query')")
    public CommonResult<PageResult<TeamMemberRespVO>> getTeamMemberPage(@Valid PageParam pageParam,
                                                                        @RequestParam("teamId") Long teamId) {
        return success(teamService.getTeamMemberPage(pageParam, teamId));
    }

}
