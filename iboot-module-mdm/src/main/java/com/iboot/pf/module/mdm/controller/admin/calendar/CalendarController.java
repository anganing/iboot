package com.iboot.pf.module.mdm.controller.admin.calendar;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mdm.controller.admin.calendar.vo.*;
import com.iboot.pf.module.mdm.service.calendar.CalendarService;
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

@Tag(name = "管理后台 - 日历模板")
@RestController
@RequestMapping("/mdm/calendar")
@Validated
public class CalendarController {

    @Resource
    private CalendarService calendarService;

    // ==================== 日历主档 ====================

    @PostMapping("/create")
    @Operation(summary = "创建日历")
    @PreAuthorize("@ss.hasPermission('mdm:calendar:create')")
    public CommonResult<Long> createCalendar(@Valid @RequestBody CalendarSaveReqVO createReqVO) {
        return success(calendarService.createCalendar(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新日历")
    @PreAuthorize("@ss.hasPermission('mdm:calendar:update')")
    public CommonResult<Boolean> updateCalendar(@Valid @RequestBody CalendarSaveReqVO updateReqVO) {
        calendarService.updateCalendar(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除日历")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:calendar:delete')")
    public CommonResult<Boolean> deleteCalendar(@RequestParam("id") Long id) {
        calendarService.deleteCalendar(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除日历")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:calendar:delete')")
    public CommonResult<Boolean> deleteCalendarList(@RequestParam("ids") List<Long> ids) {
        for (Long id : ids) {
            calendarService.deleteCalendar(id);
        }
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得日历")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:calendar:query')")
    public CommonResult<CalendarRespVO> getCalendar(@RequestParam("id") Long id) {
        return success(calendarService.getCalendar(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得日历分页")
    @PreAuthorize("@ss.hasPermission('mdm:calendar:query')")
    public CommonResult<PageResult<CalendarRespVO>> getCalendarPage(@Valid CalendarPageReqVO pageReqVO) {
        return success(calendarService.getCalendarPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出日历 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:calendar:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCalendarExcel(@Valid CalendarPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CalendarRespVO> list = calendarService.getCalendarPage(pageReqVO).getList();
        ExcelUtils.write(response, "日历.xls", "数据", CalendarRespVO.class, list);
    }

    // ==================== 常规规则 ====================

    @PostMapping("/rule/create")
    @Operation(summary = "创建日历规则")
    @PreAuthorize("@ss.hasPermission('mdm:calendar:update')")
    public CommonResult<Long> createCalendarRule(@Valid @RequestBody CalendarRuleSaveReqVO createReqVO) {
        return success(calendarService.createCalendarRule(createReqVO));
    }

    @PutMapping("/rule/update")
    @Operation(summary = "更新日历规则")
    @PreAuthorize("@ss.hasPermission('mdm:calendar:update')")
    public CommonResult<Boolean> updateCalendarRule(@Valid @RequestBody CalendarRuleSaveReqVO updateReqVO) {
        calendarService.updateCalendarRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/rule/delete")
    @Operation(summary = "删除日历规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:calendar:update')")
    public CommonResult<Boolean> deleteCalendarRule(@RequestParam("id") Long id) {
        calendarService.deleteCalendarRule(id);
        return success(true);
    }

    @DeleteMapping("/rule/delete-list")
    @Operation(summary = "批量删除日历规则")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:calendar:update')")
    public CommonResult<Boolean> deleteCalendarRuleList(@RequestParam("ids") List<Long> ids) {
        calendarService.deleteCalendarRuleListByIds(ids);
        return success(true);
    }

    @GetMapping("/rule/get")
    @Operation(summary = "获得日历规则")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:calendar:query')")
    public CommonResult<CalendarRuleRespVO> getCalendarRule(@RequestParam("id") Long id) {
        return success(calendarService.getCalendarRule(id));
    }

    @GetMapping("/rule/page")
    @Operation(summary = "获得日历规则分页")
    @PreAuthorize("@ss.hasPermission('mdm:calendar:query')")
    public CommonResult<PageResult<CalendarRuleRespVO>> getCalendarRulePage(@Valid PageParam pageParam,
                                                                            @RequestParam("calendarId") Long calendarId) {
        return success(calendarService.getCalendarRulePage(pageParam, calendarId));
    }

    // ==================== 日期例外 ====================

    @PostMapping("/exception/create")
    @Operation(summary = "创建日历例外")
    @PreAuthorize("@ss.hasPermission('mdm:calendar:update')")
    public CommonResult<Long> createCalendarException(@Valid @RequestBody CalendarExceptionSaveReqVO createReqVO) {
        return success(calendarService.createCalendarException(createReqVO));
    }

    @PutMapping("/exception/update")
    @Operation(summary = "更新日历例外")
    @PreAuthorize("@ss.hasPermission('mdm:calendar:update')")
    public CommonResult<Boolean> updateCalendarException(@Valid @RequestBody CalendarExceptionSaveReqVO updateReqVO) {
        calendarService.updateCalendarException(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/exception/delete")
    @Operation(summary = "删除日历例外")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:calendar:update')")
    public CommonResult<Boolean> deleteCalendarException(@RequestParam("id") Long id) {
        calendarService.deleteCalendarException(id);
        return success(true);
    }

    @DeleteMapping("/exception/delete-list")
    @Operation(summary = "批量删除日历例外")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:calendar:update')")
    public CommonResult<Boolean> deleteCalendarExceptionList(@RequestParam("ids") List<Long> ids) {
        calendarService.deleteCalendarExceptionListByIds(ids);
        return success(true);
    }

    @GetMapping("/exception/get")
    @Operation(summary = "获得日历例外")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:calendar:query')")
    public CommonResult<CalendarExceptionRespVO> getCalendarException(@RequestParam("id") Long id) {
        return success(calendarService.getCalendarException(id));
    }

    @GetMapping("/exception/page")
    @Operation(summary = "获得日历例外分页")
    @PreAuthorize("@ss.hasPermission('mdm:calendar:query')")
    public CommonResult<PageResult<CalendarExceptionRespVO>> getCalendarExceptionPage(
            @Valid PageParam pageParam, @RequestParam("calendarId") Long calendarId) {
        return success(calendarService.getCalendarExceptionPage(pageParam, calendarId));
    }

    // ==================== 资源日历生成 ====================

    @PostMapping("/generate")
    @Operation(summary = "按日历模板生成资源日历")
    @PreAuthorize("@ss.hasPermission('mdm:calendar:update')")
    public CommonResult<Integer> generateResCalendars(@Valid @RequestBody CalendarGenerateReqVO generateReqVO) {
        return success(calendarService.generateResCalendars(generateReqVO).size());
    }

}
