package com.iboot.pf.module.mes.service.qctask;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.enums.MesQcItemConclusionEnum;
import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;
import com.iboot.pf.module.mes.dal.mysql.qcplan.QcPlanMapper;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStateMachine;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.service.MesBusinessRules;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandService;
import com.iboot.pf.module.mes.service.qctask.bo.MesCreateQcTaskBO;
import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskActionBO;
import com.iboot.pf.module.mes.service.qctask.bo.MesQcTaskConclusionBO;
import com.iboot.pf.module.mes.service.qcrule.QcTriggerService;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_CONCURRENT_CONFLICT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_PLAN_ITEM_EMPTY;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_PLAN_NOT_PUBLISHED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;

/**
 * 质检任务领域动作实现。
 *
 * <p>{@link #createQcTask} 负责任务怎么来：规则命中后的"建任务 + 锁方案版本 + 算抽样 + 展开空行"
 * 四步是同一个事务，任何一步失败整个回滚，不会留下没有检验项的半截任务。
 *
 * <p>四个生命周期动作（指派、开始检验、提交结论、取消）共用一套骨架：命令幂等 -&gt; 状态机裁决
 * 目标状态 -&gt; 带乐观锁条件落库，与工序任务域动作同构。目标状态一律由状态机给出，方法签名里只有
 * 动作名。提交结论末尾预留按触发点分派 Handler 的钩子位，五个检验点的下游联动由 T2-07 ~ T2-11 接入。
 *
 * @author iboot
 */
@Slf4j
@Service
public class MesQcTaskDomainServiceImpl implements MesQcTaskDomainService {

    /** 幂等键的段分隔符，与 08 文档幂等键全表的格式一致。 */
    private static final String KEY_SEPARATOR = ":";

    /**
     * 预生成检验结果空行的数量上限。
     *
     * <p>全检 1 万件 × 10 个检验项就是 10 万行空行，插入耗时会拖垮触发它的业务动作。
     * 超限时只生成每个检验项的第一个样本，实测值先录第一件、后续样本由结论提交时按项汇总。
     */
    private static final int MAX_PRE_GENERATED_ROWS = 500;

    /** 幂等键命令段，创建型动作固定走列判重，不进 {@code MesCommandService}。 */
    private static final String COMMAND_QC_CREATE = "QC_CREATE";

    /** 任务级动作没有行号，幂等键第四段固定 0。 */
    private static final String WHOLE_TASK_LINE_NO = "0";

    @Resource
    private QcTriggerService qcTriggerService;
    @Resource
    private QcTaskMapper qcTaskMapper;
    @Resource
    private QcResultMapper qcResultMapper;
    @Resource
    private QcPlanMapper qcPlanMapper;
    @Resource
    private QcItemMapper qcItemMapper;
    @Resource
    private MesCodeService codeService;
    @Resource
    private MesSnapshotService snapshotService;
    @Resource
    private MesCommandService commandService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Optional<Long> createQcTask(MesCreateQcTaskBO bo) {
        validateQty(bo.getQty());
        Optional<QcRuleDO> ruleOpt = qcTriggerService.resolve(bo.getTriggerPoint(), bo);
        if (ruleOpt.isEmpty()) {
            // 未命中规则也是规则的一种答案：业务动作直接放行，打 info 留痕方便事后解释"为什么没检"
            log.info("[createQcTask] 规则未命中，业务动作放行。triggerPoint={}, bizNo={}",
                    bo.getTriggerPoint(), bo.getBizNo());
            return Optional.empty();
        }
        QcRuleDO rule = ruleOpt.get();

        // 幂等：同业务单同触发点重复触发直接返回首次任务，不产生第二条
        String idempotencyKey = buildIdempotencyKey(bo.getBizNo(), bo.getTriggerPoint());
        QcTaskDO existed = qcTaskMapper.selectByIdempotencyKey(idempotencyKey);
        if (existed != null) {
            log.info("[createQcTask] 幂等命中，返回既有任务 {}。idempotencyKey={}", existed.getTaskNo(), idempotencyKey);
            return Optional.of(existed.getId());
        }

        // 方案在 resolve 里校验过已发布，这里重查一次取抽样配置与版本：规则校验与任务生成之间
        // 方案可能被停用，任务必须锁它此刻的版本快照
        QcPlanDO plan = qcPlanMapper.selectById(rule.getQcPlanId());
        if (plan == null || !MesQcPlanStatusEnum.isPublished(plan.getStatus())) {
            throw exception(QC_TASK_PLAN_NOT_PUBLISHED);
        }

        BigDecimal sampleQty = MesQcSampleCalculator.calc(plan, bo.getQty());

        QcTaskDO qcTask = buildQcTask(bo, rule, plan, sampleQty, idempotencyKey);
        qcTaskMapper.insert(qcTask);

        List<QcItemDO> items = selectEnabledItems(plan.getId());
        if (items.isEmpty()) {
            // 已发布方案被清空检验项属于配置事故，空任务没有判定依据，直接报错回滚
            throw exception(QC_TASK_PLAN_ITEM_EMPTY, plan.getId());
        }
        boolean truncated = insertResultPlaceholders(qcTask, items, sampleQty);
        if (truncated) {
            // 降级生成要留痕：不标注的话没人知道这个任务的空行不全，结论汇总口径会算错
            String degradedRemark = StrUtil.isBlank(bo.getRemark()) ? "" : bo.getRemark() + "；";
            qcTaskMapper.updateById(new QcTaskDO().setId(qcTask.getId()).setRemark(degradedRemark
                    + "送检数量较大，检验结果空行仅预生成第 1 个样本，其余样本由结论提交时按项汇总"));
        }
        return Optional.of(qcTask.getId());
    }

    /** 送检数量必须大于零：零数量与负数量的检验没有意义。 */
    private void validateQty(BigDecimal qty) {
        if (qty == null || qty.signum() <= 0) {
            throw exception(QTY_MUST_BE_POSITIVE);
        }
    }

    // ========== 生命周期动作 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignQcTask(MesQcTaskActionBO bo) {
        Assert.notNull(bo.getInspectorId(), "指派检验员不能为空");
        QcTaskDO task = requireQcTask(bo.getTaskId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.ASSIGN_QC_TASK, task.getTaskNo());
        commandService.executeVoid(MesCommandTypeEnum.ASSIGN_QC_TASK, command,
                () -> {
                    String toStatus = MesQcTaskStateMachine.next(task.getStatus(),
                            MesQcTaskStateMachine.ACTION_ASSIGN);
                    advanceQcTask(task, QcTaskDO.builder()
                            .status(toStatus)
                            .assignedInspectorId(bo.getInspectorId())
                            .build());
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startInspect(MesQcTaskActionBO bo) {
        QcTaskDO task = requireQcTask(bo.getTaskId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.START_INSPECT, task.getTaskNo());
        commandService.executeVoid(MesCommandTypeEnum.START_INSPECT, command,
                () -> {
                    String toStatus = MesQcTaskStateMachine.next(task.getStatus(),
                            MesQcTaskStateMachine.ACTION_START_INSPECT);
                    advanceQcTask(task, QcTaskDO.builder()
                            .status(toStatus)
                            .inspectStartTime(LocalDateTime.now())
                            .build());
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitConclusion(MesQcTaskConclusionBO bo) {
        QcTaskDO task = requireQcTask(bo.getTaskId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.SUBMIT_CONCLUSION, task.getTaskNo());
        commandService.executeVoid(MesCommandTypeEnum.SUBMIT_CONCLUSION, command,
                () -> {
                    String fromStatus = task.getStatus();
                    // 数量校验在状态推进之前：先落 DONE 再发现数量对不上，DONE 是终态没有回退边
                    MesBusinessRules.validateQualityQty(task.getSampleQty(),
                            bo.getQtyQualified(), bo.getQtyRejected());
                    String toStatus = MesQcTaskStateMachine.next(fromStatus,
                            MesQcTaskStateMachine.ACTION_SUBMIT_CONCLUSION);
                    advanceQcTask(task, QcTaskDO.builder()
                            .status(toStatus)
                            .inspectEndTime(LocalDateTime.now())
                            .conclusion(bo.getConclusion())
                            .qtyQualified(bo.getQtyQualified())
                            .qtyRejected(bo.getQtyRejected())
                            .build());

                    // TODO T2-07 ~ T2-11 按 task.getTriggerPoint() 分派五个检验点的结果 Handler
                    //  （IQC/FAI/IPQC/FQC/OQC 各自的 onXxxDone），放行或拦截下游动作。
                    //  分派要覆盖 CONCESSION（让步接收走审批，通过前按各自检验点的口径处理），
                    //  REJECTED 不在分派里放行——强制等处置单。本任务只预留钩子位。
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelQcTask(MesQcTaskActionBO bo) {
        QcTaskDO task = requireQcTask(bo.getTaskId());
        MesCommandReqDTO command = buildCommand(bo, MesCommandTypeEnum.CANCEL_QC_TASK, task.getTaskNo());
        commandService.executeVoid(MesCommandTypeEnum.CANCEL_QC_TASK, command,
                () -> {
                    String toStatus = MesQcTaskStateMachine.next(task.getStatus(),
                            MesQcTaskStateMachine.ACTION_CANCEL);
                    QcTaskDO.QcTaskDOBuilder builder = QcTaskDO.builder().status(toStatus);
                    if (StrUtil.isNotBlank(bo.getRemark())) {
                        builder.remark(bo.getRemark());
                    }
                    advanceQcTask(task, builder.build());
                });
    }

    /**
     * 带乐观锁条件推进质检任务，并把新版本号同步回内存对象。
     *
     * <p>质检任务与工序任务同属并发热点：结果录入（T2-05 自动推进 INSPECTING -&gt; RESULT_POSTING）
     * 与检验员手工动作可能同时到达。少了 {@code WHERE version = 读到的版本}，并发提交会各自基于
     * 同一份旧值写回，后写的把前写的整条覆盖掉。
     */
    private void advanceQcTask(QcTaskDO task, QcTaskDO update) {
        int currentVersion = task.getVersion() == null ? MesConstants.INIT_VERSION : task.getVersion();
        update.setVersion(currentVersion + 1);
        LambdaUpdateWrapper<QcTaskDO> wrapper = new LambdaUpdateWrapper<QcTaskDO>()
                .eq(QcTaskDO::getId, task.getId())
                .eq(QcTaskDO::getVersion, currentVersion);
        if (qcTaskMapper.update(update, wrapper) == 0) {
            throw exception(QC_TASK_CONCURRENT_CONFLICT);
        }
        task.setVersion(currentVersion + 1);
    }

    /**
     * 组装幂等命令。
     *
     * <p>幂等键调用方给了以它为准，留空时按四段格式 {@code {来源模块}:{质检任务号}:{命令类型}:0}
     * 派生，与工序任务域的幂等键格式一致。动作段取 {@link MesCommandTypeEnum} 的枚举名而不是字面量：
     * 键会落进 {@code mes_command_record}，字面量拼出来的历史键在命令改名后就与枚举对不上。
     */
    private MesCommandReqDTO buildCommand(MesQcTaskActionBO source, MesCommandTypeEnum commandType, String taskNo) {
        // 来源模块是幂等键的第一段，也是排查"这条命令谁下的"的唯一线索，漏传属于调用方契约违约
        Assert.notBlank(source.getSourceModule(), "来源模块不能为空，它是幂等键的第一段");
        QcTaskCommand command = new QcTaskCommand();
        command.setSourceModule(source.getSourceModule());
        command.setSourceBizNo(StrUtil.isBlank(source.getSourceBizNo()) ? taskNo : source.getSourceBizNo());
        command.setSourceBizLineNo(source.getSourceBizLineNo());
        command.setIdempotencyKey(StrUtil.isBlank(source.getIdempotencyKey())
                ? String.join(KEY_SEPARATOR, source.getSourceModule(), taskNo,
                commandType.name(), WHOLE_TASK_LINE_NO)
                : source.getIdempotencyKey());
        return command;
    }

    /**
     * 幂等命令载体。
     *
     * <p>{@link MesCommandReqDTO} 是抽象类，质检任务域给它一个最小实现：既不必新建公共 DTO，
     * 也不用把派生出来的幂等键写回调用方的 BO——改调用方传进来的对象是隐蔽的副作用。
     */
    private static class QcTaskCommand extends MesCommandReqDTO {
    }

    private QcTaskDO requireQcTask(Long id) {
        QcTaskDO task = id == null ? null : qcTaskMapper.selectById(id);
        if (task == null) {
            throw exception(QC_TASK_NOT_EXISTS);
        }
        return task;
    }

    /** 组装任务：锁方案版本、快照物料与批次、落幂等键与初始状态。 */
    private QcTaskDO buildQcTask(MesCreateQcTaskBO bo, QcRuleDO rule,
                                 QcPlanDO plan, BigDecimal sampleQty, String idempotencyKey) {
        MaterialRespDTO material = snapshotService.requireMaterial(bo.getMatId());
        return QcTaskDO.builder()
                .taskNo(codeService.generate(MesCodeRuleConstants.QC_TASK, null))
                .ruleId(rule.getId())
                .qcPlanId(plan.getId())
                .qcPlanVersion(plan.getVersionNo())
                .triggerPoint(bo.getTriggerPoint())
                .bizType(bo.getBizType())
                .bizNo(bo.getBizNo())
                .bizLineId(nvlId(bo.getBizLineId()))
                .matId(bo.getMatId())
                .matCode(material.getCode())
                .matName(material.getName())
                .matSpec(material.getSpec())
                .batchId(nvlId(bo.getBatchId()))
                .lotNo(snapshotService.lotNoOrNull(nvlId(bo.getBatchId())))
                .qty(bo.getQty())
                .sampleQty(sampleQty)
                .uomId(nvlId(bo.getUomId()))
                .uomCode(snapshotService.requireUomCode(nvlId(bo.getUomId())))
                .qtyQualified(MesConstants.ZERO_QTY)
                .qtyRejected(MesConstants.ZERO_QTY)
                // 指派检验员初始为 0（未指派），指派动作由 T2-04 负责
                .assignedInspectorId(MesConstants.NONE_ID)
                .status(MesQcTaskStatusEnum.CREATED.name())
                .idempotencyKey(idempotencyKey)
                .version(MesConstants.INIT_VERSION)
                .remark(bo.getRemark())
                .build();
    }

    /** 取方案下启用的检验项，按 sortNo 升序：展开顺序决定检验结果空行的生成顺序。 */
    private List<QcItemDO> selectEnabledItems(Long planId) {
        List<QcItemDO> items = qcItemMapper.selectList(QcItemDO::getPlanId, planId);
        return items.stream()
                .filter(item -> CommonStatusEnum.ENABLE.getStatus().equals(item.getStatus()))
                .sorted(Comparator.comparing(QcItemDO::getSortNo, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /**
     * 预生成检验结果空行。
     *
     * <p>四列 NOT NULL 无默认值的列（pass_flag/inspector_id/inspect_time/conclusion）必须落初始值：
     * 生产 MySQL 严格模式下缺一即插入失败。未检验的格子按 FAIL（pass_flag=false）占位，
     * 与"未检验不算合格"的保守口径一致，录入时逐格覆盖。
     *
     * @return 是否触发了降级（空行数超过上限，只生成每个检验项的第 1 个样本）
     */
    private boolean insertResultPlaceholders(QcTaskDO qcTask, List<QcItemDO> items, BigDecimal sampleQty) {
        int itemCount = items.size();
        long fullRows = itemCount * sampleQty.longValue();
        boolean truncated = fullRows > MAX_PRE_GENERATED_ROWS;
        int sampleCount = truncated ? 1 : sampleQty.intValue();
        List<QcResultDO> results = new ArrayList<>(itemCount * sampleCount);
        for (QcItemDO item : items) {
            for (int sampleNo = 1; sampleNo <= sampleCount; sampleNo++) {
                results.add(QcResultDO.builder()
                        .taskId(qcTask.getId())
                        .itemId(item.getId())
                        .sampleNo(sampleNo)
                        .itemCode(item.getItemCode())
                        .itemName(item.getItemName())
                        .passFlag(false)
                        .inspectorId(0L)
                        .inspectTime(qcTask.getCreateTime())
                        .conclusion(MesQcItemConclusionEnum.FAIL.name())
                        .build());
            }
        }
        qcResultMapper.insertBatch(results);
        return truncated;
    }

    /** 幂等键格式 {@code MES:{bizNo}:QC_CREATE:{triggerPoint}}，见任务书 T2-02。 */
    private String buildIdempotencyKey(String bizNo, String triggerPoint) {
        return String.join(KEY_SEPARATOR, "MES", bizNo, COMMAND_QC_CREATE, triggerPoint);
    }

    /** 可选维度落 0 不落 null，与库表 NOT NULL DEFAULT 0 的口径一致。 */
    private Long nvlId(Long id) {
        return id == null ? MesConstants.NONE_ID : id;
    }

}
