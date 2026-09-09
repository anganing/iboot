package com.iboot.pf.module.mes.service.qcplan;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemSaveReqVO;
import com.iboot.pf.module.mes.controller.admin.qcplan.vo.QcPlanSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.dataobject.qcrule.QcRuleDO;
import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;
import com.iboot.pf.module.mes.dal.mysql.qcplan.QcPlanMapper;
import com.iboot.pf.module.mes.dal.mysql.qcrule.QcRuleMapper;
import com.iboot.pf.module.mes.enums.MesQcItemTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcSampleTypeEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandServiceImpl;
import com.iboot.pf.module.mes.service.qcitem.QcItemService;
import com.iboot.pf.module.mes.service.qcitem.QcItemServiceImpl;
import com.iboot.pf.module.mes.service.qcplan.bo.MesQcPlanActionBO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_ITEM_PLAN_NOT_DRAFT;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_IN_USE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_PUBLISH_WITHOUT_ITEM;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_PLAN_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 检验方案领域动作测试：发布校验检验项、发布后方案与检验项整体锁定（改方案、增改检验项全部拒绝）、
 * 停用时被启用规则引用拒绝、动作幂等。
 *
 * <p>锁定效果是跨 Service 的联动：发布只发生在领域 Service，改方案与改检验项被拦在 codegen
 * Service 的状态校验上，所以这里把三者和命令幂等一起拉起来测端到端。
 */
@Import({MesQcPlanDomainServiceImpl.class, MesCommandServiceImpl.class,
        QcPlanServiceImpl.class, QcItemServiceImpl.class})
class MesQcPlanDomainServiceImplTest extends BaseDbUnitTest {

    private static final String SOURCE_MODULE = "UI";

    @Resource
    private MesQcPlanDomainService qcPlanDomainService;
    @Resource
    private QcPlanService qcPlanService;
    @Resource
    private QcItemService qcItemService;
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

    /** 建一条指定状态的方案，直接插 Mapper，动作测试只关心状态推进不关心建单校验。 */
    private Long insertPlan(String code, String status) {
        QcPlanDO plan = QcPlanDO.builder().code(code).name("定子外观检验方案")
                .versionNo("V1.0").sampleType(MesQcSampleTypeEnum.FULL.name()).status(status).build();
        qcPlanMapper.insert(plan);
        return plan.getId();
    }

    private Long insertItem(Long planId, String itemCode) {
        QcItemDO item = QcItemDO.builder()
                .planId(planId).itemCode(itemCode).itemName("外观")
                .itemType(MesQcItemTypeEnum.TEXT.name())
                .requiredFlag(Boolean.TRUE).sortNo(0).status(CommonStatusEnum.ENABLE.getStatus()).build();
        qcItemMapper.insert(item);
        return item.getId();
    }

    private MesQcPlanActionBO buildActionBO(Long planId) {
        MesQcPlanActionBO bo = new MesQcPlanActionBO();
        bo.setPlanId(planId);
        bo.setSourceModule(SOURCE_MODULE);
        return bo;
    }

    private QcPlanSaveReqVO buildUpdateReqVO(Long id, String code) {
        QcPlanSaveReqVO reqVO = new QcPlanSaveReqVO();
        reqVO.setId(id);
        reqVO.setCode(code);
        reqVO.setName("被改掉的名称");
        reqVO.setVersionNo("V9.9");
        reqVO.setSampleType(MesQcSampleTypeEnum.FULL.name());
        reqVO.setStatus(MesQcPlanStatusEnum.DRAFT.name());
        return reqVO;
    }

    @Test
    void testPublishPlan_success() {
        Long planId = insertPlan("QCP-A", MesQcPlanStatusEnum.DRAFT.name());
        insertItem(planId, "IT-01");

        qcPlanDomainService.publishPlan(buildActionBO(planId));

        assertEquals(MesQcPlanStatusEnum.PUBLISHED.name(), qcPlanMapper.selectById(planId).getStatus());
    }

    @Test
    void testPublishPlan_withoutItemRejected() {
        Long planId = insertPlan("QCP-A", MesQcPlanStatusEnum.DRAFT.name());

        // 空方案发布了也展开不出检验项，录入环节必然卡死
        assertServiceException(() -> qcPlanDomainService.publishPlan(buildActionBO(planId)),
                QC_PLAN_PUBLISH_WITHOUT_ITEM);
        assertEquals(MesQcPlanStatusEnum.DRAFT.name(), qcPlanMapper.selectById(planId).getStatus());
    }

    @Test
    void testPublishPlan_notDraftRejected() {
        Long planId = insertPlan("QCP-A", MesQcPlanStatusEnum.PUBLISHED.name());
        insertItem(planId, "IT-01");

        assertServiceException(() -> qcPlanDomainService.publishPlan(buildActionBO(planId)),
                QC_PLAN_STATUS_INVALID);
    }

    @Test
    void testPublishPlan_notExists() {
        assertServiceException(() -> qcPlanDomainService.publishPlan(buildActionBO(randomLongId())),
                QC_PLAN_NOT_EXISTS);
    }

    @Test
    void testPublishPlan_idempotent() {
        Long planId = insertPlan("QCP-A", MesQcPlanStatusEnum.DRAFT.name());
        insertItem(planId, "IT-01");

        qcPlanDomainService.publishPlan(buildActionBO(planId));
        // 重复下发直接被命令记录拦下，不再触发状态校验
        qcPlanDomainService.publishPlan(buildActionBO(planId));

        assertEquals(MesQcPlanStatusEnum.PUBLISHED.name(), qcPlanMapper.selectById(planId).getStatus());
    }

    @Test
    void testPublishedPlan_updateRejected() {
        Long planId = insertPlan("QCP-A", MesQcPlanStatusEnum.DRAFT.name());
        insertItem(planId, "IT-01");
        qcPlanDomainService.publishPlan(buildActionBO(planId));

        // 发布后方案锁定：改名称、改抽样规则都不允许，状态由停用动作推进而不是表单
        assertServiceException(() -> qcPlanService.updateQcPlan(buildUpdateReqVO(planId, "QCP-A")),
                QC_PLAN_STATUS_INVALID);
    }

    @Test
    void testPublishedPlan_deleteRejected() {
        Long planId = insertPlan("QCP-A", MesQcPlanStatusEnum.DRAFT.name());
        insertItem(planId, "IT-01");
        qcPlanDomainService.publishPlan(buildActionBO(planId));

        assertServiceException(() -> qcPlanService.deleteQcPlan(planId), QC_PLAN_STATUS_INVALID);
    }

    @Test
    void testPublishedPlan_createItemRejected() {
        Long planId = insertPlan("QCP-A", MesQcPlanStatusEnum.DRAFT.name());
        insertItem(planId, "IT-01");
        qcPlanDomainService.publishPlan(buildActionBO(planId));

        QcItemSaveReqVO itemReqVO = new QcItemSaveReqVO();
        itemReqVO.setPlanId(planId);
        itemReqVO.setItemCode("IT-02");
        itemReqVO.setItemName("耐压");
        itemReqVO.setItemType(MesQcItemTypeEnum.TEXT.name());
        itemReqVO.setRequiredFlag(Boolean.TRUE);
        itemReqVO.setSortNo(1);
        itemReqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        assertServiceException(() -> qcItemService.createQcItem(itemReqVO), QC_ITEM_PLAN_NOT_DRAFT);
    }

    @Test
    void testPublishedPlan_updateItemRejected() {
        Long planId = insertPlan("QCP-A", MesQcPlanStatusEnum.DRAFT.name());
        Long itemId = insertItem(planId, "IT-01");
        qcPlanDomainService.publishPlan(buildActionBO(planId));

        QcItemSaveReqVO itemReqVO = new QcItemSaveReqVO();
        itemReqVO.setId(itemId);
        itemReqVO.setPlanId(planId);
        itemReqVO.setItemCode("IT-01");
        itemReqVO.setItemName("外观改名");
        itemReqVO.setItemType(MesQcItemTypeEnum.TEXT.name());

        // 已发布方案的检验项属于历史任务的判定口径，改名也不允许
        assertServiceException(() -> qcItemService.updateQcItem(itemReqVO), QC_ITEM_PLAN_NOT_DRAFT);
    }

    @Test
    void testDisablePlan_success() {
        Long planId = insertPlan("QCP-A", MesQcPlanStatusEnum.PUBLISHED.name());
        // 停用的规则不算占用，方案照样能停
        qcRuleMapper.insert(QcRuleDO.builder().code("QR-01").name("来料检规则")
                .qcPlanId(planId).status(CommonStatusEnum.DISABLE.getStatus()).build());

        qcPlanDomainService.disablePlan(buildActionBO(planId));

        assertEquals(MesQcPlanStatusEnum.DISABLED.name(), qcPlanMapper.selectById(planId).getStatus());
    }

    @Test
    void testDisablePlan_enabledRuleExistsRejected() {
        Long planId = insertPlan("QCP-A", MesQcPlanStatusEnum.PUBLISHED.name());
        qcRuleMapper.insert(QcRuleDO.builder().code("QR-01").name("来料检规则")
                .qcPlanId(planId).status(CommonStatusEnum.ENABLE.getStatus()).build());

        // 启用中的规则还在往方案上派活，停了它下一次任务生成就报错
        assertServiceException(() -> qcPlanDomainService.disablePlan(buildActionBO(planId)),
                QC_PLAN_IN_USE);
        assertEquals(MesQcPlanStatusEnum.PUBLISHED.name(), qcPlanMapper.selectById(planId).getStatus());
    }

    @Test
    void testDisablePlan_notPublishedRejected() {
        Long draftId = insertPlan("QCP-A", MesQcPlanStatusEnum.DRAFT.name());

        // 草稿不叫"下线"，直接删
        assertServiceException(() -> qcPlanDomainService.disablePlan(buildActionBO(draftId)),
                QC_PLAN_STATUS_INVALID);
    }

    @Test
    void testDisablePlan_notExists() {
        assertServiceException(() -> qcPlanDomainService.disablePlan(buildActionBO(randomLongId())),
                QC_PLAN_NOT_EXISTS);
    }

    @Test
    void testDisablePlan_afterRuleReleased() {
        Long planId = insertPlan("QCP-A", MesQcPlanStatusEnum.PUBLISHED.name());
        qcRuleMapper.insert(QcRuleDO.builder().code("QR-01").name("来料检规则")
                .qcPlanId(planId).status(CommonStatusEnum.ENABLE.getStatus()).build());
        // 规则先停用，方案就能停了——这是停用方案前的标准顺序
        QcRuleDO rule = qcRuleMapper.selectOne(QcRuleDO::getCode, "QR-01");
        rule.setStatus(CommonStatusEnum.DISABLE.getStatus());
        qcRuleMapper.updateById(rule);

        qcPlanDomainService.disablePlan(buildActionBO(planId));

        assertEquals(MesQcPlanStatusEnum.DISABLED.name(), qcPlanMapper.selectById(planId).getStatus());
    }

}
