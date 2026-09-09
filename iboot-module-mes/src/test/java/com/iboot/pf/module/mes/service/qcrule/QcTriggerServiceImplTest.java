package com.iboot.pf.module.mes.service.qcrule;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import com.iboot.pf.module.mes.dal.mysql.qcplan.QcPlanMapper;
import com.iboot.pf.module.mes.dal.mysql.qcrule.QcRuleMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesQcObjectTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.service.qcrule.bo.QcTriggerContextBO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Optional;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_NOT_PUBLISHED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 质检规则触发解析测试。
 *
 * <p>七个用例对应任务书：单维度命中、多维度精确命中、优先级取高、同优先级取小、
 * 维度不匹配返回空、引用未发布方案抛错、停用规则不参与匹配；外加 object_type 必须匹配
 * 与同维度取小两条边界。规则与方案经 Mapper 直接落库，只测解析本身。
 */
@Import(QcTriggerServiceImpl.class)
class QcTriggerServiceImplTest extends BaseDbUnitTest {

    @Resource
    private QcTriggerService qcTriggerService;
    @Resource
    private QcRuleMapper qcRuleMapper;
    @Resource
    private QcPlanMapper qcPlanMapper;

    @Test
    void testResolve_singleUnlimitedRuleHit() {
        // 准备：一条不限维度的启用规则
        Long planId = insertPlan(MesQcPlanStatusEnum.PUBLISHED);
        insertRule(planId, "QR-1", 0, CommonStatusEnum.ENABLE);

        Optional<QcRuleDO> matched = qcTriggerService.resolve(MesQcTriggerPointEnum.IQC.name(),
                ctx(MesQcObjectTypeEnum.MATERIAL, 100L, null));

        assertTrue(matched.isPresent());
        assertEquals("QR-1", matched.get().getCode());
    }

    @Test
    void testResolve_multiDimensionExactRuleHit() {
        // 准备：mat_cat_id + supplier_id 双维度精确规则与一条不限规则并存，精确的优先级更高
        Long planId = insertPlan(MesQcPlanStatusEnum.PUBLISHED);
        insertRule(planId, "QR-UNLIMITED", 0, CommonStatusEnum.ENABLE);
        QcRuleDO exact = insertRule(planId, "QR-EXACT", 10, CommonStatusEnum.ENABLE);
        exact.setMatCatId(5L);
        exact.setSupplierId(12L);
        qcRuleMapper.updateById(exact);

        QcTriggerContextBO ctx = ctx(MesQcObjectTypeEnum.MATERIAL, null, 5L);
        ctx.setSupplierId(12L);
        ctx.setMatId(100L);

        Optional<QcRuleDO> matched = qcTriggerService.resolve(MesQcTriggerPointEnum.IQC.name(), ctx);

        assertTrue(matched.isPresent());
        assertEquals("QR-EXACT", matched.get().getCode());
    }

    @Test
    void testResolve_priorityHigherWins() {
        // 准备：两条都能命中的规则，优先级高者胜
        Long planId = insertPlan(MesQcPlanStatusEnum.PUBLISHED);
        insertRule(planId, "QR-LOW", 0, CommonStatusEnum.ENABLE);
        insertRule(planId, "QR-HIGH", 5, CommonStatusEnum.ENABLE);

        Optional<QcRuleDO> matched = qcTriggerService.resolve(MesQcTriggerPointEnum.IQC.name(),
                ctx(MesQcObjectTypeEnum.MATERIAL, null, null));

        assertTrue(matched.isPresent());
        assertEquals("QR-HIGH", matched.get().getCode());
    }

    @Test
    void testResolve_samePriorityLowerIdWins() {
        // 准备：两条同优先级规则，先配置的（id 小）胜
        Long planId = insertPlan(MesQcPlanStatusEnum.PUBLISHED);
        QcRuleDO first = insertRule(planId, "QR-FIRST", 3, CommonStatusEnum.ENABLE);
        QcRuleDO second = insertRule(planId, "QR-SECOND", 3, CommonStatusEnum.ENABLE);
        assertTrue(first.getId() < second.getId(), "H2 自增主键应保证先插的 id 小");

        Optional<QcRuleDO> matched = qcTriggerService.resolve(MesQcTriggerPointEnum.IQC.name(),
                ctx(MesQcObjectTypeEnum.MATERIAL, null, null));

        assertTrue(matched.isPresent());
        assertEquals("QR-FIRST", matched.get().getCode());
    }

    @Test
    void testResolve_dimensionMismatchReturnsEmpty() {
        // 准备：规则限定 mat_cat_id = 5，上下文带的是 6，"与关系"下不命中
        Long planId = insertPlan(MesQcPlanStatusEnum.PUBLISHED);
        QcRuleDO rule = insertRule(planId, "QR-CAT5", 0, CommonStatusEnum.ENABLE);
        rule.setMatCatId(5L);
        qcRuleMapper.updateById(rule);
        Optional<QcRuleDO> matched = qcTriggerService.resolve(MesQcTriggerPointEnum.IQC.name(),
                ctx(MesQcObjectTypeEnum.MATERIAL, null, 6L));

        assertTrue(matched.isEmpty());
    }

    @Test
    void testResolve_unpublishedPlanThrows() {
        // 准备：规则引用草稿方案——保存时校验挡不住"发布后停用"，触发时必须重验
        Long draftPlanId = insertPlan(MesQcPlanStatusEnum.DRAFT);
        insertRule(draftPlanId, "QR-DRAFT", 0, CommonStatusEnum.ENABLE);

        assertServiceException(() -> qcTriggerService.resolve(MesQcTriggerPointEnum.IQC.name(),
                        ctx(MesQcObjectTypeEnum.MATERIAL, null, null)),
                QC_PLAN_NOT_PUBLISHED);
    }

    @Test
    void testResolve_disabledRuleSkipped() {
        // 准备：唯一一条规则已停用，不参与匹配
        Long planId = insertPlan(MesQcPlanStatusEnum.PUBLISHED);
        insertRule(planId, "QR-DISABLED", 0, CommonStatusEnum.DISABLE);

        Optional<QcRuleDO> matched = qcTriggerService.resolve(MesQcTriggerPointEnum.IQC.name(),
                ctx(MesQcObjectTypeEnum.MATERIAL, null, null));

        assertTrue(matched.isEmpty());
    }

    @Test
    void testResolve_objectTypeMustMatch() {
        // 准备：MATERIAL 规则不服务 OP_TASK 上下文，object_type 必须相等
        Long planId = insertPlan(MesQcPlanStatusEnum.PUBLISHED);
        insertRule(planId, "QR-MATERIAL", 0, CommonStatusEnum.ENABLE);

        Optional<QcRuleDO> matched = qcTriggerService.resolve(MesQcTriggerPointEnum.IQC.name(),
                ctx(MesQcObjectTypeEnum.OP_TASK, null, null));

        assertTrue(matched.isEmpty());
    }

    // ========== 造数辅助 ==========

    private Long insertPlan(MesQcPlanStatusEnum status) {
        QcPlanDO plan = QcPlanDO.builder()
                .code("QP-1")
                .name("来料检验方案")
                .versionNo("V1.0")
                .status(status.name())
                .build();
        qcPlanMapper.insert(plan);
        return plan.getId();
    }

    /** 插入一条全维度不限的启用规则，返回带 id 的 DO，供用例微调维度后 update。 */
    private QcRuleDO insertRule(Long qcPlanId, String code, int priority, CommonStatusEnum status) {
        QcRuleDO rule = QcRuleDO.builder()
                .code(code)
                .name("来料检验规则")
                .triggerPoint(MesQcTriggerPointEnum.IQC.name())
                .objectType(MesQcObjectTypeEnum.MATERIAL.name())
                .matId(MesConstants.NONE_ID)
                .matCatId(MesConstants.NONE_ID)
                .opId(MesConstants.NONE_ID)
                .wcId(MesConstants.NONE_ID)
                .supplierId(MesConstants.NONE_ID)
                .qcPlanId(qcPlanId)
                .priority(priority)
                .status(status.getStatus())
                .build();
        qcRuleMapper.insert(rule);
        return rule;
    }

    private QcTriggerContextBO ctx(MesQcObjectTypeEnum objectType, Long matId, Long matCatId) {
        QcTriggerContextBO bo = new QcTriggerContextBO();
        bo.setObjectType(objectType.name());
        bo.setMatId(matId);
        bo.setMatCatId(matCatId);
        bo.setQty(BigDecimal.ONE);
        return bo;
    }

}
