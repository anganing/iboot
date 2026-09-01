package com.iboot.pf.module.mes.service.prodreport;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportPageReqVO;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportRespVO;
import com.iboot.pf.module.mes.controller.admin.prodreport.vo.ProdReportSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesProdReportStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
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
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.IDEMPOTENCY_KEY_DUPLICATE;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_QTY_EMPTY;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_REPORT_TIME_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 生产报工 Service 测试：单据链从工序任务反查、数量不能全零、结束时间不早于开始时间、已确认禁改禁删、幂等键唯一。
 */
@Import(ProdReportServiceImpl.class)
class ProdReportServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProdReportService prodReportService;
    @Resource
    private ProdReportMapper prodReportMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;

    @MockitoBean
    private MesCodeService codeService;
    @MockitoBean
    private MesSnapshotService snapshotService;
    @MockitoBean
    private MesRelationFiller relationFiller;

    private Long opTaskId;
    private Long workOrderId;
    private static final Long PROD_ORDER_ID = 9L;

    @BeforeEach
    void mockExternalDependencies() {
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "RPT0001" : invocation.getArgument(1));
        WorkOrderDO workOrder = new WorkOrderDO().setWorkOrderNo("WO-0001").setProdOrderId(PROD_ORDER_ID);
        workOrderMapper.insert(workOrder);
        workOrderId = workOrder.getId();
        OpTaskDO opTask = new OpTaskDO().setTaskNo("OPT-0001").setWorkOrderId(workOrderId).setRouteOpId(31L);
        opTaskMapper.insert(opTask);
        opTaskId = opTask.getId();
    }

    private ProdReportSaveReqVO buildReqVO(String reportNo) {
        ProdReportSaveReqVO reqVO = new ProdReportSaveReqVO();
        reqVO.setReportNo(reportNo);
        reqVO.setOpTaskId(opTaskId);
        reqVO.setWorkerId(1L);
        reqVO.setResId(MesConstants.NONE_ID);
        reqVO.setShiftId(MesConstants.NONE_ID);
        reqVO.setQtyGood(new BigDecimal("10"));
        reqVO.setQtyScrap(BigDecimal.ZERO);
        reqVO.setQtyRework(BigDecimal.ZERO);
        reqVO.setStartTime(LocalDateTime.now().minusHours(2));
        reqVO.setEndTime(LocalDateTime.now().minusHours(1));
        reqVO.setStatus(MesProdReportStatusEnum.DRAFT.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateProdReport_inheritOrderChain() {
        Long id = prodReportService.createProdReport(buildReqVO(null));

        ProdReportDO report = prodReportMapper.selectById(id);
        assertNotNull(report);
        assertEquals("RPT0001", report.getReportNo());
        // 生产工单与作业工单从工序任务反查，前端只提交工序任务
        assertEquals(workOrderId, report.getWorkOrderId());
        assertEquals(PROD_ORDER_ID, report.getProdOrderId());
        // 报工时间留空取当前时间
        assertNotNull(report.getReportTime());
    }

    @Test
    void testCreateProdReport_opTaskNotExists() {
        ProdReportSaveReqVO reqVO = buildReqVO("RPT-A");
        reqVO.setOpTaskId(randomLongId());

        assertServiceException(() -> prodReportService.createProdReport(reqVO), OP_TASK_NOT_EXISTS);
    }

    @Test
    void testCreateProdReport_allQtyZeroRejected() {
        ProdReportSaveReqVO reqVO = buildReqVO("RPT-A");
        reqVO.setQtyGood(BigDecimal.ZERO);

        // 三个数量全为零这条报工没有意义
        assertServiceException(() -> prodReportService.createProdReport(reqVO), PROD_REPORT_QTY_EMPTY);
    }

    @Test
    void testCreateProdReport_endBeforeStartRejected() {
        ProdReportSaveReqVO reqVO = buildReqVO("RPT-A");
        reqVO.setEndTime(reqVO.getStartTime().minusMinutes(1));

        assertServiceException(() -> prodReportService.createProdReport(reqVO), PROD_REPORT_TIME_INVALID);
    }

    @Test
    void testCreateProdReport_duplicateIdempotencyKeyRejected() {
        ProdReportSaveReqVO first = buildReqVO("RPT-A");
        first.setIdempotencyKey("SCAN:OPT-0001:1");
        prodReportService.createProdReport(first);

        ProdReportSaveReqVO second = buildReqVO("RPT-B");
        second.setIdempotencyKey("SCAN:OPT-0001:1");
        assertServiceException(() -> prodReportService.createProdReport(second), IDEMPOTENCY_KEY_DUPLICATE);
    }

    @Test
    void testUpdateProdReport_keepReportNo() {
        Long id = prodReportService.createProdReport(buildReqVO("RPT-A"));

        ProdReportSaveReqVO updateReqVO = buildReqVO("RPT-CHANGED");
        updateReqVO.setId(id);
        updateReqVO.setRemark("数量修正");
        prodReportService.updateProdReport(updateReqVO);

        ProdReportDO report = prodReportMapper.selectById(id);
        assertEquals("RPT-A", report.getReportNo());
        assertEquals("数量修正", report.getRemark());
    }

    @Test
    void testUpdateProdReport_confirmedRejected() {
        ProdReportSaveReqVO createReqVO = buildReqVO("RPT-A");
        createReqVO.setStatus(MesProdReportStatusEnum.CONFIRMED.name());
        Long id = prodReportService.createProdReport(createReqVO);

        ProdReportSaveReqVO updateReqVO = buildReqVO("RPT-A");
        updateReqVO.setId(id);
        // 已确认的报工数量已推进到工序任务，不允许再改
        assertServiceException(() -> prodReportService.updateProdReport(updateReqVO), PROD_REPORT_STATUS_INVALID);
    }

    @Test
    void testUpdateProdReport_notExists() {
        ProdReportSaveReqVO updateReqVO = buildReqVO("RPT-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> prodReportService.updateProdReport(updateReqVO), PROD_REPORT_NOT_EXISTS);
    }

    @Test
    void testDeleteProdReport_success() {
        Long id = prodReportService.createProdReport(buildReqVO("RPT-A"));

        prodReportService.deleteProdReport(id);

        assertNull(prodReportMapper.selectById(id));
    }

    @Test
    void testDeleteProdReport_confirmedRejected() {
        ProdReportSaveReqVO createReqVO = buildReqVO("RPT-A");
        createReqVO.setStatus(MesProdReportStatusEnum.CONFIRMED.name());
        Long id = prodReportService.createProdReport(createReqVO);

        assertServiceException(() -> prodReportService.deleteProdReport(id), PROD_REPORT_STATUS_INVALID);
    }

    @Test
    void testDeleteProdReportList_confirmedRejected() {
        Long first = prodReportService.createProdReport(buildReqVO("RPT-A"));
        ProdReportSaveReqVO confirmed = buildReqVO("RPT-B");
        confirmed.setStatus(MesProdReportStatusEnum.CONFIRMED.name());
        Long second = prodReportService.createProdReport(confirmed);

        assertServiceException(() -> prodReportService.deleteProdReportListByIds(List.of(first, second)),
                PROD_REPORT_STATUS_INVALID);
        assertNotNull(prodReportMapper.selectById(first));
    }

    @Test
    void testGetProdReportPage() {
        prodReportService.createProdReport(buildReqVO("RPT-A"));

        ProdReportPageReqVO reqVO = new ProdReportPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<ProdReportRespVO> list = prodReportService.getProdReportPage(reqVO).getList();

        assertEquals(1, list.size());
        assertEquals("RPT-A", list.get(0).getReportNo());
    }

}
