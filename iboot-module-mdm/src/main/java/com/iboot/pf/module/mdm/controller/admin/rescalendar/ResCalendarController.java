package com.iboot.pf.module.mdm.controller.admin.rescalendar;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

import com.iboot.pf.framework.excel.core.util.ExcelUtils;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.*;

import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.rescalendar.ResCalendarDO;
import com.iboot.pf.module.mdm.service.rescalendar.ResCalendarService;

@Tag(name = "管理后台 - 资源日历")
@RestController
@RequestMapping("/mdm/res-calendar")
@Validated
public class ResCalendarController {

    @Resource
    private ResCalendarService resCalendarService;

    @PostMapping("/create")
    @Operation(summary = "创建资源日历")
    @PreAuthorize("@ss.hasPermission('mdm:res-calendar:create')")
    public CommonResult<Long> createResCalendar(@Valid @RequestBody ResCalendarSaveReqVO createReqVO) {
        return success(resCalendarService.createResCalendar(createReqVO));
    }

    @PostMapping("/save-batch")
    @Operation(summary = "批量排班", description = "按资源与日期区间逐日写入，已存在同资源同日期的记录整体覆盖")
    @PreAuthorize("@ss.hasPermission('mdm:res-calendar:create')")
    public CommonResult<ResCalendarBatchSaveRespVO> saveResCalendarBatch(
            @Valid @RequestBody ResCalendarBatchSaveReqVO batchReqVO) {
        return success(resCalendarService.saveResCalendarBatch(batchReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新资源日历")
    @PreAuthorize("@ss.hasPermission('mdm:res-calendar:update')")
    public CommonResult<Boolean> updateResCalendar(@Valid @RequestBody ResCalendarSaveReqVO updateReqVO) {
        resCalendarService.updateResCalendar(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除资源日历")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:res-calendar:delete')")
    public CommonResult<Boolean> deleteResCalendar(@RequestParam("id") Long id) {
        resCalendarService.deleteResCalendar(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除资源日历")
                @PreAuthorize("@ss.hasPermission('mdm:res-calendar:delete')")
    public CommonResult<Boolean> deleteResCalendarList(@RequestParam("ids") List<Long> ids) {
        resCalendarService.deleteResCalendarListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得资源日历")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:res-calendar:query')")
    public CommonResult<ResCalendarRespVO> getResCalendar(@RequestParam("id") Long id) {
        return success(resCalendarService.getResCalendar(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得资源日历分页")
    @PreAuthorize("@ss.hasPermission('mdm:res-calendar:query')")
    public CommonResult<PageResult<ResCalendarRespVO>> getResCalendarPage(@Valid ResCalendarPageReqVO pageReqVO) {
        return success(resCalendarService.getResCalendarPage(pageReqVO));
    }

    @GetMapping("/list")
    @Operation(summary = "获得资源日历列表", description = "按资源与日期区间查询，供日历视图加载")
    @PreAuthorize("@ss.hasPermission('mdm:res-calendar:query')")
    public CommonResult<List<ResCalendarRespVO>> getResCalendarList(@Valid ResCalendarListReqVO listReqVO) {
        return success(resCalendarService.getResCalendarList(listReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出资源日历 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:res-calendar:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportResCalendarExcel(@Valid ResCalendarPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ResCalendarRespVO> list = resCalendarService.getResCalendarPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "资源日历.xls", "数据", ResCalendarRespVO.class, list);
    }

}
