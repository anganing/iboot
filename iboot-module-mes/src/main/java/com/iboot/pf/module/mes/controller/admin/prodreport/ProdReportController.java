package com.iboot.pf.module.mes.controller.admin.prodreport;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportCancelReqVO;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportPageReqVO;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportRespVO;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportSubmitReqVO;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.service.prodreport.MesProdReportDomainService;
import com.iboot.pf.module.mes.service.prodreport.ProdReportService;
import com.iboot.pf.module.mes.service.prodreport.bo.MesCancelReportBO;
import com.iboot.pf.module.mes.service.prodreport.bo.MesSubmitReportBO;
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
 * 生产报工管理端接口：创建即提交，报错走取消冲销，没有改与删。
 *
 * <p><b>报工记录是生产事实</b>。原先的 {@code /update} {@code /delete} {@code /delete-list} 三个端点已删除：
 * 一笔报工的数量已经推进到工序任务、作业工单、生产工单三级，改数量或删记录只改了记录本身，
 * 三级数量不会跟着回退，进度链当场失真且事后无从追查。报错了走 {@code /cancel}：
 * 它会反向扣减三级数量并留下一条负数量的反向流水，冲销这件事本身也是可追溯的。
 *
 * <p>创建走领域 Service 的提交动作，落已提交状态，界面上不留草稿态 —— 草稿态的报工既不推进度也不算产出，
 * 除了让人以为报过工没有任何用处。
 *
 * <p>同样不提供 Excel 导入：一批报工要逐条校验工序任务状态与数量口径，批量灌入等于绕过这些校验。
 */
@Tag(name = "管理后台 - MES-生产报工")
@RestController
@RequestMapping("/mes/prod-report")
@Validated
public class ProdReportController {

    /** codegen Service，只用于列表、详情与导出的展示查询。 */
    @Resource
    private ProdReportService prodReportService;
    /** 领域 Service，负责报工推进链：写记录、留流水、把数量推进到三级。 */
    @Resource
    private MesProdReportDomainService mesProdReportDomainService;

    // ==================== 领域动作 ====================

    @PostMapping("/create")
    @Operation(summary = "提交报工，创建即提交并把数量推进到工序任务与两级工单")
    @PreAuthorize("@ss.hasPermission('mes:prod-report:create')")
    public CommonResult<Long> createProdReport(@Valid @RequestBody ProdReportSubmitReqVO createReqVO) {
        MesSubmitReportBO bo = BeanUtils.toBean(createReqVO, MesSubmitReportBO.class);
        // 来源模块固定 UI；幂等键原样用前端给的，这个动作服务端不派生（见 ProdReportSubmitReqVO 类注释）
        bo.setSourceModule(MesSourceModuleEnum.UI.name());
        bo.setIdempotencyKey(createReqVO.getIdempotencyKey());
        return success(mesProdReportDomainService.submitReport(bo));
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消报工，反向扣减三级数量并写反向流水")
    @PreAuthorize("@ss.hasPermission('mes:prod-report:cancel')")
    public CommonResult<Boolean> cancelProdReport(@Valid @RequestBody ProdReportCancelReqVO reqVO) {
        MesCancelReportBO bo = new MesCancelReportBO();
        bo.setProdReportId(reqVO.getId());
        bo.setReason(reqVO.getReason());
        bo.setSourceModule(MesSourceModuleEnum.UI.name());
        // 取消的键留空即可：领域 Service 按 UI:{报工单号}:CANCEL_PROD_REPORT:0 派生，
        // 同一条报工的取消恒为同一个键，连点两次不会把产量扣成负数
        bo.setIdempotencyKey(reqVO.getIdempotencyKey());
        mesProdReportDomainService.cancelReport(bo);
        return success(true);
    }

    // ==================== 展示查询 ====================

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
