package com.iboot.pf.module.wms.controller.admin.containermgr;

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

import com.iboot.pf.module.wms.controller.admin.containermgr.vo.*;
import com.iboot.pf.module.wms.service.containermgr.ContainerService;

@Tag(name = "管理后台 - WMS-容器主档")
@RestController
@RequestMapping("/wms/container")
@Validated
public class ContainerController {

    @Resource
    private ContainerService containerService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-容器主档")
    @PreAuthorize("@ss.hasPermission('wms:container:create')")
    public CommonResult<Long> createContainer(@Valid @RequestBody ContainerSaveReqVO createReqVO) {
        return success(containerService.createContainer(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-容器主档")
    @PreAuthorize("@ss.hasPermission('wms:container:update')")
    public CommonResult<Boolean> updateContainer(@Valid @RequestBody ContainerSaveReqVO updateReqVO) {
        containerService.updateContainer(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-容器主档")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:container:delete')")
    public CommonResult<Boolean> deleteContainer(@RequestParam("id") Long id) {
        containerService.deleteContainer(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-容器主档")
                @PreAuthorize("@ss.hasPermission('wms:container:delete')")
    public CommonResult<Boolean> deleteContainerList(@RequestParam("ids") List<Long> ids) {
        containerService.deleteContainerListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-容器主档")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:container:query')")
    public CommonResult<ContainerRespVO> getContainer(@RequestParam("id") Long id) {
        return success(containerService.getContainer(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-容器主档分页")
    @PreAuthorize("@ss.hasPermission('wms:container:query')")
    public CommonResult<PageResult<ContainerRespVO>> getContainerPage(@Valid ContainerPageReqVO pageReqVO) {
        return success(containerService.getContainerPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-容器主档 Excel")
    @PreAuthorize("@ss.hasPermission('wms:container:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportContainerExcel(@Valid ContainerPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ContainerRespVO> list = containerService.getContainerPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-容器主档.xls", "数据", ContainerRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-容器主档模板")
    @PreAuthorize("@ss.hasPermission('wms:container:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-容器主档导入模板.xls", "数据",
                ContainerImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-容器主档")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:container:import')")
    public CommonResult<ContainerImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<ContainerImportExcelVO> list = ExcelUtils.read(file, ContainerImportExcelVO.class);
        return success(containerService.importContainerList(list));
    }

}