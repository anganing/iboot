package com.iboot.pf.module.mdm.controller.admin.workshop;

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

import com.iboot.pf.module.mdm.controller.admin.workshop.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.workshop.WorkshopDO;
import com.iboot.pf.module.mdm.service.workshop.WorkshopService;

@Tag(name = "管理后台 - 车间")
@RestController
@RequestMapping("/mdm/workshop")
@Validated
public class WorkshopController {

    @Resource
    private WorkshopService workshopService;

    @PostMapping("/create")
    @Operation(summary = "创建车间")
    @PreAuthorize("@ss.hasPermission('mdm:workshop:create')")
    public CommonResult<Long> createWorkshop(@Valid @RequestBody WorkshopSaveReqVO createReqVO) {
        return success(workshopService.createWorkshop(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新车间")
    @PreAuthorize("@ss.hasPermission('mdm:workshop:update')")
    public CommonResult<Boolean> updateWorkshop(@Valid @RequestBody WorkshopSaveReqVO updateReqVO) {
        workshopService.updateWorkshop(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除车间")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:workshop:delete')")
    public CommonResult<Boolean> deleteWorkshop(@RequestParam("id") Long id) {
        workshopService.deleteWorkshop(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除车间")
                @PreAuthorize("@ss.hasPermission('mdm:workshop:delete')")
    public CommonResult<Boolean> deleteWorkshopList(@RequestParam("ids") List<Long> ids) {
        workshopService.deleteWorkshopListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得车间")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:workshop:query')")
    public CommonResult<WorkshopRespVO> getWorkshop(@RequestParam("id") Long id) {
        WorkshopDO workshop = workshopService.getWorkshop(id);
        return success(BeanUtils.toBean(workshop, WorkshopRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得车间分页")
    @PreAuthorize("@ss.hasPermission('mdm:workshop:query')")
    public CommonResult<PageResult<WorkshopRespVO>> getWorkshopPage(@Valid WorkshopPageReqVO pageReqVO) {
        PageResult<WorkshopDO> pageResult = workshopService.getWorkshopPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WorkshopRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出车间 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:workshop:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWorkshopExcel(@Valid WorkshopPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WorkshopDO> list = workshopService.getWorkshopPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "车间.xls", "数据", WorkshopRespVO.class,
                        BeanUtils.toBean(list, WorkshopRespVO.class));
    }

}