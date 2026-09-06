package com.iboot.pf.module.mes.controller.admin.optask;

import com.iboot.pf.framework.apilog.core.annotation.ApiAccessLog;
import com.iboot.pf.framework.common.pojo.CommonResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.excel.core.util.ExcelUtils;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskActionReqVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskFinishReqVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskPageReqVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskRespVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskRevokeSkipReqVO;
import com.iboot.pf.module.mes.controller.admin.optask.vo.OpTaskSkipReqVO;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.service.opskiprecord.MesOpSkipDomainService;
import com.iboot.pf.module.mes.service.opskiprecord.bo.MesOpSkipBO;
import com.iboot.pf.module.mes.service.opskiprecord.bo.MesRevokeSkipBO;
import com.iboot.pf.module.mes.service.optask.MesOpTaskDomainService;
import com.iboot.pf.module.mes.service.optask.OpTaskService;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskActionBO;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskFinishBO;
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
 * 工序任务管理端接口：只有动作与查询，没有任何写入型 CRUD。
 *
 * <p><b>工序任务是工单下达的产物，不许手工建、改、删</b>。原先的 {@code /create} {@code /update}
 * {@code /delete} {@code /delete-list} 四个端点已删除，理由不是"用不上"而是"用了就错"：
 * 工序编码、顺序、并行组、前后序串联都由 {@code MesOrderExpandHelper} 按锁定的工艺路线版本展开，
 * 手工加一道工序不会有 {@code prev_task_id} / {@code next_task_id}，后序推进当场断链；
 * 手工改状态会绕过开工门禁、报工累计与完工校验；手工删一道在制工序会让作业工单永远等不到"本段全部完工"。
 * 同理不提供 Excel 导入。codegen 的 {@code OpTaskService} 的建改删方法保留给下达展开等内部调用。
 *
 * <p>状态只能由五个动作推进，每个动作都走状态机裁决 + 乐观锁 + 在制流水 + 命令幂等。
 */
@Tag(name = "管理后台 - MES-工序任务")
@RestController
@RequestMapping("/mes/op-task")
@Validated
public class OpTaskController {

    /** codegen Service，只用于列表、详情与导出的展示查询。 */
    @Resource
    private OpTaskService opTaskService;
    /** 领域 Service，承担全部状态推进：工序任务状态只能由这里改。 */
    @Resource
    private MesOpTaskDomainService mesOpTaskDomainService;
    /** 跳序域，撤销跳序的入口是跳过记录而不是工序任务，因此单独注入。 */
    @Resource
    private MesOpSkipDomainService mesOpSkipDomainService;

    // ==================== 领域动作 ====================

    @PostMapping("/start")
    @Operation(summary = "工序开工，过开工门禁并级联把作业工单与生产工单推到生产中")
    @PreAuthorize("@ss.hasPermission('mes:op-task:start')")
    public CommonResult<Boolean> startOpTask(@Valid @RequestBody OpTaskActionReqVO reqVO) {
        // 门禁不通过时抛的错带着可执行信息（缺料的物料编码与缺口量、未完工工序的顺序与工序名），
        // 这里不做任何捕获、包装或截断：车间要的就是那句话，包一层"开工失败"等于把线索删掉
        mesOpTaskDomainService.startOpTask(toActionBO(reqVO));
        return success(true);
    }

    @PostMapping("/pause")
    @Operation(summary = "工序暂停，生产中推进到已暂停")
    @PreAuthorize("@ss.hasPermission('mes:op-task:pause')")
    public CommonResult<Boolean> pauseOpTask(@Valid @RequestBody OpTaskActionReqVO reqVO) {
        mesOpTaskDomainService.pauseOpTask(toActionBO(reqVO));
        return success(true);
    }

    @PostMapping("/resume")
    @Operation(summary = "工序恢复，已暂停或质量挂起推进回生产中")
    @PreAuthorize("@ss.hasPermission('mes:op-task:resume')")
    public CommonResult<Boolean> resumeOpTask(@Valid @RequestBody OpTaskActionReqVO reqVO) {
        mesOpTaskDomainService.resumeOpTask(toActionBO(reqVO));
        return success(true);
    }

    @PostMapping("/finish")
    @Operation(summary = "工序完工，校验累计数量凑齐后封版并把后序推到待开工")
    @PreAuthorize("@ss.hasPermission('mes:op-task:finish')")
    public CommonResult<Boolean> finishOpTask(@Valid @RequestBody OpTaskFinishReqVO reqVO) {
        MesOpTaskFinishBO bo = new MesOpTaskFinishBO();
        bo.setOpTaskId(reqVO.getId());
        bo.setOperatorId(reqVO.getOperatorId());
        bo.setReason(reqVO.getReason());
        bo.setQualifiedQty(reqVO.getQualifiedQty());
        bo.setScrapQty(reqVO.getScrapQty());
        fillCommand(bo, reqVO.getIdempotencyKey());
        mesOpTaskDomainService.finishOpTask(bo);
        return success(true);
    }

    @PostMapping("/skip")
    @Operation(summary = "工序跳序，按跳序策略分为直接拒绝、立即生效、待审批三路")
    @PreAuthorize("@ss.hasPermission('mes:op-task:skip')")
    public CommonResult<Boolean> skipOpTask(@Valid @RequestBody OpTaskSkipReqVO reqVO) {
        MesOpSkipBO bo = new MesOpSkipBO();
        bo.setOpTaskId(reqVO.getId());
        bo.setOperatorId(reqVO.getOperatorId());
        bo.setReason(reqVO.getReason());
        bo.setSkipReasonType(reqVO.getSkipReasonType());
        bo.setEffectRemark(reqVO.getEffectRemark());
        fillCommand(bo, reqVO.getIdempotencyKey());
        mesOpTaskDomainService.skipOpTask(bo);
        return success(true);
    }

    @PostMapping("/revoke-skip")
    @Operation(summary = "撤销跳序，已生效的把工序退回待开工，待审批的相当于撤回申请")
    @PreAuthorize("@ss.hasPermission('mes:op-task:revoke-skip')")
    public CommonResult<Boolean> revokeSkip(@Valid @RequestBody OpTaskRevokeSkipReqVO reqVO) {
        MesRevokeSkipBO bo = new MesRevokeSkipBO();
        bo.setSkipRecordId(reqVO.getSkipRecordId());
        bo.setReason(reqVO.getReason());
        fillCommand(bo, reqVO.getIdempotencyKey());
        mesOpSkipDomainService.revokeSkip(bo);
        return success(true);
    }

    // ==================== 展示查询 ====================

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

    // ==================== 内部转换 ====================

    /** 开工、暂停、恢复共用一个 BO：三个动作的入参完全相同，目标状态由动作名与状态机决定。 */
    private MesOpTaskActionBO toActionBO(OpTaskActionReqVO reqVO) {
        MesOpTaskActionBO bo = new MesOpTaskActionBO();
        bo.setOpTaskId(reqVO.getId());
        bo.setOperatorId(reqVO.getOperatorId());
        bo.setReason(reqVO.getReason());
        fillCommand(bo, reqVO.getIdempotencyKey());
        return bo;
    }

    /**
     * 补齐命令的来源与幂等键。
     *
     * <p>来源模块固定 {@code UI}：端点是人在界面上点出来的，来源就是后台人工。
     *
     * <p>幂等键原样透传，<b>为空是常态</b>：领域 Service 会按 {@code UI:{工序任务号}:{命令类型}:0} 派生。
     * 确定性派生而不是让前端造键 —— 工序动作是车间现场最容易被连点的一类，键不稳定幂等就等于没有。
     *
     * <p>来源单号留空：领域侧会兜底成工序任务号，比数字主键在命令记录里更可读。
     */
    private void fillCommand(MesCommandReqDTO command, String idempotencyKey) {
        command.setSourceModule(MesSourceModuleEnum.UI.name());
        command.setIdempotencyKey(idempotencyKey);
    }

}
