package com.iboot.pf.module.mes.controller.admin.prodreport;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportPageReqVO;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportRespVO;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportSaveReqVO;
import com.iboot.pf.module.mes.service.prodreport.ProdReportService;
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
 * 生产报工管理端接口。
 *
 * <p>报工是生产现场的实时动作，不提供 Excel 导入：一批报工需要逐条校验工序任务状态与数量口径，
 * 批量灌入等于绕过这些校验。
 */
@Tag(name = "管理后台 - MES-生产报工")
@RestController
@RequestMapping("/mes/prod-report")
@Validated
public class ProdReportController {

    @Resource
    private ProdReportService prodReportService;

    @PostMapping("/create")
    @Operation(summary = "创建报工记录")
    @PreAuthorize("@ss.hasPermission('mes:prod-report:create')")
    public CommonResult<Long> createProdReport(@Valid @RequestBody ProdReportSaveReqVO createReqVO) {
        return success(prodReportService.createProdReport(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新报工记录")
    @PreAuthorize("@ss.hasPermission('mes:prod-report:update')")
    public CommonResult<Boolean> updateProdReport(@Valid @RequestBody ProdReportSaveReqVO updateReqVO) {
        prodReportService.updateProdReport(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除报工记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:prod-report:delete')")
    public CommonResult<Boolean> deleteProdReport(@RequestParam("id") Long id) {
        prodReportService.deleteProdReport(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除报工记录")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:prod-report:delete')")
    public CommonResult<Boolean> deleteProdReportList(@RequestParam("ids") List<Long> ids) {
        prodReportService.deleteProdReportListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得报工记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:prod-report:query')")
    public CommonResult<ProdReportRespVO> getProdReport(@RequestParam("id") Long id) {
        return success(prodReportService.getProdReport(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得报工记录分页")
    @PreAuthorize("@ss.hasPermission('mes:prod-report:query')")
    public CommonResult<PageResult<ProdReportRespVO>> getProdReportPage(@Valid ProdReportPageReqVO pageReqVO) {
        return success(prodReportService.getProdReportPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出报工记录 Excel")
    @PreAuthorize("@ss.hasPermission('mes:prod-report:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProdReportExcel(@Valid ProdReportPageReqVO pageReqVO,
                                      HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProdReportRespVO> list = prodReportService.getProdReportPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-生产报工.xls", "数据", ProdReportRespVO.class, list);
    }

}
