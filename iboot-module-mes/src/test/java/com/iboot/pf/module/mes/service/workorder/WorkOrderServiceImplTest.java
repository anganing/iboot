package com.iboot.pf.module.mes.service.workorder;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderPageReqVO;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderRespVO;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.WorkOrderSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.snapshot.MesRelationFiller;
import com.iboot.pf.module.mes.service.snapshot.MesSnapshotService;
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
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_HAS_OP_TASK;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 作业工单 Service 测试：单号自动分配、组织维度从生产工单继承、进度归零、待处理才可删、已有工序任务禁删。
 */
@Import(WorkOrderServiceImpl.class)
class WorkOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WorkOrderService workOrderService;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private OpTaskMapper opTaskMapper;

    @MockitoBean
    private MesCodeService codeService;
    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private MesRelationFiller relationFiller;

    private Long prodOrderId;

    @BeforeEach
    void mockExternalDependencies() {
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "WO0001" : invocation.getArgument(1));
        prodOrderId = insertProdOrder(MesProdOrderStatusEnum.RELEASED.name());
    }

    /** 生产工单是作业工单的父单，组织维度都从它继承。 */
    private Long insertProdOrder(String status) {
        ProdOrderDO prodOrder = new ProdOrderDO()
                .setOrderNo("PO-0001")
                .setPlantId(1L)
                .setWorkshopId(2L)
                .setProjectId(MesConstants.NONE_ID)
                .setProjectTaskId(MesConstants.NONE_ID)
                .setStatus(status);
        prodOrderMapper.insert(prodOrder);
        return prodOrder.getId();
    }

    private WorkOrderSaveReqVO buildReqVO(String workOrderNo) {
        WorkOrderSaveReqVO reqVO = new WorkOrderSaveReqVO();
        reqVO.setWorkOrderNo(workOrderNo);
        reqVO.setProdOrderId(prodOrderId);
        reqVO.setWcId(3L);
        reqVO.setPlannedQty(new BigDecimal("100"));
        reqVO.setStatus(MesWorkOrderStatusEnum.PENDING.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateWorkOrder_inheritFromProdOrder() {
        Long id = workOrderService.createWorkOrder(buildReqVO(null));

        WorkOrderDO workOrder = workOrderMapper.selectById(id);
        assertNotNull(workOrder);
        assertEquals("WO0001", workOrder.getWorkOrderNo());
        // 工厂、车间、单号快照从生产工单继承，让前端各填一遍只会制造不一致
        assertEquals("PO-0001", workOrder.getProdOrderNo());
        assertEquals(1L, workOrder.getPlantId());
        assertEquals(2L, workOrder.getWorkshopId());
        assertEquals(0, workOrder.getCompletedQty().compareTo(BigDecimal.ZERO));
        assertEquals(MesKitStatusEnum.UNCHECKED.name(), workOrder.getKitStatus());
    }

    @Test
    void testCreateWorkOrder_prodOrderNotExists() {
        WorkOrderSaveReqVO reqVO = buildReqVO("WO-A");
        reqVO.setProdOrderId(randomLongId());

        assertServiceException(() -> workOrderService.createWorkOrder(reqVO), PROD_ORDER_NOT_EXISTS);
    }

    @Test
    void testCreateWorkOrder_prodOrderFinalRejected() {
        WorkOrderSaveReqVO reqVO = buildReqVO("WO-A");
        reqVO.setProdOrderId(insertProdOrder(MesProdOrderStatusEnum.CLOSED.name()));

        // 已关闭的生产工单不允许再开作业工单
        assertServiceException(() -> workOrderService.createWorkOrder(reqVO), PROD_ORDER_STATUS_INVALID);
    }

    @Test
    void testUpdateWorkOrder_keepWorkOrderNo() {
        Long id = workOrderService.createWorkOrder(buildReqVO("WO-A"));

        WorkOrderSaveReqVO updateReqVO = buildReqVO("WO-CHANGED");
        updateReqVO.setId(id);
        updateReqVO.setRemark("计划调整");
        workOrderService.updateWorkOrder(updateReqVO);

        WorkOrderDO workOrder = workOrderMapper.selectById(id);
        assertEquals("WO-A", workOrder.getWorkOrderNo());
        assertEquals("计划调整", workOrder.getRemark());
    }

    @Test
    void testUpdateWorkOrder_finalStatusRejected() {
        WorkOrderSaveReqVO createReqVO = buildReqVO("WO-A");
        createReqVO.setStatus(MesWorkOrderStatusEnum.DONE.name());
        Long id = workOrderService.createWorkOrder(createReqVO);

        WorkOrderSaveReqVO updateReqVO = buildReqVO("WO-A");
        updateReqVO.setId(id);
        assertServiceException(() -> workOrderService.updateWorkOrder(updateReqVO), WORK_ORDER_STATUS_INVALID);
    }

    @Test
    void testUpdateWorkOrder_notExists() {
        WorkOrderSaveReqVO updateReqVO = buildReqVO("WO-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> workOrderService.updateWorkOrder(updateReqVO), WORK_ORDER_NOT_EXISTS);
    }

    @Test
    void testDeleteWorkOrder_success() {
        Long id = workOrderService.createWorkOrder(buildReqVO("WO-A"));

        workOrderService.deleteWorkOrder(id);

        assertNull(workOrderMapper.selectById(id));
    }

    @Test
    void testDeleteWorkOrder_hasOpTaskRejected() {
        Long id = workOrderService.createWorkOrder(buildReqVO("WO-A"));
        opTaskMapper.insert(new OpTaskDO().setWorkOrderId(id).setTaskNo("OPT-0001").setRouteOpId(31L));

        assertServiceException(() -> workOrderService.deleteWorkOrder(id), WORK_ORDER_HAS_OP_TASK);
    }

    @Test
    void testDeleteWorkOrderList_hasOpTaskRejected() {
        Long first = workOrderService.createWorkOrder(buildReqVO("WO-A"));
        Long second = workOrderService.createWorkOrder(buildReqVO("WO-B"));
        opTaskMapper.insert(new OpTaskDO().setWorkOrderId(second).setTaskNo("OPT-0001").setRouteOpId(31L));

        assertServiceException(() -> workOrderService.deleteWorkOrderListByIds(List.of(first, second)),
                WORK_ORDER_HAS_OP_TASK);
        assertNotNull(workOrderMapper.selectById(first));
    }

    @Test
    void testGetWorkOrderPage() {
        workOrderService.createWorkOrder(buildReqVO("WO-A"));

        WorkOrderPageReqVO reqVO = new WorkOrderPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<WorkOrderRespVO> list = workOrderService.getWorkOrderPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("WO-A", list.get(0).getWorkOrderNo());
        // 生产工单号由模块内 JOIN 出来，前端不做二次拼接
        assertEquals("PO-0001", list.get(0).getProdOrderNo());
    }

}
