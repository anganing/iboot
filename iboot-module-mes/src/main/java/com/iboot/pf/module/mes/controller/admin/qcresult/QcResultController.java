package com.iboot.pf.module.mes.controller.admin.qcresult;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultRespVO;
import com.iboot.pf.module.mes.controller.admin.qcresult.vo.QcResultSaveReqVO;
import com.iboot.pf.module.mes.service.qcresult.QcResultService;
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
 * 检验结果管理端接口。
 *
 * <p>结果按质检任务逐项录入，不提供 Excel 导入。
 */
@Tag(name = "管理后台 - MES-检验结果")
@RestController
@RequestMapping("/mes/qc-result")
@Validated
public class QcResultController {

    @Resource
    private QcResultService qcResultService;

    @PostMapping("/create")
    @Operation(summary = "创建检验结果")
    @PreAuthorize("@ss.hasPermission('mes:qc-result:create')")
    public CommonResult<Long> createQcResult(@Valid @RequestBody QcResultSaveReqVO createReqVO) {
        return success(qcResultService.createQcResult(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新检验结果")
    @PreAuthorize("@ss.hasPermission('mes:qc-result:update')")
    public CommonResult<Boolean> updateQcResult(@Valid @RequestBody QcResultSaveReqVO updateReqVO) {
        qcResultService.updateQcResult(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除检验结果")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:qc-result:delete')")
    public CommonResult<Boolean> deleteQcResult(@RequestParam("id") Long id) {
        qcResultService.deleteQcResult(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除检验结果")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-result:delete')")
    public CommonResult<Boolean> deleteQcResultList(@RequestParam("ids") List<Long> ids) {
        qcResultService.deleteQcResultListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得检验结果")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:qc-result:query')")
    public CommonResult<QcResultRespVO> getQcResult(@RequestParam("id") Long id) {
        return success(qcResultService.getQcResult(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得检验结果分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-result:query')")
    public CommonResult<PageResult<QcResultRespVO>> getQcResultPage(@Valid QcResultPageReqVO pageReqVO) {
        return success(qcResultService.getQcResultPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出检验结果 Excel")
    @PreAuthorize("@ss.hasPermission('mes:qc-result:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportQcResultExcel(@Valid QcResultPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<QcResultRespVO> list = qcResultService.getQcResultPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-检验结果.xls", "数据", QcResultRespVO.class, list);
    }

}
