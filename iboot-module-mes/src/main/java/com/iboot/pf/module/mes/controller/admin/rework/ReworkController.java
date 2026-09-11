package com.iboot.pf.module.mes.controller.admin.rework;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkPageReqVO;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkRespVO;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkActionReqVO;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.service.rework.ReworkService;
import com.iboot.pf.module.mes.service.rework.MesReworkDomainService;
import com.iboot.pf.module.mes.service.rework.bo.MesReworkActionBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.iboot.pf.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.iboot.pf.framework.common.pojo.CommonResult.success;

/**
 * 返工任务管理端接口。
 *
 * <p>返工任务由质检结论或人工发起，不提供 Excel 导入。
 */
@Tag(name = "管理后台 - MES-返工任务")
@RestController
@RequestMapping("/mes/rework")
@Validated
public class ReworkController {

    @Resource
    private ReworkService reworkService;
    @Resource
    private MesReworkDomainService reworkDomainService;

    @GetMapping("/get")
    @Operation(summary = "获得返工任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:rework:query')")
    public CommonResult<ReworkRespVO> getRework(@RequestParam("id") Long id) {
        return success(reworkService.getRework(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得返工任务分页")
    @PreAuthorize("@ss.hasPermission('mes:rework:query')")
    public CommonResult<PageResult<ReworkRespVO>> getReworkPage(@Valid ReworkPageReqVO pageReqVO) {
        return success(reworkService.getReworkPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出返工任务 Excel")
    @PreAuthorize("@ss.hasPermission('mes:rework:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportReworkExcel(@Valid ReworkPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ReworkRespVO> list = reworkService.getReworkPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-返工任务.xls", "数据", ReworkRespVO.class, list);
    }

    @PostMapping("/release")
    @Operation(summary = "下达返工任务并展开返工工序")
    @PreAuthorize("@ss.hasPermission('mes:rework:release')")
    public CommonResult<Boolean> releaseRework(@Valid @RequestBody ReworkActionReqVO reqVO) {
        reworkDomainService.releaseRework(toActionBO(reqVO));
        return success(true);
    }

    @PostMapping("/finish")
    @Operation(summary = "返工完工并生成复检任务")
    @PreAuthorize("@ss.hasPermission('mes:rework:finish')")
    public CommonResult<Boolean> finishRework(@Valid @RequestBody ReworkActionReqVO reqVO) {
        reworkDomainService.finishRework(toActionBO(reqVO));
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消返工任务")
    @PreAuthorize("@ss.hasPermission('mes:rework:cancel')")
    public CommonResult<Boolean> cancelRework(@Valid @RequestBody ReworkActionReqVO reqVO) {
        reworkDomainService.cancelRework(toActionBO(reqVO));
        return success(true);
    }

    private MesReworkActionBO toActionBO(ReworkActionReqVO reqVO) {
        MesReworkActionBO bo = new MesReworkActionBO();
        bo.setReworkId(reqVO.getId());
        bo.setReason(reqVO.getReason());
        bo.setSourceModule(MesSourceModuleEnum.UI.name());
        bo.setIdempotencyKey(reqVO.getIdempotencyKey());
        return bo;
    }

}
