package com.iboot.pf.module.wms.controller.admin.containertype;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

import com.iboot.pf.framework.excel.core.util.ExcelUtils;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.*;

import com.iboot.pf.module.wms.controller.admin.containertype.vo.*;
import com.iboot.pf.module.wms.service.containertype.ContainerTypeService;

@Tag(name = "管理后台 - WMS-容器类型")
@RestController
@RequestMapping("/wms/container-type")
@Validated
public class ContainerTypeController {

    @Resource
    private ContainerTypeService containerTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-容器类型")
    @PreAuthorize("@ss.hasPermission('wms:container-type:create')")
    public CommonResult<Long> createContainerType(@Valid @RequestBody ContainerTypeSaveReqVO createReqVO) {
        return success(containerTypeService.createContainerType(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-容器类型")
    @PreAuthorize("@ss.hasPermission('wms:container-type:update')")
    public CommonResult<Boolean> updateContainerType(@Valid @RequestBody ContainerTypeSaveReqVO updateReqVO) {
        containerTypeService.updateContainerType(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-容器类型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:container-type:delete')")
    public CommonResult<Boolean> deleteContainerType(@RequestParam("id") Long id) {
        containerTypeService.deleteContainerType(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-容器类型")
                @PreAuthorize("@ss.hasPermission('wms:container-type:delete')")
    public CommonResult<Boolean> deleteContainerTypeList(@RequestParam("ids") List<Long> ids) {
        containerTypeService.deleteContainerTypeListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-容器类型")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:container-type:query')")
    public CommonResult<ContainerTypeRespVO> getContainerType(@RequestParam("id") Long id) {
        return success(containerTypeService.getContainerType(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-容器类型分页")
    @PreAuthorize("@ss.hasPermission('wms:container-type:query')")
    public CommonResult<PageResult<ContainerTypeRespVO>> getContainerTypePage(@Valid ContainerTypePageReqVO pageReqVO) {
        return success(containerTypeService.getContainerTypePage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-容器类型 Excel")
    @PreAuthorize("@ss.hasPermission('wms:container-type:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportContainerTypeExcel(@Valid ContainerTypePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ContainerTypeRespVO> list = containerTypeService.getContainerTypePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-容器类型.xls", "数据", ContainerTypeRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-容器类型模板")
    @PreAuthorize("@ss.hasPermission('wms:container-type:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-容器类型导入模板.xls", "数据",
                ContainerTypeImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-容器类型")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:container-type:import')")
    public CommonResult<ContainerTypeImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ContainerTypeImportExcelVO> list = ExcelUtils.read(file, ContainerTypeImportExcelVO.class);
        return success(containerTypeService.importContainerTypeList(list));
    }

}