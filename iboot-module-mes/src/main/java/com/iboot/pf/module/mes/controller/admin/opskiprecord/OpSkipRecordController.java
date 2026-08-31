package com.iboot.pf.module.mes.controller.admin.opskiprecord;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordPageReqVO;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordRespVO;
import com.iboot.pf.module.mes.controller.admin.opskiprecord.vo.OpSkipRecordSaveReqVO;
import com.iboot.pf.module.mes.service.opskiprecord.OpSkipRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * 工序跳过记录管理端接口。
 *
 * <p>跳过是现场例外处理并留痕，不提供 Excel 导入。
 */
@Tag(name = "管理后台 - MES-工序跳过记录")
@RestController
@RequestMapping("/mes/op-skip-record")
@Validated
public class OpSkipRecordController {

    @Resource
    private OpSkipRecordService opSkipRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建工序跳过记录")
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:create')")
    public CommonResult<Long> createOpSkipRecord(@Valid @RequestBody OpSkipRecordSaveReqVO createReqVO) {
        return success(opSkipRecordService.createOpSkipRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工序跳过记录")
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:update')")
    public CommonResult<Boolean> updateOpSkipRecord(@Valid @RequestBody OpSkipRecordSaveReqVO updateReqVO) {
        opSkipRecordService.updateOpSkipRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工序跳过记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:delete')")
    public CommonResult<Boolean> deleteOpSkipRecord(@RequestParam("id") Long id) {
        opSkipRecordService.deleteOpSkipRecord(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除工序跳过记录")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:delete')")
    public CommonResult<Boolean> deleteOpSkipRecordList(@RequestParam("ids") List<Long> ids) {
        opSkipRecordService.deleteOpSkipRecordListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工序跳过记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:query')")
    public CommonResult<OpSkipRecordRespVO> getOpSkipRecord(@RequestParam("id") Long id) {
        return success(opSkipRecordService.getOpSkipRecord(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工序跳过记录分页")
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:query')")
    public CommonResult<PageResult<OpSkipRecordRespVO>> getOpSkipRecordPage(
            @Valid OpSkipRecordPageReqVO pageReqVO) {
        return success(opSkipRecordService.getOpSkipRecordPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出工序跳过记录 Excel")
    @PreAuthorize("@ss.hasPermission('mes:op-skip-record:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOpSkipRecordExcel(@Valid OpSkipRecordPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OpSkipRecordRespVO> list = opSkipRecordService.getOpSkipRecordPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-工序跳过记录.xls", "数据", OpSkipRecordRespVO.class, list);
    }

}
