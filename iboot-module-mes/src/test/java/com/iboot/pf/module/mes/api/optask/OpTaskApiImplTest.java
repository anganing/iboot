package com.iboot.pf.module.mes.api.optask;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.test.core.ut.BaseMockitoUnitTest;
import com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskFinishReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskSkipReqDTO;
import com.iboot.pf.module.mes.api.optask.dto.OpTaskStatusReqDTO;
import com.iboot.pf.module.mes.service.opskiprecord.bo.MesOpSkipBO;
import com.iboot.pf.module.mes.service.optask.MesOpTaskDomainService;
import com.iboot.pf.module.mes.service.optask.OpTaskService;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskActionBO;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskFinishBO;
import com.iboot.pf.module.mes.service.workorder.WorkOrderService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 工序任务门面委托测试。
 *
 * <p>四个动作各一例，重点盯两处名字与语义都对不上的映射：完工的 {@code qtyGood} / {@code qtyScrap} 到
 * {@code qualifiedQty} / {@code scrapQty}（那两个字段现在是对账声明，不是数量入口），
 * 跳序的 {@code reasonType} 到 {@code skipReasonType}（丢了它跳序会被判成"没填原因分类"）。
 *
 * <p>另有一例守住 {@code applySchedule}：它是 APS 下达计划的入口，本任务不许改它的行为。
 */
class OpTaskApiImplTest extends BaseMockitoUnitTest {

    private static final String SOURCE_MODULE = "UI";
    private static final String SOURCE_BIZ_NO = "OT-0001";
    private static final Long OP_TASK_ID = 77L;
    private static final Long OPERATOR_ID = 501L;

    @InjectMocks
    private OpTaskApiImpl opTaskApi;

    @Mock
    private OpTaskService opTaskService;
    @Mock
    private WorkOrderService workOrderService;
    @Mock
    private MesOpTaskDomainService opTaskDomainService;

    @Test
    void testStartOpTask_delegatesWithOperatorAndCommandFields() {
        OpTaskStatusReqDTO reqDTO = statusReqDTO("UI:OT-0001:START_OP_TASK:0", null);

        opTaskApi.startOpTask(reqDTO);

        ArgumentCaptor<MesOpTaskActionBO> captor = ArgumentCaptor.forClass(MesOpTaskActionBO.class);
        verify(opTaskDomainService).startOpTask(captor.capture());
        MesOpTaskActionBO bo = captor.getValue();
        assertEquals(OP_TASK_ID, bo.getOpTaskId());
        // 操作者要原样带进去：开工门禁靠它校验技能资质，丢了会按"未指定作业人员"直接拦下
        assertEquals(OPERATOR_ID, bo.getOperatorId());
        assertEquals("UI:OT-0001:START_OP_TASK:0", bo.getIdempotencyKey());
        assertEquals(SOURCE_MODULE, bo.getSourceModule());
        assertEquals(SOURCE_BIZ_NO, bo.getSourceBizNo());
    }

    @Test
    void testPauseOpTask_delegatesWithReason() {
        OpTaskStatusReqDTO reqDTO = statusReqDTO("UI:OT-0001:PAUSE_OP_TASK:0", "设备故障停机");

        opTaskApi.pauseOpTask(reqDTO);

        ArgumentCaptor<MesOpTaskActionBO> captor = ArgumentCaptor.forClass(MesOpTaskActionBO.class);
        verify(opTaskDomainService).pauseOpTask(captor.capture());
        assertEquals(OP_TASK_ID, captor.getValue().getOpTaskId());
        // 暂停原因落在在制流水的 remark 上，是事后追停机时长的唯一依据
        assertEquals("设备故障停机", captor.getValue().getReason());
    }

    @Test
    void testFinishOpTask_mapsQtyToDeclaredReconciliation() {
        OpTaskFinishReqDTO reqDTO = new OpTaskFinishReqDTO();
        reqDTO.setIdempotencyKey("UI:OT-0001:FINISH_OP_TASK:0");
        reqDTO.setSourceModule(SOURCE_MODULE);
        reqDTO.setSourceBizNo(SOURCE_BIZ_NO);
        reqDTO.setOpTaskId(OP_TASK_ID);
        reqDTO.setOperatorId(OPERATOR_ID);
        reqDTO.setQtyGood(new BigDecimal("90"));
        reqDTO.setQtyScrap(new BigDecimal("10"));
        reqDTO.setActualEndTime(LocalDateTime.of(2026, 9, 5, 17, 30));

        opTaskApi.finishOpTask(reqDTO);

        ArgumentCaptor<MesOpTaskFinishBO> captor = ArgumentCaptor.forClass(MesOpTaskFinishBO.class);
        verify(opTaskDomainService).finishOpTask(captor.capture());
        MesOpTaskFinishBO bo = captor.getValue();
        assertEquals(OP_TASK_ID, bo.getOpTaskId());
        assertEquals(OPERATOR_ID, bo.getOperatorId());
        // 两个数量落到对账字段上：不搬过去，界面数据过期时的完工就无人拦
        assertEquals(0, new BigDecimal("90").compareTo(bo.getQualifiedQty()));
        assertEquals(0, new BigDecimal("10").compareTo(bo.getScrapQty()));
        assertEquals("UI:OT-0001:FINISH_OP_TASK:0", bo.getIdempotencyKey());
    }

    @Test
    void testSkipOpTask_mapsReasonTypeAndEffectRemark() {
        OpTaskSkipReqDTO reqDTO = new OpTaskSkipReqDTO();
        reqDTO.setIdempotencyKey("UI:OT-0001:SKIP_OP_TASK:0");
        reqDTO.setSourceModule(SOURCE_MODULE);
        reqDTO.setSourceBizNo(SOURCE_BIZ_NO);
        reqDTO.setOpTaskId(OP_TASK_ID);
        reqDTO.setReason("客户同意免去此道工序");
        reqDTO.setReasonType("PROCESS_CHANGE");
        reqDTO.setEffectRemark("不影响后序装配，终检加一项外观检查");

        opTaskApi.skipOpTask(reqDTO);

        ArgumentCaptor<MesOpSkipBO> captor = ArgumentCaptor.forClass(MesOpSkipBO.class);
        verify(opTaskDomainService).skipOpTask(captor.capture());
        MesOpSkipBO bo = captor.getValue();
        assertEquals(OP_TASK_ID, bo.getOpTaskId());
        assertEquals("客户同意免去此道工序", bo.getReason());
        // DTO 的 reasonType 与 BO 的 skipReasonType 是同一件事，名字不同必须显式搬
        assertEquals("PROCESS_CHANGE", bo.getSkipReasonType());
        assertEquals("不影响后序装配，终检加一项外观检查", bo.getEffectRemark());
        assertEquals("UI:OT-0001:SKIP_OP_TASK:0", bo.getIdempotencyKey());
    }

    @Test
    void testApplySchedule_stillDelegatesToCodegenService() {
        ApplyScheduleReqDTO reqDTO = new ApplyScheduleReqDTO();
        when(opTaskService.applySchedule(reqDTO)).thenReturn(3);

        assertEquals(3, opTaskApi.applySchedule(reqDTO));

        // 排程回写是 APS 下达计划的入口，本任务不接领域动作，改了 APS 的发布链就断
        verify(opTaskService).applySchedule(reqDTO);
        verifyNoInteractions(opTaskDomainService);
    }

    @Test
    void testQueryMethods_emptyIdsShortCircuit() {
        assertTrue(opTaskApi.getOpTaskListByOrderIds(List.of()).isEmpty());
        assertTrue(opTaskApi.getOpTaskListByRouteOpIds(List.of()).isEmpty());
        verifyNoInteractions(opTaskService, workOrderService);
    }

    @Test
    void testDataPermissionDisabledOnClassOnly() {
        DataPermission classLevel = OpTaskApiImpl.class.getAnnotation(DataPermission.class);
        assertNotNull(classLevel, "跨模块门面必须在类上关掉数据权限");
        assertFalse(classLevel.enable());
        for (Method method : OpTaskApiImpl.class.getDeclaredMethods()) {
            assertNull(method.getAnnotation(DataPermission.class), method.getName());
        }
    }

    private OpTaskStatusReqDTO statusReqDTO(String idempotencyKey, String reason) {
        OpTaskStatusReqDTO reqDTO = new OpTaskStatusReqDTO();
        reqDTO.setIdempotencyKey(idempotencyKey);
        reqDTO.setSourceModule(SOURCE_MODULE);
        reqDTO.setSourceBizNo(SOURCE_BIZ_NO);
        reqDTO.setOpTaskId(OP_TASK_ID);
        reqDTO.setOperatorId(OPERATOR_ID);
        reqDTO.setReason(reason);
        return reqDTO;
    }

}
