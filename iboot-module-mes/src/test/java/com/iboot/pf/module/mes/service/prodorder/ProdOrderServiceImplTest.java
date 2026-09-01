package com.iboot.pf.module.mes.service.prodorder;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderPageReqVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderRespVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
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
import java.util.List;

import static com.iboot.pf.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_HAS_WORK_ORDER;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 生产工单 Service 测试：单号自动分配、BOM 与路线版本快照锁定、进度数量归零、终态禁改、已开作业工单禁删。
 */
@Import(ProdOrderServiceImpl.class)
class ProdOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProdOrderService prodOrderService;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;

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
        when(snapshotService.requireBomVersion(any())).thenReturn(MesTestFixtures.bom().getVersion());
        when(snapshotService.requireRouteVersion(any())).thenReturn(MesTestFixtures.route().getVersion());
    }

    private ProdOrderSaveReqVO buildReqVO(String orderNo) {
        ProdOrderSaveReqVO reqVO = new ProdOrderSaveReqVO();
        reqVO.setOrderNo(orderNo);
        reqVO.setDemandId(MesConstants.NONE_ID);
        reqVO.setPlantId(1L);
        reqVO.setWorkshopId(1L);
        reqVO.setProjectId(MesConstants.NONE_ID);
        reqVO.setProjectTaskId(MesConstants.NONE_ID);
        reqVO.setMatId(1L);
        reqVO.setQty(new BigDecimal("100"));
        reqVO.setUomId(1L);
        reqVO.setBomId(11L);
        reqVO.setRouteId(21L);
        reqVO.setDueDate(LocalDateTime.now().plusDays(10));
        reqVO.setPriority(0);
        reqVO.setStatus(MesProdOrderStatusEnum.DRAFT.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateProdOrder_snapshotLockedAndProgressZeroed() {
        Long id = prodOrderService.createProdOrder(buildReqVO(null));

        ProdOrderDO order = prodOrderMapper.selectById(id);
        assertNotNull(order);
        assertEquals("PO0001", order.getOrderNo());
        // BOM 与路线版本在建单时锁定，之后主数据改版不影响本单
        assertEquals("B1.0", order.getBomVersion());
        assertEquals("R1.0", order.getRouteVersion());
        // 进度数量由报工推进，齐套状态由齐套检查写入，创建时归零
        assertEquals(0, order.getCompletedQty().compareTo(BigDecimal.ZERO));
        assertEquals(0, order.getScrapQty().compareTo(BigDecimal.ZERO));
        assertEquals(MesKitStatusEnum.UNCHECKED.name(), order.getKitStatus());
        assertEquals(MesConstants.INIT_VERSION, order.getVersion());
    }

    @Test
    void testUpdateProdOrder_keepOrderNoAndProgress() {
        Long id = prodOrderService.createProdOrder(buildReqVO("PO-A"));

        ProdOrderSaveReqVO updateReqVO = buildReqVO("PO-CHANGED");
        updateReqVO.setId(id);
        updateReqVO.setRemark("交期调整");
        prodOrderService.updateProdOrder(updateReqVO);

        ProdOrderDO order = prodOrderMapper.selectById(id);
        assertEquals("PO-A", order.getOrderNo());
        assertEquals("交期调整", order.getRemark());
        assertEquals(MesKitStatusEnum.UNCHECKED.name(), order.getKitStatus());
    }

    @Test
    void testUpdateProdOrder_finalStatusRejected() {
        ProdOrderSaveReqVO createReqVO = buildReqVO("PO-A");
        createReqVO.setStatus(MesProdOrderStatusEnum.CLOSED.name());
        Long id = prodOrderService.createProdOrder(createReqVO);

        ProdOrderSaveReqVO updateReqVO = buildReqVO("PO-A");
        updateReqVO.setId(id);
        assertServiceException(() -> prodOrderService.updateProdOrder(updateReqVO), PROD_ORDER_STATUS_INVALID);
    }

    @Test
    void testUpdateProdOrder_notExists() {
        ProdOrderSaveReqVO updateReqVO = buildReqVO("PO-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> prodOrderService.updateProdOrder(updateReqVO), PROD_ORDER_NOT_EXISTS);
    }

    @Test
    void testDeleteProdOrder_success() {
        Long id = prodOrderService.createProdOrder(buildReqVO("PO-A"));

        prodOrderService.deleteProdOrder(id);

        assertNull(prodOrderMapper.selectById(id));
    }

    @Test
    void testDeleteProdOrder_releasedRejected() {
        ProdOrderSaveReqVO createReqVO = buildReqVO("PO-A");
        createReqVO.setStatus(MesProdOrderStatusEnum.RELEASED.name());
        Long id = prodOrderService.createProdOrder(createReqVO);

        // 已下达的工单下面挂着作业工单与工序任务，不允许删
        assertServiceException(() -> prodOrderService.deleteProdOrder(id), PROD_ORDER_STATUS_INVALID);
    }

    @Test
    void testDeleteProdOrder_hasWorkOrderRejected() {
        Long id = prodOrderService.createProdOrder(buildReqVO("PO-A"));
        workOrderMapper.insert(new WorkOrderDO().setProdOrderId(id).setWorkOrderNo("WO-0001"));

        assertServiceException(() -> prodOrderService.deleteProdOrder(id), PROD_ORDER_HAS_WORK_ORDER);
    }

    @Test
    void testDeleteProdOrderList_hasWorkOrderRejected() {
        Long first = prodOrderService.createProdOrder(buildReqVO("PO-A"));
        Long second = prodOrderService.createProdOrder(buildReqVO("PO-B"));
        workOrderMapper.insert(new WorkOrderDO().setProdOrderId(second).setWorkOrderNo("WO-0001"));

        assertServiceException(() -> prodOrderService.deleteProdOrderListByIds(List.of(first, second)),
                PROD_ORDER_HAS_WORK_ORDER);
        assertNotNull(prodOrderMapper.selectById(first));
    }

    @Test
    void testGetProdOrderPage() {
        prodOrderService.createProdOrder(buildReqVO("PO-A"));

        ProdOrderPageReqVO reqVO = new ProdOrderPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<ProdOrderRespVO> list = prodOrderService.getProdOrderPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("PO-A", list.get(0).getOrderNo());
        assertEquals("MAT-001", list.get(0).getMatCode());
    }

}
