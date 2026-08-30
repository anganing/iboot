package com.iboot.pf.module.mes.controller.admin.opskiprecord;

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

import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.*;
import com.iboot.pf.module.mes.service.opskiprecord.OpSkipRecordService;

@Tag(name = "管理后台 - MES-工序跳过记录")
@RestController
@RequestMapping("/mes/op-skip-record")
@Validated
public class OpSkipRecordController {

    @Resource
    private OpSkipRecordService opSkipRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建MES-工序跳过记录")
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:create')")
    public CommonResult<Long> createOpSkipRecord(@Valid @RequestBody OpSkipRecordSaveReqVO createReqVO) {
        return success(opSkipRecordService.createOpSkipRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新MES-工序跳过记录")
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:update')")
    public CommonResult<Boolean> updateOpSkipRecord(@Valid @RequestBody OpSkipRecordSaveReqVO updateReqVO) {
        opSkipRecordService.updateOpSkipRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除MES-工序跳过记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:delete')")
    public CommonResult<Boolean> deleteOpSkipRecord(@RequestParam("id") Long id) {
        opSkipRecordService.deleteOpSkipRecord(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除MES-工序跳过记录")
                @PreAuthorize("@ss.hasPermission('mes:op-skip-record:delete')")
    public CommonResult<Boolean> deleteOpSkipRecordList(@RequestParam("ids") List<Long> ids) {
        opSkipRecordService.deleteOpSkipRecordListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得MES-工序跳过记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:query')")
    public CommonResult<OpSkipRecordRespVO> getOpSkipRecord(@RequestParam("id") Long id) {
        return success(opSkipRecordService.getOpSkipRecord(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得MES-工序跳过记录分页")
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:query')")
    public CommonResult<PageResult<OpSkipRecordRespVO>> getOpSkipRecordPage(@Valid OpSkipRecordPageReqVO pageReqVO) {
        return success(opSkipRecordService.getOpSkipRecordPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出MES-工序跳过记录 Excel")
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOpSkipRecordExcel(@Valid OpSkipRecordPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OpSkipRecordRespVO> list = opSkipRecordService.getOpSkipRecordPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "MES-工序跳过记录.xls", "数据", OpSkipRecordRespVO.class,
                        list);
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入MES-工序跳过记录模板")
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "MES-工序跳过记录导入模板.xls", "数据",
                OpSkipRecordImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping("/import")
    @Operation(summary = "导入MES-工序跳过记录")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:import')")
    public CommonResult<OpSkipRecordImportRespVO> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<OpSkipRecordImportExcelVO> list = ExcelUtils.read(file, OpSkipRecordImportExcelVO.class);
        return success(opSkipRecordService.importOpSkipRecordList(list));
    }

}