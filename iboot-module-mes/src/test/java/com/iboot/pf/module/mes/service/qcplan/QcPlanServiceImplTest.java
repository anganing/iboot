package com.iboot.pf.module.mes.service.qcplan;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanRespVO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;
import com.iboot.pf.module.mes.dal.mysql.qcplan.QcPlanMapper;
import com.iboot.pf.module.mes.dal.mysql.qcrule.QcRuleMapper;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcSampleTypeEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static com.iboot.pf.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_AQL_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_CODE_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_HAS_RULE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_SAMPLE_VALUE_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 检验方案 Service 测试：抽样配置跨字段校验、编码与版本唯一、已发布只允许改状态、被规则引用禁删、删除级联清理检验项。
 */
@Import(QcPlanServiceImpl.class)
class QcPlanServiceImplTest extends BaseDbUnitTest {

    @Resource
    private QcPlanService qcPlanService;
    @Resource
    private QcPlanMapper qcPlanMapper;
    @Resource
    private QcItemMapper qcItemMapper;
    @Resource
    private QcRuleMapper qcRuleMapper;

    @MockitoBean
    private MesCodeService codeService;

    @BeforeEach
    void mockExternalDependencies() {
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "QCP0001" : invocation.getArgument(1));
    }

    private QcPlanSaveReqVO buildReqVO(String code, String versionNo) {
        QcPlanSaveReqVO reqVO = new QcPlanSaveReqVO();
        reqVO.setCode(code);
        reqVO.setName("定子外观检验方案");
        reqVO.setVersionNo(versionNo);
        reqVO.setSampleType(MesQcSampleTypeEnum.FULL.name());
        reqVO.setStatus(MesQcPlanStatusEnum.DRAFT.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateQcPlan_blankCodeFilledByRule() {
        Long id = qcPlanService.createQcPlan(buildReqVO(null, "V1.0"));

        QcPlanDO qcPlan = qcPlanMapper.selectById(id);
        assertNotNull(qcPlan);
        assertEquals("QCP0001", qcPlan.getCode());
    }

    @Test
    void testCreateQcPlan_sampleValueRequired() {
        QcPlanSaveReqVO reqVO = buildReqVO("QCP-A", "V1.0");
        reqVO.setSampleType(MesQcSampleTypeEnum.FIXED_QTY.name());

        // 固定数量抽检必须给抽样值，否则生成质检任务时算不出抽样数量
        assertServiceException(() -> qcPlanService.createQcPlan(reqVO), QC_PLAN_SAMPLE_VALUE_REQUIRED);
    }

    @Test
    void testCreateQcPlan_aqlRequired() {
        QcPlanSaveReqVO reqVO = buildReqVO("QCP-A", "V1.0");
        reqVO.setSampleType(MesQcSampleTypeEnum.AQL.name());

        assertServiceException(() -> qcPlanService.createQcPlan(reqVO), QC_PLAN_AQL_REQUIRED);
    }

    @Test
    void testCreateQcPlan_percentWithSampleValueAccepted() {
        QcPlanSaveReqVO reqVO = buildReqVO("QCP-A", "V1.0");
        reqVO.setSampleType(MesQcSampleTypeEnum.PERCENT.name());
        reqVO.setSampleValue(new BigDecimal("10"));

        Long id = qcPlanService.createQcPlan(reqVO);

        assertEquals(0, qcPlanMapper.selectById(id).getSampleValue().compareTo(new BigDecimal("10")));
    }

    @Test
    void testCreateQcPlan_duplicateCodeAndVersionRejected() {
        qcPlanService.createQcPlan(buildReqVO("QCP-A", "V1.0"));

        assertServiceException(() -> qcPlanService.createQcPlan(buildReqVO("QCP-A", "V1.0")),
                QC_PLAN_CODE_DUPLICATE);
    }

    @Test
    void testCreateQcPlan_sameCodeDifferentVersionAccepted() {
        qcPlanService.createQcPlan(buildReqVO("QCP-A", "V1.0"));

        // 编码 + 版本才是唯一键，同编码升版是正常业务
        assertNotNull(qcPlanService.createQcPlan(buildReqVO("QCP-A", "V2.0")));
    }

    @Test
    void testUpdateQcPlan_publishedOnlyStatusAndRemark() {
        QcPlanSaveReqVO createReqVO = buildReqVO("QCP-A", "V1.0");
        createReqVO.setStatus(MesQcPlanStatusEnum.PUBLISHED.name());
        Long id = qcPlanService.createQcPlan(createReqVO);

        QcPlanSaveReqVO updateReqVO = buildReqVO("QCP-A", "V9.9");
        updateReqVO.setId(id);
        updateReqVO.setName("被改掉的名称");
        updateReqVO.setStatus(MesQcPlanStatusEnum.DISABLED.name());
        updateReqVO.setRemark("停用说明");
        qcPlanService.updateQcPlan(updateReqVO);

        QcPlanDO qcPlan = qcPlanMapper.selectById(id);
        // 已发布方案被质检任务锁定引用，只放行状态与备注
        assertEquals(MesQcPlanStatusEnum.DISABLED.name(), qcPlan.getStatus());
        assertEquals("停用说明", qcPlan.getRemark());
        assertEquals("定子外观检验方案", qcPlan.getName());
        assertEquals("V1.0", qcPlan.getVersionNo());
    }

    @Test
    void testUpdateQcPlan_notExists() {
        QcPlanSaveReqVO updateReqVO = buildReqVO("QCP-A", "V1.0");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> qcPlanService.updateQcPlan(updateReqVO), QC_PLAN_NOT_EXISTS);
    }

    @Test
    void testDeleteQcPlan_cascadeQcItem() {
        Long id = qcPlanService.createQcPlan(buildReqVO("QCP-A", "V1.0"));
        QcItemDO qcItem = new QcItemDO().setPlanId(id).setItemCode("IT-01").setItemName("外观");
        qcItemMapper.insert(qcItem);

        qcPlanService.deleteQcPlan(id);

        assertNull(qcPlanMapper.selectById(id));
        // 方案删除后检验项没有归属，必须一起清理
        assertNull(qcItemMapper.selectById(qcItem.getId()));
    }

    @Test
    void testDeleteQcPlan_publishedRejected() {
        QcPlanSaveReqVO createReqVO = buildReqVO("QCP-A", "V1.0");
        createReqVO.setStatus(MesQcPlanStatusEnum.PUBLISHED.name());
        Long id = qcPlanService.createQcPlan(createReqVO);

        assertServiceException(() -> qcPlanService.deleteQcPlan(id), QC_PLAN_STATUS_INVALID);
    }

    @Test
    void testDeleteQcPlan_hasRuleRejected() {
        Long id = qcPlanService.createQcPlan(buildReqVO("QCP-A", "V1.0"));
        qcRuleMapper.insert(new QcRuleDO().setQcPlanId(id).setCode("QR-01").setName("来料检规则"));

        assertServiceException(() -> qcPlanService.deleteQcPlan(id), QC_PLAN_HAS_RULE);
    }

    @Test
    void testDeleteQcPlanList_hasRuleRejected() {
        Long first = qcPlanService.createQcPlan(buildReqVO("QCP-A", "V1.0"));
        Long second = qcPlanService.createQcPlan(buildReqVO("QCP-B", "V1.0"));
        qcRuleMapper.insert(new QcRuleDO().setQcPlanId(second).setCode("QR-01").setName("来料检规则"));

        assertServiceException(() -> qcPlanService.deleteQcPlanListByIds(List.of(first, second)), QC_PLAN_HAS_RULE);
        assertNotNull(qcPlanMapper.selectById(first));
    }

    @Test
    void testGetQcPlanPage() {
        qcPlanService.createQcPlan(buildReqVO("QCP-A", "V1.0"));

        QcPlanPageReqVO reqVO = new QcPlanPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<QcPlanRespVO> list = qcPlanService.getQcPlanPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("QCP-A", list.get(0).getCode());
    }

}
