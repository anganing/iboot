package com.iboot.pf.module.mes.controller.admin.qctask;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.QcTaskSaveReqVO;
import com.iboot.pf.module.mes.service.qctask.QcTaskService;
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

    @PostMapping("/create")
    @Operation(summary = "创建质检任务")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:create')")
    public CommonResult<Long> createQcTask(@Valid @RequestBody QcTaskSaveReqVO createReqVO) {
        return success(qcTaskService.createQcTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新质检任务")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:update')")
    public CommonResult<Boolean> updateQcTask(@Valid @RequestBody QcTaskSaveReqVO updateReqVO) {
        qcTaskService.updateQcTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除质检任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:qc-task:delete')")
    public CommonResult<Boolean> deleteQcTask(@RequestParam("id") Long id) {
        qcTaskService.deleteQcTask(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除质检任务")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:qc-task:delete')")
    public CommonResult<Boolean> deleteQcTaskList(@RequestParam("ids") List<Long> ids) {
        qcTaskService.deleteQcTaskListByIds(ids);
        return success(true);
    }

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

}
