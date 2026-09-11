package com.iboot.pf.module.mes.controller.admin.qctask;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskActionReqVO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskAssignReqVO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskConclusionReqVO;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.service.qctask.MesQcTaskDomainService;
import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskActionBO;
import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskConclusionBO;
import com.iboot.pf.module.mes.service.qctask.QcTaskService;
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
 * 质检任务管理端接口。
 *
 * <p>任务由质检规则命中触发或人工建单，不提供 Excel 导入。
 */
@Tag(name = "管理后台 - MES-质检任务")
@RestController
@RequestMapping("/mes/qc-task")
@Validated
public class QcTaskController {

    @Resource
    private QcTaskService qcTaskService;
    @Resource
    private MesQcTaskDomainService qcTaskDomainService;

    @GetMapping("/get")
    @Operation(summary = "获得质检任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:query')")
    public CommonResult<QcTaskRespVO> getQcTask(@RequestParam("id") Long id) {
        return success(qcTaskService.getQcTask(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得质检任务分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:query')")
    public CommonResult<PageResult<QcTaskRespVO>> getQcTaskPage(@Valid QcTaskPageReqVO pageReqVO) {
        return success(qcTaskService.getQcTaskPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出质检任务 Excel")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportQcTaskExcel(@Valid QcTaskPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<QcTaskRespVO> list = qcTaskService.getQcTaskPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-质检任务.xls", "数据", QcTaskRespVO.class, list);
    }

    @PostMapping("/assign")
    @Operation(summary = "指派检验员")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:assign')")
    public CommonResult<Boolean> assignQcTask(@Valid @RequestBody QcTaskAssignReqVO reqVO) {
        MesQcTaskActionBO bo = new MesQcTaskActionBO();
        bo.setTaskId(reqVO.getId());
        bo.setInspectorId(reqVO.getInspectorId());
        bo.setRemark(reqVO.getRemark());
        fillCommand(bo, reqVO.getIdempotencyKey());
        qcTaskDomainService.assignQcTask(bo);
        return success(true);
    }

    @PostMapping("/start-inspect")
    @Operation(summary = "开始检验")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:start-inspect')")
    public CommonResult<Boolean> startInspect(@Valid @RequestBody QcTaskActionReqVO reqVO) {
        MesQcTaskActionBO bo = new MesQcTaskActionBO();
        bo.setTaskId(reqVO.getId());
        bo.setRemark(reqVO.getRemark());
        fillCommand(bo, reqVO.getIdempotencyKey());
        qcTaskDomainService.startInspect(bo);
        return success(true);
    }

    @PostMapping("/submit-conclusion")
    @Operation(summary = "提交检验结论")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:submit-conclusion')")
    public CommonResult<Boolean> submitConclusion(@Valid @RequestBody QcTaskConclusionReqVO reqVO) {
        MesQcTaskConclusionBO bo = new MesQcTaskConclusionBO();
        bo.setTaskId(reqVO.getId());
        bo.setRemark(reqVO.getRemark());
        bo.setConclusion(reqVO.getConclusion());
        bo.setQtyQualified(reqVO.getQtyQualified());
        bo.setQtyRejected(reqVO.getQtyRejected());
        fillCommand(bo, reqVO.getIdempotencyKey());
        qcTaskDomainService.submitConclusion(bo);
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消质检任务")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:cancel')")
    public CommonResult<Boolean> cancelQcTask(@Valid @RequestBody QcTaskActionReqVO reqVO) {
        MesQcTaskActionBO bo = new MesQcTaskActionBO();
        bo.setTaskId(reqVO.getId());
        bo.setRemark(reqVO.getRemark());
        fillCommand(bo, reqVO.getIdempotencyKey());
        qcTaskDomainService.cancelQcTask(bo);
        return success(true);
    }

    private void fillCommand(MesQcTaskActionBO bo, String idempotencyKey) {
        bo.setSourceModule(MesSourceModuleEnum.UI.name());
        bo.setIdempotencyKey(idempotencyKey);
    }

}
