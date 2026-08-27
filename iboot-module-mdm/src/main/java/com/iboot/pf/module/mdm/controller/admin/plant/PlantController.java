package com.iboot.pf.module.mdm.controller.admin.plant;

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

import com.iboot.pf.module.mdm.controller.admin.plant.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.plant.PlantDO;
import com.iboot.pf.module.mdm.service.plant.PlantService;

@Tag(name = "管理后台 - 工厂")
@RestController
@RequestMapping("/mdm/plant")
@Validated
public class PlantController {

    @Resource
    private PlantService plantService;

    @PostMapping("/create")
    @Operation(summary = "创建工厂")
    @PreAuthorize("@ss.hasPermission('mdm:plant:create')")
    public CommonResult<Long> createPlant(@Valid @RequestBody PlantSaveReqVO createReqVO) {
        return success(plantService.createPlant(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工厂")
    @PreAuthorize("@ss.hasPermission('mdm:plant:update')")
    public CommonResult<Boolean> updatePlant(@Valid @RequestBody PlantSaveReqVO updateReqVO) {
        plantService.updatePlant(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工厂")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mdm:plant:delete')")
    public CommonResult<Boolean> deletePlant(@RequestParam("id") Long id) {
        plantService.deletePlant(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除工厂")
                @PreAuthorize("@ss.hasPermission('mdm:plant:delete')")
    public CommonResult<Boolean> deletePlantList(@RequestParam("ids") List<Long> ids) {
        plantService.deletePlantListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工厂")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mdm:plant:query')")
    public CommonResult<PlantRespVO> getPlant(@RequestParam("id") Long id) {
        return success(plantService.getPlant(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工厂分页")
    @PreAuthorize("@ss.hasPermission('mdm:plant:query')")
    public CommonResult<PageResult<PlantRespVO>> getPlantPage(@Valid PlantPageReqVO pageReqVO) {
        return success(plantService.getPlantPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出工厂 Excel")
    @PreAuthorize("@ss.hasPermission('mdm:plant:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPlantExcel(@Valid PlantPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<PlantRespVO> list = plantService.getPlantPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "工厂.xls", "数据", PlantRespVO.class, list);
    }

}
