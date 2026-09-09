package com.iboot.pf.module.mes.controller.admin.qcplan;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanActionReqVO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanRespVO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanSaveReqVO;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.service.qcplan.MesQcPlanDomainService;
import com.iboot.pf.module.mes.service.qcplan.QcPlanService;
import com.iboot.pf.module.mes.service.qcplan.bo.MesQcPlanActionBO;
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
 * 检验方案管理端接口。
 *
 * <p>方案是质量主数据，检验项作为子表挂在 /mes/qc-item 下按 planId 维护，这里不提供 Excel 导入。
 */
@Tag(name = "管理后台 - MES-检验方案")
@RestController
@RequestMapping("/mes/qc-plan")
@Validated
public class QcPlanController {

    @Resource
    private QcPlanService qcPlanService;
    @Resource
    private MesQcPlanDomainService qcPlanDomainService;

    // ==================== 领域动作 ====================

    @PostMapping("/publish")
    @Operation(summary = "发布检验方案，发布后进入版本锁定，规则只能引用已发布方案")
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:publish')")
    public CommonResult<Boolean> publishQcPlan(@Valid @RequestBody QcPlanActionReqVO reqVO) {
        qcPlanDomainService.publishPlan(toActionBO(reqVO));
        return success(true);
    }

    @PostMapping("/disable")
    @Operation(summary = "停用检验方案，启用中的质检规则引用着时会拒绝")
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:disable')")
    public CommonResult<Boolean> disableQcPlan(@Valid @RequestBody QcPlanActionReqVO reqVO) {
        qcPlanDomainService.disablePlan(toActionBO(reqVO));
        return success(true);
    }

    // ==================== 内部转换 ====================

    /** 发布、停用共用一个 BO：两个动作的入参相同，目标状态由动作名与当前状态校验决定。 */
    private MesQcPlanActionBO toActionBO(QcPlanActionReqVO reqVO) {
        MesQcPlanActionBO bo = new MesQcPlanActionBO();
        bo.setPlanId(reqVO.getId());
        fillCommand(bo, reqVO.getIdempotencyKey());
        return bo;
    }

    /** 管理后台的动作端点来源固定 UI，幂等键留空时由领域 Service 按方案编码派生。 */
    private void fillCommand(MesCommandReqDTO command, String idempotencyKey) {
        command.setSourceModule(MesSourceModuleEnum.UI.name());
        command.setIdempotencyKey(idempotencyKey);
    }

    @PostMapping("/create")
    @Operation(summary = "创建检验方案")
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:create')")
    public CommonResult<Long> createQcPlan(@Valid @RequestBody QcPlanSaveReqVO createReqVO) {
        return success(qcPlanService.createQcPlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新检验方案")
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:update')")
    public CommonResult<Boolean> updateQcPlan(@Valid @RequestBody QcPlanSaveReqVO updateReqVO) {
        qcPlanService.updateQcPlan(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除检验方案")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:delete')")
    public CommonResult<Boolean> deleteQcPlan(@RequestParam("id") Long id) {
        qcPlanService.deleteQcPlan(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除检验方案")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:delete')")
    public CommonResult<Boolean> deleteQcPlanList(@RequestParam("ids") List<Long> ids) {
        qcPlanService.deleteQcPlanListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得检验方案")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:query')")
    public CommonResult<QcPlanRespVO> getQcPlan(@RequestParam("id") Long id) {
        return success(qcPlanService.getQcPlan(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得检验方案分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:query')")
    public CommonResult<PageResult<QcPlanRespVO>> getQcPlanPage(@Valid QcPlanPageReqVO pageReqVO) {
        return success(qcPlanService.getQcPlanPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出检验方案 Excel")
    @PreAuthorize("@ss.hasPermission('mes:qc-plan:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportQcPlanExcel(@Valid QcPlanPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<QcPlanRespVO> list = qcPlanService.getQcPlanPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-检验方案.xls", "数据", QcPlanRespVO.class, list);
    }

}
