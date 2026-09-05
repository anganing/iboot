package com.iboot.pf.module.mes.api.report;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.api.report.dto.ProdReportReqDTO;
import com.iboot.pf.module.mes.api.report.dto.ProdReportRespDTO;
import com.iboot.pf.module.mes.api.report.dto.WipTxnRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesProdReportStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.service.prodreport.MesProdReportDomainService;
import com.iboot.pf.module.mes.service.prodreport.bo.MesSubmitReportBO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 生产报工门面委托测试。
 *
 * <p>推进链 mock 掉（三级数量口径由它自己的测试类保证），这里证两件事：报工入参逐字段落地
 * （返工数丢了就再也算不回来）、领域方法只返回编号而门面契约要整条记录，回读那一步不能省。
 *
 * <p>回读与流水查询走真实 H2：用替身证不了"按编号取回的是这条记录""按工单过滤取回的是这些流水"。
 */
@Import(ProdReportApiImpl.class)
class ProdReportApiImplTest extends BaseDbUnitTest {

    private static final Long OP_TASK_ID = 77L;
    private static final Long PROD_ORDER_ID = 99L;
    private static final LocalDateTime START_TIME = LocalDateTime.of(2026, 9, 5, 8, 0);
    private static final LocalDateTime END_TIME = LocalDateTime.of(2026, 9, 5, 12, 0);

    @Resource
    private ProdReportApi prodReportApi;
    @Resource
    private ProdReportMapper prodReportMapper;
    @Resource
    private WipTxnMapper wipTxnMapper;

    @MockitoBean
    private MesProdReportDomainService prodReportDomainService;

    @Test
    void testReportProd_mapsInputAndReadsBackRecord() {
        ProdReportDO stored = ProdReportDO.builder()
                .reportNo("PR-0001").prodOrderId(PROD_ORDER_ID).workOrderId(66L).opTaskId(OP_TASK_ID)
                .workerId(501L).resId(601L).shiftId(701L)
                .qtyGood(new BigDecimal("30")).qtyScrap(new BigDecimal("2")).qtyRework(new BigDecimal("1"))
                .startTime(START_TIME).endTime(END_TIME).reportTime(END_TIME)
                .status(MesProdReportStatusEnum.SUBMITTED.name())
                .idempotencyKey("UI:OT-0001:REPORT:1")
                .build();
        prodReportMapper.insert(stored);
        when(prodReportDomainService.submitReport(any())).thenReturn(stored.getId());

        ProdReportReqDTO reqDTO = new ProdReportReqDTO();
        reqDTO.setIdempotencyKey("UI:OT-0001:REPORT:1");
        reqDTO.setSourceModule("UI");
        reqDTO.setSourceBizNo("OT-0001");
        reqDTO.setOpTaskId(OP_TASK_ID);
        reqDTO.setWorkerId(501L);
        reqDTO.setResId(601L);
        reqDTO.setShiftId(701L);
        reqDTO.setQtyGood(new BigDecimal("30"));
        reqDTO.setQtyScrap(new BigDecimal("2"));
        reqDTO.setQtyRework(new BigDecimal("1"));
        reqDTO.setStartTime(START_TIME);
        reqDTO.setEndTime(END_TIME);
        reqDTO.setRemark("首批报工");

        ProdReportRespDTO respDTO = prodReportApi.reportProd(reqDTO);

        ArgumentCaptor<MesSubmitReportBO> captor = ArgumentCaptor.forClass(MesSubmitReportBO.class);
        verify(prodReportDomainService).submitReport(captor.capture());
        MesSubmitReportBO bo = captor.getValue();
        assertEquals(OP_TASK_ID, bo.getOpTaskId());
        assertEquals(501L, bo.getWorkerId());
        assertEquals(601L, bo.getResId());
        assertEquals(701L, bo.getShiftId());
        assertEquals(0, new BigDecimal("30").compareTo(bo.getQtyGood()));
        assertEquals(0, new BigDecimal("2").compareTo(bo.getQtyScrap()));
        // 返工数只记在报工记录上，丢了它这一批件的归属事后再也算不回来
        assertEquals(0, new BigDecimal("1").compareTo(bo.getQtyRework()));
        assertEquals(START_TIME, bo.getStartTime());
        assertEquals(END_TIME, bo.getEndTime());
        assertEquals("首批报工", bo.getRemark());
        // 幂等键必填且服务端不派生，丢了它扫码枪连扫就会落两条报工
        assertEquals("UI:OT-0001:REPORT:1", bo.getIdempotencyKey());

        assertNotNull(respDTO, "门面契约要的是整条记录，不是编号");
        assertEquals(stored.getId(), respDTO.getId());
        assertEquals("PR-0001", respDTO.getReportNo());
        assertEquals(PROD_ORDER_ID, respDTO.getProdOrderId());
        assertEquals(66L, respDTO.getWorkOrderId());
        assertEquals(MesProdReportStatusEnum.SUBMITTED.name(), respDTO.getStatus());
        assertEquals(0, new BigDecimal("30").compareTo(respDTO.getQtyGood()));
        assertEquals(END_TIME, respDTO.getReportTime());
    }

    @Test
    void testGetWipTxnList_filtersByProdOrderIdsInOneQuery() {
        wipTxnMapper.insert(txn(PROD_ORDER_ID, "WT-0001", MesWipTxnTypeEnum.START, "DRAFT", "RELEASED"));
        wipTxnMapper.insert(txn(PROD_ORDER_ID, "WT-0002", MesWipTxnTypeEnum.REPORT, null, "SUBMITTED"));
        wipTxnMapper.insert(txn(100L, "WT-0003", MesWipTxnTypeEnum.START, "DRAFT", "RELEASED"));
        // 第三张工单不在入参里，用来证明 IN 过滤生效而不是全表返回
        wipTxnMapper.insert(txn(101L, "WT-0004", MesWipTxnTypeEnum.COMPLETE, "RUNNING", "DONE"));

        List<WipTxnRespDTO> list = prodReportApi.getWipTxnList(List.of(PROD_ORDER_ID, 100L));

        assertEquals(3, list.size());
        assertEquals("WT-0001", list.get(0).getTxnNo());
        assertEquals("WT-0003", list.get(2).getTxnNo());
        // 前后状态是追溯查询复现工单走过的路的唯一凭据
        assertEquals("DRAFT", list.get(0).getFromStatus());
        assertEquals("RELEASED", list.get(0).getToStatus());
        assertNull(list.get(1).getFromStatus(), "报工流水的前状态本来就是空");
        assertEquals(MesWipTxnTypeEnum.REPORT.name(), list.get(1).getTxnType());
        assertEquals(MesBizTypeEnum.PROD_ORDER.name(), list.get(1).getBizType());
        assertEquals(0, new BigDecimal("30").compareTo(list.get(1).getQty()));
    }

    @Test
    void testGetWipTxnList_emptyIdsShortCircuit() {
        assertTrue(prodReportApi.getWipTxnList(List.of()).isEmpty());
        assertTrue(prodReportApi.getWipTxnList(null).isEmpty());
    }

    @Test
    void testDataPermissionDisabledOnClassOnly() {
        DataPermission classLevel = ProdReportApiImpl.class.getAnnotation(DataPermission.class);
        assertNotNull(classLevel, "跨模块门面必须在类上关掉数据权限");
        assertFalse(classLevel.enable());
        for (Method method : ProdReportApiImpl.class.getDeclaredMethods()) {
            assertNull(method.getAnnotation(DataPermission.class), method.getName());
        }
    }

    private WipTxnDO txn(Long prodOrderId, String txnNo, MesWipTxnTypeEnum txnType,
                         String fromStatus, String toStatus) {
        return WipTxnDO.builder()
                .txnNo(txnNo).prodOrderId(prodOrderId)
                .workOrderId(MesConstants.NONE_ID).opTaskId(MesConstants.NONE_ID)
                .txnType(txnType.name()).qty(new BigDecimal("30"))
                .fromStatus(fromStatus).toStatus(toStatus)
                .bizType(MesBizTypeEnum.PROD_ORDER.name()).bizNo("PO-0001")
                .fromOpTaskId(MesConstants.NONE_ID).toOpTaskId(MesConstants.NONE_ID)
                .txnTime(END_TIME).idempotencyKey("UI:PO-0001:" + txnNo)
                .build();
    }

}
