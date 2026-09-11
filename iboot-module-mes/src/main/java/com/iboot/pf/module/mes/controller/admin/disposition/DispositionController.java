package com.iboot.pf.module.mes.controller.admin.disposition;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionPageReqVO;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionRespVO;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionSaveReqVO;
import com.iboot.pf.module.mes.controller.admin.disposition.vo.DispositionActionReqVO;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.service.disposition.DispositionService;
import com.iboot.pf.module.mes.service.disposition.MesDispositionDomainService;
import com.iboot.pf.module.mes.service.disposition.bo.MesDispositionActionBO;
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
 * 不合格品处置单管理端接口。
 *
 * <p>处置单由质检结论触发，涉及审批与跨模块联动，不提供 Excel 导入。
 */
@Tag(name = "管理后台 - MES-不合格品处置单")
@RestController
@RequestMapping("/mes/disposition")
@Validated
public class DispositionController {

    @Resource
    private DispositionService dispositionService;
    @Resource
    private MesDispositionDomainService dispositionDomainService;

    @PostMapping("/create")
    @Operation(summary = "创建不合格品处置单")
    @PreAuthorize("@ss.hasPermission('mes:disposition:create')")
    public CommonResult<Long> createDisposition(@Valid @RequestBody DispositionSaveReqVO createReqVO) {
        return success(dispositionService.createDisposition(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新不合格品处置单")
    @PreAuthorize("@ss.hasPermission('mes:disposition:update')")
    public CommonResult<Boolean> updateDisposition(@Valid @RequestBody DispositionSaveReqVO updateReqVO) {
        dispositionService.updateDisposition(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除不合格品处置单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:disposition:delete')")
    public CommonResult<Boolean> deleteDisposition(@RequestParam("id") Long id) {
        dispositionService.deleteDisposition(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除不合格品处置单")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:disposition:delete')")
    public CommonResult<Boolean> deleteDispositionList(@RequestParam("ids") List<Long> ids) {
        dispositionService.deleteDispositionListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得不合格品处置单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasAnyPermissions('mes:disposition:query', "
            + "'bpm:process-instance:query', 'bpm:task:query')")
    public CommonResult<DispositionRespVO> getDisposition(@RequestParam("id") Long id) {
        return success(dispositionService.getDisposition(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得不合格品处置单分页")
    @PreAuthorize("@ss.hasPermission('mes:disposition:query')")
    public CommonResult<PageResult<DispositionRespVO>> getDispositionPage(
            @Valid DispositionPageReqVO pageReqVO) {
        return success(dispositionService.getDispositionPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出不合格品处置单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:disposition:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDispositionExcel(@Valid DispositionPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DispositionRespVO> list = dispositionService.getDispositionPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-不合格品处置单.xls", "数据", DispositionRespVO.class, list);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交处置单")
    @PreAuthorize("@ss.hasPermission('mes:disposition:submit')")
    public CommonResult<Boolean> submitDisposition(@Valid @RequestBody DispositionActionReqVO reqVO) {
        dispositionDomainService.submitDisposition(toActionBO(reqVO));
        return success(true);
    }

    @PostMapping("/execute")
    @Operation(summary = "执行处置单")
    @PreAuthorize("@ss.hasPermission('mes:disposition:execute')")
    public CommonResult<Boolean> executeDisposition(@Valid @RequestBody DispositionActionReqVO reqVO) {
        dispositionDomainService.executeDisposition(toActionBO(reqVO));
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消处置单")
    @PreAuthorize("@ss.hasPermission('mes:disposition:cancel')")
    public CommonResult<Boolean> cancelDisposition(@Valid @RequestBody DispositionActionReqVO reqVO) {
        dispositionDomainService.cancelDisposition(toActionBO(reqVO));
        return success(true);
    }

    private MesDispositionActionBO toActionBO(DispositionActionReqVO reqVO) {
        MesDispositionActionBO bo = new MesDispositionActionBO();
        bo.setDispositionId(reqVO.getId());
        bo.setSourceModule(MesSourceModuleEnum.UI.name());
        bo.setIdempotencyKey(reqVO.getIdempotencyKey());
        return bo;
    }

}
