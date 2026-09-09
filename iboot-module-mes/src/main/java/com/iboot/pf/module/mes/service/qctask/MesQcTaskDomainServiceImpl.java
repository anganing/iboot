package com.iboot.pf.module.mes.service.qctask;

import cn.hutool.core.util.StrUtil;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.dataobject.qcresult.QcResultDO;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;
import com.iboot.pf.module.mes.dal.mysql.qcplan.QcPlanMapper;
import com.iboot.pf.module.mes.dal.mysql.qcresult.QcResultMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesCodeRuleConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.qctask.bo.MesCreateQcTaskBO;
import com.iboot.pf.module.mes.service.qcrule.QcTriggerService;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_PLAN_ITEM_EMPTY;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_PLAN_NOT_PUBLISHED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;

/**
 * 质检任务领域动作实现。
 *
 * <p>本类只实现 {@link #createQcTask}：规则命中后的"建任务 + 锁方案版本 + 算抽样 + 展开空行"
 * 四步是同一个事务，任何一步失败整个回滚，不会留下没有检验项的半截任务。
 * 生命周期动作（指派、开始检验、提交结论、取消）由 T2-04 在本类追加。
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
