package com.iboot.pf.module.mes.controller.admin.workorder;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderPageReqVO;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderRespVO;
import com.iboot.pf.module.mes.service.workorder.WorkOrderService;
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
 * 作业工单管理端接口：只有查询，没有任何写入型 CRUD，也没有动作端点。
 *
 * <p><b>作业工单由生产工单下达时按工作中心派生，状态随下属工序任务推进</b>。原先的 {@code /create}
 * {@code /update} {@code /delete} {@code /delete-list} 四个端点已删除：一张作业工单的组织维度、计划数量
 * 都是从生产工单抄下来的，手工建一张不挂任何工序任务的作业工单毫无意义，改它的数量会让它与下属工序任务对不上，
 * 删一张在制的会让工序任务失去归属。同理不提供 Excel 导入。
 * codegen 的 {@code WorkOrderService} 的建改删方法保留给下达展开等内部调用。
 *
 * <p>本单据<b>没有自己的动作端点</b>：{@code PENDING → READY → RUNNING → DONE} 全部由工序任务的开工、
 * 完工动作级联推进（见 {@code MesOpTaskDomainService} 与 {@code MesProgressPropagator}），
 * 给它单独开动作等于给同一个业务事实开第二条写入路径。
 */
@Tag(name = "管理后台 - MES-作业工单")
@RestController
@RequestMapping("/mes/work-order")
@Validated
public class WorkOrderController {

    /** codegen Service，只用于列表、详情与导出的展示查询。 */
    @Resource
    private WorkOrderService workOrderService;

    @GetMapping("/get")
    @Operation(summary = "获得作业工单")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:work-order:query')")
    public CommonResult<WorkOrderRespVO> getWorkOrder(@RequestParam("id") Long id) {
        return success(workOrderService.getWorkOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得作业工单分页")
    @PreAuthorize("@ss.hasPermission('mes:work-order:query')")
    public CommonResult<PageResult<WorkOrderRespVO>> getWorkOrderPage(@Valid WorkOrderPageReqVO pageReqVO) {
        return success(workOrderService.getWorkOrderPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出作业工单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:work-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWorkOrderExcel(@Valid WorkOrderPageReqVO pageReqVO,
                                     HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WorkOrderRespVO> list = workOrderService.getWorkOrderPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-作业工单.xls", "数据", WorkOrderRespVO.class, list);
    }

}
