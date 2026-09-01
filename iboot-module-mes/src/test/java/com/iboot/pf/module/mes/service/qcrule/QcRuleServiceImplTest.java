package com.iboot.pf.module.mes.service.qcrule;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRulePageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRuleRespVO;
import com.iboot.pf.module.mes.controller.admin.qcrule.vo.QcRuleSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.qcrule.QcRuleMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesQcModeEnum;
import com.iboot.pf.module.mes.enums.MesQcObjectTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.qcplan.QcPlanService;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.iboot.pf.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RULE_CODE_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RULE_HAS_TASK;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_RULE_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_TASK_PLAN_NOT_PUBLISHED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 质检触发规则 Service 测试：编码自动分配与唯一、只能引用已发布方案、可选匹配维度归零、已生成任务禁删。
 */
@Import(QcRuleServiceImpl.class)
class QcRuleServiceImplTest extends BaseDbUnitTest {

    private static final Long PUBLISHED_PLAN = 1L;
    private static final Long DRAFT_PLAN = 2L;

    @Resource
    private QcRuleService qcRuleService;
    @Resource
    private QcRuleMapper qcRuleMapper;
    @Resource
    private QcTaskMapper qcTaskMapper;

    @MockitoBean
    private QcPlanService qcPlanService;
    @MockitoBean
    private MesCodeService codeService;
    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private MesRelationFiller relationFiller;

    @BeforeEach
    void mockExternalDependencies() {
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "QR0001" : invocation.getArgument(1));
        when(qcPlanService.validateQcPlanExists(eq(PUBLISHED_PLAN))).thenReturn(new QcPlanDO()
                .setId(PUBLISHED_PLAN).setStatus(MesQcPlanStatusEnum.PUBLISHED.name()));
        when(qcPlanService.validateQcPlanExists(eq(DRAFT_PLAN))).thenReturn(new QcPlanDO()
                .setId(DRAFT_PLAN).setStatus(MesQcPlanStatusEnum.DRAFT.name()));
    }

    private QcRuleSaveReqVO buildReqVO(String code) {
        QcRuleSaveReqVO reqVO = new QcRuleSaveReqVO();
        reqVO.setCode(code);
        reqVO.setName("来料检验规则");
        reqVO.setTriggerPoint(MesQcTriggerPointEnum.IQC.name());
        reqVO.setObjectType(MesQcObjectTypeEnum.MATERIAL.name());
        reqVO.setQcPlanId(PUBLISHED_PLAN);
        reqVO.setQcMode(MesQcModeEnum.PROCESS.name());
        reqVO.setPriority(0);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateQcRule_blankCodeFilledAndOptionalDimensionsZeroed() {
        Long id = qcRuleService.createQcRule(buildReqVO(null));

        QcRuleDO qcRule = qcRuleMapper.selectById(id);
        assertNotNull(qcRule);
        assertEquals("QR0001", qcRule.getCode());
        // 可选匹配维度落 0 表示不限，不能留 NULL，否则统计口径失效
        assertEquals(MesConstants.NONE_ID, qcRule.getMatId());
        assertEquals(MesConstants.NONE_ID, qcRule.getMatCatId());
        assertEquals(MesConstants.NONE_ID, qcRule.getOpId());
        assertEquals(MesConstants.NONE_ID, qcRule.getWcId());
        assertEquals(MesConstants.NONE_ID, qcRule.getSupplierId());
    }

    @Test
    void testCreateQcRule_draftPlanRejected() {
        QcRuleSaveReqVO reqVO = buildReqVO("QR-A");
        reqVO.setQcPlanId(DRAFT_PLAN);

        assertServiceException(() -> qcRuleService.createQcRule(reqVO), QC_TASK_PLAN_NOT_PUBLISHED);
    }

    @Test
    void testCreateQcRule_duplicateCodeRejected() {
        qcRuleService.createQcRule(buildReqVO("QR-A"));

        assertServiceException(() -> qcRuleService.createQcRule(buildReqVO("QR-A")), QC_RULE_CODE_DUPLICATE);
    }

    @Test
    void testUpdateQcRule_keepCode() {
        Long id = qcRuleService.createQcRule(buildReqVO("QR-A"));

        QcRuleSaveReqVO updateReqVO = buildReqVO("QR-CHANGED");
        updateReqVO.setId(id);
        updateReqVO.setName("来料检验规则改名");
        qcRuleService.updateQcRule(updateReqVO);

        QcRuleDO qcRule = qcRuleMapper.selectById(id);
        assertEquals("QR-A", qcRule.getCode());
        assertEquals("来料检验规则改名", qcRule.getName());
    }

    @Test
    void testUpdateQcRule_notExists() {
        QcRuleSaveReqVO updateReqVO = buildReqVO("QR-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> qcRuleService.updateQcRule(updateReqVO), QC_RULE_NOT_EXISTS);
    }

    @Test
    void testDeleteQcRule_success() {
        Long id = qcRuleService.createQcRule(buildReqVO("QR-A"));

        qcRuleService.deleteQcRule(id);

        assertNull(qcRuleMapper.selectById(id));
    }

    @Test
    void testDeleteQcRule_hasTaskRejected() {
        Long id = qcRuleService.createQcRule(buildReqVO("QR-A"));
        qcTaskMapper.insert(new QcTaskDO().setRuleId(id).setTaskNo("QCT-0001"));

        assertServiceException(() -> qcRuleService.deleteQcRule(id), QC_RULE_HAS_TASK);
    }

    @Test
    void testDeleteQcRuleList_hasTaskRejected() {
        Long first = qcRuleService.createQcRule(buildReqVO("QR-A"));
        Long second = qcRuleService.createQcRule(buildReqVO("QR-B"));
        qcTaskMapper.insert(new QcTaskDO().setRuleId(second).setTaskNo("QCT-0001"));

        assertServiceException(() -> qcRuleService.deleteQcRuleListByIds(List.of(first, second)), QC_RULE_HAS_TASK);
        assertNotNull(qcRuleMapper.selectById(first));
    }

    @Test
    void testGetQcRulePage() {
        qcRuleService.createQcRule(buildReqVO("QR-A"));

        QcRulePageReqVO reqVO = new QcRulePageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<QcRuleRespVO> list = qcRuleService.getQcRulePage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("QR-A", list.get(0).getCode());
    }

}
