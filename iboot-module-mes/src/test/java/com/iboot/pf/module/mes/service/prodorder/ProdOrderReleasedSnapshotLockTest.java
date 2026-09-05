package com.iboot.pf.module.mes.service.prodorder;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.service.MesTestFixtures;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesImportResolver;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_VERSION_SNAPSHOT_LOCKED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 下达后版本快照锁定测试，覆盖 codegen {@code ProdOrderServiceImpl.updateProdOrder} 里的那处校验。
 *
 * <p>下达那一刻起这张工单的执行依据就固定了，之后主数据改 BOM、改工艺都不影响在制工单（04 文档
 * "为什么必须锁版本"）。改单路径是这条约束唯一的缺口，因此单独立一个测试类盯住它，
 * 不掺在领域动作的用例里。
 *
 * <p>两个缺口都要堵：入参换 BOM 编号要报错；入参没换但主数据把版本号改了，
 * 也不能被 {@code fillSnapshot} 回读刷新掉。
 */
@Import(ProdOrderServiceImpl.class)
class ProdOrderReleasedSnapshotLockTest extends BaseDbUnitTest {

    private static final Long LOCKED_BOM_ID = 11L;
    private static final Long LOCKED_ROUTE_ID = 21L;
    private static final String LOCKED_BOM_VERSION = "B1.0";
    private static final String LOCKED_ROUTE_VERSION = "R1.0";

    @Resource
    private ProdOrderService prodOrderService;
    @Resource
    private ProdOrderMapper prodOrderMapper;

    @MockitoBean
    private MesCodeService codeService;
    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private MesRelationFiller relationFiller;
    @MockitoBean
    private MesImportResolver importResolver;

    @BeforeEach
    void mockExternalDependencies() {
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "PO0001" : invocation.getArgument(1));
        when(snapshotService.requireMaterial(any())).thenReturn(MesTestFixtures.material());
        when(snapshotService.requireUomCode(any())).thenReturn(MesTestFixtures.uomCode());
        when(snapshotService.requireBomVersion(eq(LOCKED_BOM_ID))).thenReturn(LOCKED_BOM_VERSION);
        when(snapshotService.requireRouteVersion(eq(LOCKED_ROUTE_ID))).thenReturn(LOCKED_ROUTE_VERSION);
    }

    @Test
    void testUpdateProdOrder_draftAllowsSwitchingBom() {
        // 准备：新建状态的工单还没锁版本，换 BOM 是正常的计划调整
        Long id = prodOrderService.createProdOrder(buildReqVO(LOCKED_BOM_ID, LOCKED_ROUTE_ID));
        when(snapshotService.requireBomVersion(eq(12L))).thenReturn("B2.0");

        // 调用
        ProdOrderSaveReqVO updateReqVO = buildReqVO(12L, LOCKED_ROUTE_ID);
        updateReqVO.setId(id);
        prodOrderService.updateProdOrder(updateReqVO);

        // 断言：换成新 BOM 并重新取版本号
        ProdOrderDO order = prodOrderMapper.selectById(id);
        assertEquals(12L, order.getBomId());
        assertEquals("B2.0", order.getBomVersion());
    }

    @Test
    void testUpdateProdOrder_releasedRejectsSwitchingBom() {
        // 准备：已下达的工单
        Long id = prodOrderService.createProdOrder(buildReqVO(LOCKED_BOM_ID, LOCKED_ROUTE_ID));
        pushStatus(id, MesProdOrderStatusEnum.RELEASED);

        // 调用并断言：换 BOM 直接报错，而不是静默沿用原值——静默会让计划员以为已经换了版本，照新 BOM 备料
        ProdOrderSaveReqVO updateReqVO = buildReqVO(12L, LOCKED_ROUTE_ID);
        updateReqVO.setId(id);
        assertServiceException(() -> prodOrderService.updateProdOrder(updateReqVO),
                PROD_ORDER_VERSION_SNAPSHOT_LOCKED);
        assertEquals(LOCKED_BOM_ID, prodOrderMapper.selectById(id).getBomId());
    }

    @Test
    void testUpdateProdOrder_releasedRejectsSwitchingRoute() {
        // 准备
        Long id = prodOrderService.createProdOrder(buildReqVO(LOCKED_BOM_ID, LOCKED_ROUTE_ID));
        pushStatus(id, MesProdOrderStatusEnum.RUNNING);

        // 调用并断言：工艺路线同样锁住，且不止 RELEASED 一态——下达之后的任何状态都不许改
        ProdOrderSaveReqVO updateReqVO = buildReqVO(LOCKED_BOM_ID, 22L);
        updateReqVO.setId(id);
        assertServiceException(() -> prodOrderService.updateProdOrder(updateReqVO),
                PROD_ORDER_VERSION_SNAPSHOT_LOCKED);
        assertEquals(LOCKED_ROUTE_ID, prodOrderMapper.selectById(id).getRouteId());
    }

    @Test
    void testUpdateProdOrder_releasedKeepsVersionEvenIfMasterDataChanged() {
        // 准备：已下达的工单；之后主数据把这个 BOM 的版本号改了
        Long id = prodOrderService.createProdOrder(buildReqVO(LOCKED_BOM_ID, LOCKED_ROUTE_ID));
        pushStatus(id, MesProdOrderStatusEnum.RELEASED);
        when(snapshotService.requireBomVersion(eq(LOCKED_BOM_ID))).thenReturn("B9.9");
        when(snapshotService.requireRouteVersion(eq(LOCKED_ROUTE_ID))).thenReturn("R9.9");

        // 调用：只改备注，编号一个没动
        ProdOrderSaveReqVO updateReqVO = buildReqVO(LOCKED_BOM_ID, LOCKED_ROUTE_ID);
        updateReqVO.setId(id);
        updateReqVO.setRemark("交期调整");
        prodOrderService.updateProdOrder(updateReqVO);

        // 断言：版本快照仍是下达那一刻的值，没被主数据回读刷新；备注照常改到
        ProdOrderDO order = prodOrderMapper.selectById(id);
        assertEquals(LOCKED_BOM_VERSION, order.getBomVersion());
        assertEquals(LOCKED_ROUTE_VERSION, order.getRouteVersion());
        assertEquals("交期调整", order.getRemark());
    }

    /** 表单不带状态，需要非初始状态的场景直接改库：状态只能由领域动作推进，测试里也不绕表单塞状态。 */
    private void pushStatus(Long id, MesProdOrderStatusEnum status) {
        prodOrderMapper.updateById(new ProdOrderDO().setId(id).setStatus(status.name()));
    }

    private ProdOrderSaveReqVO buildReqVO(Long bomId, Long routeId) {
        ProdOrderSaveReqVO reqVO = new ProdOrderSaveReqVO();
        reqVO.setDemandId(MesConstants.NONE_ID);
        reqVO.setPlantId(1L);
        reqVO.setWorkshopId(2L);
        reqVO.setProjectId(MesConstants.NONE_ID);
        reqVO.setProjectTaskId(MesConstants.NONE_ID);
        reqVO.setMatId(1L);
        reqVO.setQty(new BigDecimal("100"));
        reqVO.setUomId(1L);
        reqVO.setBomId(bomId);
        reqVO.setRouteId(routeId);
        reqVO.setDueDate(LocalDateTime.now().plusDays(10));
        reqVO.setPriority(0);
        return reqVO;
    }

}
