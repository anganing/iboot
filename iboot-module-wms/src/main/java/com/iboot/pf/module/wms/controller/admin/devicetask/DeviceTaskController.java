package com.iboot.pf.module.wms.controller.admin.devicetask;

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

import com.iboot.pf.module.wms.controller.admin.devicetask.vo.*;
import com.iboot.pf.module.wms.service.devicetask.DeviceTaskService;

@Tag(name = "管理后台 - WMS-设备搬运任务")
@RestController
@RequestMapping("/wms/device-task")
@Validated
public class DeviceTaskController {

    @Resource
    private DeviceTaskService deviceTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-设备搬运任务")
    @PreAuthorize("@ss.hasPermission('wms:device-task:create')")
    public CommonResult<Long> createDeviceTask(@Valid @RequestBody DeviceTaskSaveReqVO createReqVO) {
        return success(deviceTaskService.createDeviceTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-设备搬运任务")
    @PreAuthorize("@ss.hasPermission('wms:device-task:update')")
    public CommonResult<Boolean> updateDeviceTask(@Valid @RequestBody DeviceTaskSaveReqVO updateReqVO) {
        deviceTaskService.updateDeviceTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-设备搬运任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:device-task:delete')")
    public CommonResult<Boolean> deleteDeviceTask(@RequestParam("id") Long id) {
        deviceTaskService.deleteDeviceTask(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-设备搬运任务")
                @PreAuthorize("@ss.hasPermission('wms:device-task:delete')")
    public CommonResult<Boolean> deleteDeviceTaskList(@RequestParam("ids") List<Long> ids) {
        deviceTaskService.deleteDeviceTaskListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-设备搬运任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:device-task:query')")
    public CommonResult<DeviceTaskRespVO> getDeviceTask(@RequestParam("id") Long id) {
        return success(deviceTaskService.getDeviceTask(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-设备搬运任务分页")
    @PreAuthorize("@ss.hasPermission('wms:device-task:query')")
    public CommonResult<PageResult<DeviceTaskRespVO>> getDeviceTaskPage(@Valid DeviceTaskPageReqVO pageReqVO) {
        return success(deviceTaskService.getDeviceTaskPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-设备搬运任务 Excel")
    @PreAuthorize("@ss.hasPermission('wms:device-task:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDeviceTaskExcel(@Valid DeviceTaskPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DeviceTaskRespVO> list = deviceTaskService.getDeviceTaskPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-设备搬运任务.xls", "数据", DeviceTaskRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-设备搬运任务模板")
    @PreAuthorize("@ss.hasPermission('wms:device-task:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-设备搬运任务导入模板.xls", "数据",
                DeviceTaskImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-设备搬运任务")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:device-task:import')")
    public CommonResult<DeviceTaskImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<DeviceTaskImportExcelVO> list = ExcelUtils.read(file, DeviceTaskImportExcelVO.class);
        return success(deviceTaskService.importDeviceTaskList(list));
    }

}