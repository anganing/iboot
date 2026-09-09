package com.iboot.pf.module.mes.service.qcitem;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemPageReqVO;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemRespVO;
import com.iboot.pf.module.mes.controller.admin.qcitem.vo.QcItemSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.qcitem.QcItemDO;
import com.iboot.pf.module.mes.dal.dataobject.qcplan.QcPlanDO;
import com.iboot.pf.module.mes.dal.mysql.qcitem.QcItemMapper;
import com.iboot.pf.module.mes.enums.MesQcItemTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.service.qcplan.QcPlanService;
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
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_ITEM_CODE_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_ITEM_LIMIT_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_ITEM_LIMIT_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_ITEM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QC_ITEM_PLAN_NOT_DRAFT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 检验项目 Service 测试：只允许在草稿方案上维护、数值型上下限校验、同方案编码唯一。
 */
@Import(QcItemServiceImpl.class)
class QcItemServiceImplTest extends BaseDbUnitTest {

    private static final Long DRAFT_PLAN = 1L;
    private static final Long PUBLISHED_PLAN = 2L;

    @Resource
    private QcItemService qcItemService;
    @Resource
    private QcItemMapper qcItemMapper;

    @MockitoBean
    private QcPlanService qcPlanService;

    @BeforeEach
    void mockExternalDependencies() {
        // 检验方案的状态决定检验项能不能改，这里直接给出两种方案
        when(qcPlanService.validateQcPlanExists(eq(DRAFT_PLAN)))
                .thenReturn(new QcPlanDO().setId(DRAFT_PLAN).setStatus(MesQcPlanStatusEnum.DRAFT.name()));
        when(qcPlanService.validateQcPlanExists(eq(PUBLISHED_PLAN)))
                .thenReturn(new QcPlanDO().setId(PUBLISHED_PLAN).setStatus(MesQcPlanStatusEnum.PUBLISHED.name()));
    }

    private QcItemSaveReqVO buildReqVO(Long planId, String itemCode) {
        QcItemSaveReqVO reqVO = new QcItemSaveReqVO();
        reqVO.setPlanId(planId);
        reqVO.setItemCode(itemCode);
        reqVO.setItemName("外观检查");
        reqVO.setItemType(MesQcItemTypeEnum.TEXT.name());
        reqVO.setRequiredFlag(Boolean.TRUE);
        reqVO.setSortNo(0);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateQcItem_success() {
        Long id = qcItemService.createQcItem(buildReqVO(DRAFT_PLAN, "IT-01"));

        QcItemDO qcItem = qcItemMapper.selectById(id);
        assertNotNull(qcItem);
        assertEquals("IT-01", qcItem.getItemCode());
    }

    @Test
    void testCreateQcItem_publishedPlanRejected() {
        // 已发布方案会被质检任务锁定引用，改检验项等于改历史任务的判定口径
        assertServiceException(() -> qcItemService.createQcItem(buildReqVO(PUBLISHED_PLAN, "IT-01")),
                QC_ITEM_PLAN_NOT_DRAFT);
    }

    @Test
    void testCreateQcItem_numericLimitRequired() {
        QcItemSaveReqVO reqVO = buildReqVO(DRAFT_PLAN, "IT-01");
        reqVO.setItemType(MesQcItemTypeEnum.NUMERIC.name());

        assertServiceException(() -> qcItemService.createQcItem(reqVO), QC_ITEM_LIMIT_REQUIRED);
    }

    @Test
    void testCreateQcItem_numericLimitInverted() {
        QcItemSaveReqVO reqVO = buildReqVO(DRAFT_PLAN, "IT-01");
        reqVO.setItemType(MesQcItemTypeEnum.NUMERIC.name());
        reqVO.setUpperLimit(new BigDecimal("1"));
        reqVO.setLowerLimit(new BigDecimal("2"));

        assertServiceException(() -> qcItemService.createQcItem(reqVO), QC_ITEM_LIMIT_INVALID);
    }

    @Test
    void testCreateQcItem_numericWithLimitAccepted() {
        QcItemSaveReqVO reqVO = buildReqVO(DRAFT_PLAN, "IT-01");
        reqVO.setItemType(MesQcItemTypeEnum.NUMERIC.name());
        reqVO.setUpperLimit(new BigDecimal("10.5"));
        reqVO.setLowerLimit(new BigDecimal("9.5"));

        assertNotNull(qcItemService.createQcItem(reqVO));
    }

    @Test
    void testCreateQcItem_duplicateCodeRejected() {
        qcItemService.createQcItem(buildReqVO(DRAFT_PLAN, "IT-01"));

        assertServiceException(() -> qcItemService.createQcItem(buildReqVO(DRAFT_PLAN, "IT-01")),
                QC_ITEM_CODE_DUPLICATE);
    }

    @Test
    void testUpdateQcItem_keepOwnCode() {
        Long id = qcItemService.createQcItem(buildReqVO(DRAFT_PLAN, "IT-01"));

        // 改自己时编码没变，不应该撞到自己
        QcItemSaveReqVO updateReqVO = buildReqVO(DRAFT_PLAN, "IT-01");
        updateReqVO.setId(id);
        updateReqVO.setItemName("外观检查改名");
        qcItemService.updateQcItem(updateReqVO);

        assertEquals("外观检查改名", qcItemMapper.selectById(id).getItemName());
    }

    @Test
    void testUpdateQcItem_moveFromPublishedPlanRejected() {
        // 原所属方案是已发布时，把 planId 改成草稿方案等于把检验项"合法搬走"，仍要拦
        Long id = qcItemService.createQcItem(buildReqVO(DRAFT_PLAN, "IT-01"));
        QcItemDO existed = qcItemMapper.selectById(id);
        existed.setPlanId(PUBLISHED_PLAN);
        qcItemMapper.updateById(existed);

        QcItemSaveReqVO updateReqVO = buildReqVO(DRAFT_PLAN, "IT-01");
        updateReqVO.setId(id);
        assertServiceException(() -> qcItemService.updateQcItem(updateReqVO), QC_ITEM_PLAN_NOT_DRAFT);
    }

    @Test
    void testUpdateQcItem_duplicateCodeRejected() {
        qcItemService.createQcItem(buildReqVO(DRAFT_PLAN, "IT-01"));
        Long secondId = qcItemService.createQcItem(buildReqVO(DRAFT_PLAN, "IT-02"));

        QcItemSaveReqVO updateReqVO = buildReqVO(DRAFT_PLAN, "IT-01");
        updateReqVO.setId(secondId);
        assertServiceException(() -> qcItemService.updateQcItem(updateReqVO), QC_ITEM_CODE_DUPLICATE);
    }

    @Test
    void testUpdateQcItem_notExists() {
        QcItemSaveReqVO updateReqVO = buildReqVO(DRAFT_PLAN, "IT-01");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> qcItemService.updateQcItem(updateReqVO), QC_ITEM_NOT_EXISTS);
    }

    @Test
    void testDeleteQcItem_success() {
        Long id = qcItemService.createQcItem(buildReqVO(DRAFT_PLAN, "IT-01"));

        qcItemService.deleteQcItem(id);

        assertNull(qcItemMapper.selectById(id));
    }

    @Test
    void testDeleteQcItemList_success() {
        Long first = qcItemService.createQcItem(buildReqVO(DRAFT_PLAN, "IT-01"));
        Long second = qcItemService.createQcItem(buildReqVO(DRAFT_PLAN, "IT-02"));

        qcItemService.deleteQcItemListByIds(List.of(first, second));

        assertNull(qcItemMapper.selectById(first));
        assertNull(qcItemMapper.selectById(second));
    }

    @Test
    void testGetQcItemPage() {
        qcItemService.createQcItem(buildReqVO(DRAFT_PLAN, "IT-01"));

        QcItemPageReqVO reqVO = new QcItemPageReqVO();
        reqVO.setPlanId(DRAFT_PLAN);
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<QcItemRespVO> list = qcItemService.getQcItemPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("IT-01", list.get(0).getItemCode());
    }

}
