package com.iboot.pf.module.wms.controller.admin.wave;

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

import com.iboot.pf.module.wms.controller.admin.wave.vo.*;
import com.iboot.pf.module.wms.service.wave.WaveService;

@Tag(name = "管理后台 - WMS-出库波次")
@RestController
@RequestMapping("/wms/wave")
@Validated
public class WaveController {

    @Resource
    private WaveService waveService;

    @PostMapping("/create")
    @Operation(summary = "创建WMS-出库波次")
    @PreAuthorize("@ss.hasPermission('wms:wave:create')")
    public CommonResult<Long> createWave(@Valid @RequestBody WaveSaveReqVO createReqVO) {
        return success(waveService.createWave(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新WMS-出库波次")
    @PreAuthorize("@ss.hasPermission('wms:wave:update')")
    public CommonResult<Boolean> updateWave(@Valid @RequestBody WaveSaveReqVO updateReqVO) {
        waveService.updateWave(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除WMS-出库波次")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:wave:delete')")
    public CommonResult<Boolean> deleteWave(@RequestParam("id") Long id) {
        waveService.deleteWave(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-出库波次")
                @PreAuthorize("@ss.hasPermission('wms:wave:delete')")
    public CommonResult<Boolean> deleteWaveList(@RequestParam("ids") List<Long> ids) {
        waveService.deleteWaveListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得WMS-出库波次")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('wms:wave:query')")
    public CommonResult<WaveRespVO> getWave(@RequestParam("id") Long id) {
        return success(waveService.getWave(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得WMS-出库波次分页")
    @PreAuthorize("@ss.hasPermission('wms:wave:query')")
    public CommonResult<PageResult<WaveRespVO>> getWavePage(@Valid WavePageReqVO pageReqVO) {
        return success(waveService.getWavePage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出WMS-出库波次 Excel")
    @PreAuthorize("@ss.hasPermission('wms:wave:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWaveExcel(@Valid WavePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WaveRespVO> list = waveService.getWavePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "WMS-出库波次.xls", "数据", WaveRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入WMS-出库波次模板")
    @PreAuthorize("@ss.hasPermission('wms:wave:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "WMS-出库波次导入模板.xls", "数据",
                WaveImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入WMS-出库波次")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('wms:wave:import')")
    public CommonResult<WaveImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<WaveImportExcelVO> list = ExcelUtils.read(file, WaveImportExcelVO.class);
        return success(waveService.importWaveList(list));
    }

    // ==================== 子表（WMS-波次明细） ====================

    @GetMapping("/wave-line/page")
    @Operation(summary = "获得WMS-波次明细分页")
    @PreAuthorize("@ss.hasPermission('wms:wave:query')")
    public CommonResult<PageResult<WaveLineRespVO>> getWaveLinePage(@Valid WaveLinePageReqVO pageReqVO) {
        return success(waveService.getWaveLinePage(pageReqVO));
    }

    @PostMapping("/wave-line/create")
    @Operation(summary = "创建WMS-波次明细")
    @PreAuthorize("@ss.hasPermission('wms:wave:create')")
    public CommonResult<Long> createWaveLine(@Valid @RequestBody WaveLineSaveReqVO createReqVO) {
        return success(waveService.createWaveLine(createReqVO));
    }

    @PutMapping("/wave-line/update")
    @Operation(summary = "更新WMS-波次明细")
    @PreAuthorize("@ss.hasPermission('wms:wave:update')")
    public CommonResult<Boolean> updateWaveLine(@Valid @RequestBody WaveLineSaveReqVO updateReqVO) {
        waveService.updateWaveLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/wave-line/delete")
    @Parameter(name = "id", description = "编号", required = true)
    @Operation(summary = "删除WMS-波次明细")
    @PreAuthorize("@ss.hasPermission('wms:wave:delete')")
    public CommonResult<Boolean> deleteWaveLine(@RequestParam("id") Long id) {
        waveService.deleteWaveLine(id);
        return success(true);
    }

    @DeleteMapping("/wave-line/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除WMS-波次明细")
    @PreAuthorize("@ss.hasPermission('wms:wave:delete')")
    public CommonResult<Boolean> deleteWaveLineList(@RequestParam("ids") List<Long> ids) {
        waveService.deleteWaveLineListByIds(ids);
        return success(true);
    }

	@GetMapping("/wave-line/get")
	@Operation(summary = "获得WMS-波次明细")
	@Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:wave:query')")
	public CommonResult<WaveLineRespVO> getWaveLine(@RequestParam("id") Long id) {
	    return success(waveService.getWaveLine(id));
	}

}