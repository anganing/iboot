package com.iboot.pf.module.mes.controller.admin.optask;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskSaveReqVO;
import com.iboot.pf.module.mes.service.optask.OpTaskService;
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
 * 工序任务管理端接口。
 *
 * <p>工序任务由作业工单按工艺路线派生，不提供 Excel 导入：工序编码、顺序、并行组必须与路线一致，
 * 从 Excel 自由填只会造出跟工艺路线对不上的工序链。
 */
@Tag(name = "管理后台 - MES-工序任务")
@RestController
@RequestMapping("/mes/op-task")
@Validated
public class OpTaskController {

    @Resource
    private OpTaskService opTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建工序任务")
    @PreAuthorize("@ss.hasPermission('mes:op-task:create')")
    public CommonResult<Long> createOpTask(@Valid @RequestBody OpTaskSaveReqVO createReqVO) {
        return success(opTaskService.createOpTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工序任务")
    @PreAuthorize("@ss.hasPermission('mes:op-task:update')")
    public CommonResult<Boolean> updateOpTask(@Valid @RequestBody OpTaskSaveReqVO updateReqVO) {
        opTaskService.updateOpTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工序任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:op-task:delete')")
    public CommonResult<Boolean> deleteOpTask(@RequestParam("id") Long id) {
        opTaskService.deleteOpTask(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除工序任务")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('mes:op-task:delete')")
    public CommonResult<Boolean> deleteOpTaskList(@RequestParam("ids") List<Long> ids) {
        opTaskService.deleteOpTaskListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工序任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:op-task:query')")
    public CommonResult<OpTaskRespVO> getOpTask(@RequestParam("id") Long id) {
        return success(opTaskService.getOpTask(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工序任务分页")
    @PreAuthorize("@ss.hasPermission('mes:op-task:query')")
    public CommonResult<PageResult<OpTaskRespVO>> getOpTaskPage(@Valid OpTaskPageReqVO pageReqVO) {
        return success(opTaskService.getOpTaskPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出工序任务 Excel")
    @PreAuthorize("@ss.hasPermission('mes:op-task:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOpTaskExcel(@Valid OpTaskPageReqVO pageReqVO,
                                  HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OpTaskRespVO> list = opTaskService.getOpTaskPage(pageReqVO).getList();
        ExcelUtils.write(response, "MES-工序任务.xls", "数据", OpTaskRespVO.class, list);
    }

}
