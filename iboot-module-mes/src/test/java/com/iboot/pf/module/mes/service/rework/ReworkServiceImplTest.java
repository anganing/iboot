package com.iboot.pf.module.mes.service.rework;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkPageReqVO;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkRespVO;
import com.iboot.pf.module.mes.controller.admin.rework.vo.ReworkSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.rework.ReworkDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.rework.ReworkMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesReworkStatusEnum;
import com.iboot.pf.module.mes.service.MesTestFixtures;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
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
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.REWORK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.REWORK_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 返工任务 Service 测试：单据链从工序任务反查、返工路线版本锁定、只有新建允许改与删。
 */
@Import(ReworkServiceImpl.class)
class ReworkServiceImplTest extends BaseDbUnitTest {

    private static final Long PROD_ORDER_ID = 9L;

    @Resource
    private ReworkService reworkService;
    @Resource
    private ReworkMapper reworkMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;

    @MockitoBean
    private MesCodeService codeService;
    @MockitoBean
    private MesSnapshotService snapshotService;

    private Long opTaskId;
    private Long workOrderId;

    @BeforeEach
    void mockExternalDependencies() {
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "RW0001" : invocation.getArgument(1));
        when(snapshotService.requireRouteVersion(any())).thenReturn(MesTestFixtures.route().getVersion());
        WorkOrderDO workOrder = new WorkOrderDO().setWorkOrderNo("WO-0001").setProdOrderId(PROD_ORDER_ID);
        workOrderMapper.insert(workOrder);
        workOrderId = workOrder.getId();
        OpTaskDO opTask = new OpTaskDO().setTaskNo("OPT-0001").setWorkOrderId(workOrderId).setRouteOpId(31L);
        opTaskMapper.insert(opTask);
        opTaskId = opTask.getId();
    }

    private ReworkSaveReqVO buildReqVO(String reworkNo) {
        ReworkSaveReqVO reqVO = new ReworkSaveReqVO();
        reqVO.setReworkNo(reworkNo);
        reqVO.setSourceQcTaskId(MesConstants.NONE_ID);
        reqVO.setOpTaskId(opTaskId);
        reqVO.setQty(new BigDecimal("5"));
        reqVO.setRouteId(21L);
        reqVO.setStatus(MesReworkStatusEnum.DRAFT.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateRework_inheritOrderChainAndLockRouteVersion() {
        Long id = reworkService.createRework(buildReqVO(null));

        ReworkDO rework = reworkMapper.selectById(id);
        assertNotNull(rework);
        assertEquals("RW0001", rework.getReworkNo());
        assertEquals(workOrderId, rework.getWorkOrderId());
        assertEquals(PROD_ORDER_ID, rework.getProdOrderId());
        // 返工路线版本在建单时锁定
        assertEquals("R1.0", rework.getRouteVersion());
    }

    @Test
    void testCreateRework_opTaskNotExists() {
        ReworkSaveReqVO reqVO = buildReqVO("RW-A");
        reqVO.setOpTaskId(randomLongId());

        assertServiceException(() -> reworkService.createRework(reqVO), OP_TASK_NOT_EXISTS);
    }

    @Test
    void testUpdateRework_keepReworkNo() {
        Long id = reworkService.createRework(buildReqVO("RW-A"));

        ReworkSaveReqVO updateReqVO = buildReqVO("RW-CHANGED");
        updateReqVO.setId(id);
        updateReqVO.setRemark("返工数量调整");
        reworkService.updateRework(updateReqVO);

        ReworkDO rework = reworkMapper.selectById(id);
        assertEquals("RW-A", rework.getReworkNo());
        assertEquals("返工数量调整", rework.getRemark());
    }

    @Test
    void testUpdateRework_releasedRejected() {
        ReworkSaveReqVO createReqVO = buildReqVO("RW-A");
        createReqVO.setStatus(MesReworkStatusEnum.RELEASED.name());
        Long id = reworkService.createRework(createReqVO);

        ReworkSaveReqVO updateReqVO = buildReqVO("RW-A");
        updateReqVO.setId(id);
        // 已下达的返工任务已经在车间执行
        assertServiceException(() -> reworkService.updateRework(updateReqVO), REWORK_STATUS_INVALID);
    }

    @Test
    void testUpdateRework_notExists() {
        ReworkSaveReqVO updateReqVO = buildReqVO("RW-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> reworkService.updateRework(updateReqVO), REWORK_NOT_EXISTS);
    }

    @Test
    void testDeleteRework_success() {
        Long id = reworkService.createRework(buildReqVO("RW-A"));

        reworkService.deleteRework(id);

        assertNull(reworkMapper.selectById(id));
    }

    @Test
    void testDeleteRework_releasedRejected() {
        ReworkSaveReqVO createReqVO = buildReqVO("RW-A");
        createReqVO.setStatus(MesReworkStatusEnum.RUNNING.name());
        Long id = reworkService.createRework(createReqVO);

        assertServiceException(() -> reworkService.deleteRework(id), REWORK_STATUS_INVALID);
    }

    @Test
    void testDeleteReworkList_releasedRejected() {
        Long first = reworkService.createRework(buildReqVO("RW-A"));
        ReworkSaveReqVO running = buildReqVO("RW-B");
        running.setStatus(MesReworkStatusEnum.RUNNING.name());
        Long second = reworkService.createRework(running);

        assertServiceException(() -> reworkService.deleteReworkListByIds(List.of(first, second)),
                REWORK_STATUS_INVALID);
        assertNotNull(reworkMapper.selectById(first));
    }

    @Test
    void testGetReworkPage() {
        reworkService.createRework(buildReqVO("RW-A"));

        ReworkPageReqVO reqVO = new ReworkPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<ReworkRespVO> list = reworkService.getReworkPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("RW-A", list.get(0).getReworkNo());
    }

}
