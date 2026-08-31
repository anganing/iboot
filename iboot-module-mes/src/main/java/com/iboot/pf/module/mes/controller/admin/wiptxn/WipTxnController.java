package com.iboot.pf.module.mes.controller.admin.wiptxn;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.WipTxnPageReqVO;
import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.WipTxnRespVO;
import com.iboot.pf.module.mes.service.wiptxn.WipTxnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * 在制流转流水管理端接口。
 *
 * <p>账表只读，只提供 get / page / export-excel：流水由业务动作过账写入，
 * 放开人工新增修改删除等于允许篡改在制履历。
 */
@Tag(name = "管理后台 - MES-在制流转流水")
@RestController
@RequestMapping("/mes/wip-txn")
@Validated
public class WipTxnController {

    @Resource
    private WipTxnService wipTxnService;

    @GetMapping("/get")
    @Operation(summary = "获得在制流转流水")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:wip-txn:query')")
    public CommonResult<WipTxnRespVO> getWipTxn(@RequestParam("id") Long id) {
        return success(wipTxnService.getWipTxn(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得在制流转流水分页")
    @PreAuthorize("@ss.hasPermission('mes:wip-txn:query')")
    public CommonResult<PageResult<WipTxnRespVO>> getWipTxnPage(@Valid WipTxnPageReqVO pageReqVO) {
        return success(wipTxnService.getWipTxnPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出在制流转流水 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wip-txn:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWipTxnExcel(@Valid WipTxnPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WipTxnRespVO> list = wipTxnService.getWipTxnPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-在制流转流水.xls", "数据", WipTxnRespVO.class, list);
    }

}
