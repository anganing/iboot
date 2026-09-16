package com.iboot.pf.server.integration.fixture;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemSaveReqVO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanSaveReqVO;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRuleSaveReqVO;
import com.iboot.pf.module.mes.enums.MesQcItemTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcModeEnum;
import com.iboot.pf.module.mes.enums.MesQcObjectTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcSampleTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.service.qcitem.QcItemService;
import com.iboot.pf.module.mes.service.qcplan.MesQcPlanDomainService;
import com.iboot.pf.module.mes.service.qcplan.QcPlanService;
import com.iboot.pf.module.mes.service.qcplan.bo.MesQcPlanActionBO;
import com.iboot.pf.module.mes.service.qcrule.QcRuleService;
import com.iboot.pf.server.integration.PilotLine;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 质量基础数据工厂（T6-01）。
 *
 * 建一个检验方案 + 若干检验项，**经 {@link MesQcPlanDomainService#publishPlan} 发布**
 * （规则只认已发布方案，草稿建规则会抛 {@code QC_TASK_PLAN_NOT_PUBLISHED}），
 * 再按五个检验点各建一条质检触发规则。
 *
 * <p>检验点的覆盖按各触发点能匹配的维度来定：IQC 按物料（子件）、
 * IPQC/FAI 按工序、FQC 按物料（成品）、OQC 按物料（成品）。
 *
 * @author iboot
 */
@Component
public class QcFixture {

    @Resource
    private QcPlanService qcPlanService;
    @Resource
    private QcItemService qcItemService;
    @Resource
    private QcRuleService qcRuleService;
    @Resource
    private MesQcPlanDomainService qcPlanDomainService;

    /**
     * 建检验方案、检验项与五个检验点的质检规则。
     *
     * @param line 试点产线句柄（用其 fgMatId / 子件 / 工序 / 工作中心）
     * @return 检验方案编号、检验项编号列表与规则编号列表
     */
    public QcBasics createQcBasics(PilotLine line) {
        // 1. 检验方案（建出来一定是草稿，发布要单独走 domain 动作）
        QcPlanSaveReqVO plan = new QcPlanSaveReqVO();
        plan.setCode("IT-QCPLAN"); // mes_qc_plan.code 为 NOT NULL，必填
        plan.setName("集成测试检验方案");
        plan.setVersionNo("V1.0");
        plan.setSampleType(MesQcSampleTypeEnum.FULL.name());
        plan.setStatus("DRAFT"); // 实际状态由 Service 强制落草稿，这里给个合法值过校验
        Long qcPlanId = qcPlanService.createQcPlan(plan);

        // 2. 检验项：数值型 + 文本型各一，覆盖两类判定
        List<Long> qcItemIds = new ArrayList<>();
        qcItemIds.add(createItem(qcPlanId, "DIM-01", "外径", MesQcItemTypeEnum.NUMERIC.name(),
                "10.00", new BigDecimal("9.90"), new BigDecimal("10.10"), true, 10));
        qcItemIds.add(createItem(qcPlanId, "APP-01", "外观", MesQcItemTypeEnum.TEXT.name(),
                "无划伤", null, null, true, 20));

        // 3. 发布（空方案发布了也展开不出检验项，Service 会拦，故必须在建完检验项之后）
        MesQcPlanActionBO publish = new MesQcPlanActionBO();
        publish.setPlanId(qcPlanId);
        // sourceModule 为 NOT NULL 且无默认值（控制器固定传 UI），fixture 走集成测试来源
        publish.setSourceModule(MesSourceModuleEnum.INTEGRATION.name());
        publish.setIdempotencyKey("INTEGRATION:T6-01-SMOKE:PUBLISH_QC_PLAN:" + qcPlanId);
        qcPlanDomainService.publishPlan(publish);

        // 4. 五个检验点各一条规则，全部指向同一方案
        List<Long> qcRuleIds = new ArrayList<>();
        // IQC 来料检验：按子件物料匹配
        qcRuleIds.add(createRule(line, qcPlanId, MesQcTriggerPointEnum.IQC, MesQcModeEnum.PRE_INBOUND,
                line.anyComponentMatId(), null));
        // IPQC 过程检验：按首道工序匹配
        qcRuleIds.add(createRule(line, qcPlanId, MesQcTriggerPointEnum.IPQC, MesQcModeEnum.PROCESS,
                null, line.opIds().get(0)));
        // FQC 完工检验：按成品物料匹配
        qcRuleIds.add(createRule(line, qcPlanId, MesQcTriggerPointEnum.FQC, MesQcModeEnum.BEFORE_COMPLETE,
                line.fgMatId(), null));
        // OQC 出货检验：按成品物料匹配
        qcRuleIds.add(createRule(line, qcPlanId, MesQcTriggerPointEnum.OQC, MesQcModeEnum.BEFORE_COMPLETE,
                line.fgMatId(), null));
        // FAI 首件检验：按首道工序匹配，开工后即生成任务
        qcRuleIds.add(createRule(line, qcPlanId, MesQcTriggerPointEnum.FAI, MesQcModeEnum.FIRST_ARTICLE,
                null, line.opIds().get(0)));

        return new QcBasics(qcPlanId, qcItemIds, qcRuleIds);
    }

    private Long createItem(Long planId, String itemCode, String itemName, String itemType,
                            String stdValue, BigDecimal lowerLimit, BigDecimal upperLimit,
                            boolean requiredFlag, int sortNo) {
        QcItemSaveReqVO item = new QcItemSaveReqVO();
        item.setPlanId(planId);
        item.setItemCode(itemCode);
        item.setItemName(itemName);
        item.setItemType(itemType);
        item.setStdValue(stdValue);
        item.setLowerLimit(lowerLimit);
        item.setUpperLimit(upperLimit);
        item.setRequiredFlag(requiredFlag);
        item.setSortNo(sortNo);
        item.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return qcItemService.createQcItem(item);
    }

    /**
     * 建一条质检触发规则。
     *
     * <p>物料与工序二选一：给 matId 表示按物料匹配（分类/工序/工作中心/供应商都传 null → 0 不限），
     * 给 opId 表示按工序匹配。
     */
    private Long createRule(PilotLine line, Long qcPlanId, MesQcTriggerPointEnum triggerPoint,
                            MesQcModeEnum qcMode, Long matId, Long opId) {
        QcRuleSaveReqVO rule = new QcRuleSaveReqVO();
        rule.setCode("IT-QCRULE-" + triggerPoint.name()); // mes_qc_rule.code 为 NOT NULL，必填
        rule.setName("集成测试质检规则-" + triggerPoint.name());
        rule.setTriggerPoint(triggerPoint.name());
        // 按工序匹配的规则检验对象是工序任务，按物料匹配的是物料
        rule.setObjectType(opId == null
                ? MesQcObjectTypeEnum.MATERIAL.name()
                : MesQcObjectTypeEnum.OP_TASK.name());
        rule.setMatId(matId);
        rule.setOpId(opId);
        rule.setQcPlanId(qcPlanId);
        rule.setQcMode(qcMode.name());
        rule.setPriority(0);
        rule.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return qcRuleService.createQcRule(rule);
    }

    /** 质量基础数据建立结果：供用例回填 {@link PilotLine} 的质检段或直接断言。 */
    public record QcBasics(Long qcPlanId, List<Long> qcItemIds, List<Long> qcRuleIds) {
    }

}
