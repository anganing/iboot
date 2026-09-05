package com.iboot.pf.module.mes.service.wiptxn;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.wiptxn.bo.MesWipTxnBO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 在制流水统一写入封装测试：首次写入的字段口径、同幂等键只留一条、可选维度落 0 不落 null。
 */
@Import(MesWipTxnRecorder.class)
class MesWipTxnRecorderTest extends BaseDbUnitTest {

    @Resource
    private MesWipTxnRecorder recorder;
    @Resource
    private WipTxnMapper wipTxnMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;

    @MockitoBean
    private MesCodeService codeService;

    @BeforeEach
    void mockCodeRule() {
        // 编码规则在 infra，单测里按调用次数发号，验证 txn_no 确实由编码规则产出而非调用方拼串
        AtomicInteger seq = new AtomicInteger();
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) != null
                        ? invocation.getArgument(1)
                        : String.format("WIPTXN%04d", seq.incrementAndGet()));
    }

    @Test
    void testRecord_success() {
        // 准备：工单下达的流水，14 个业务列全部显式传入
        LocalDateTime txnTime = LocalDateTime.of(2026, 9, 4, 10, 30, 0);
        MesWipTxnBO bo = new MesWipTxnBO()
                .setProdOrderId(1001L)
                .setWorkOrderId(2001L)
                .setOpTaskId(3001L)
                .setTxnType(MesWipTxnTypeEnum.START)
                .setQty(new BigDecimal("100"))
                .setFromStatus(MesProdOrderStatusEnum.DRAFT.name())
                .setToStatus(MesProdOrderStatusEnum.RELEASED.name())
                .setBizType(MesBizTypeEnum.PROD_ORDER)
                .setBizNo("PO-0001")
                .setFromOpTaskId(4001L)
                .setToOpTaskId(4002L)
                .setTxnTime(txnTime)
                .setIdempotencyKey("UI:PO-0001:RELEASE_PROD_ORDER:0")
                .setRemark("工单下达");

        // 调用
        WipTxnDO created = recorder.record(bo);

        // 断言：落库值逐一核对，从库里读回而不是断言内存对象
        WipTxnDO txn = wipTxnMapper.selectById(created.getId());
        assertNotNull(txn);
        assertEquals("WIPTXN0001", txn.getTxnNo());
        assertEquals(1001L, txn.getProdOrderId());
        assertEquals(2001L, txn.getWorkOrderId());
        assertEquals(3001L, txn.getOpTaskId());
        assertEquals(MesWipTxnTypeEnum.START.name(), txn.getTxnType());
        assertEquals(0, new BigDecimal("100").compareTo(txn.getQty()));
        assertEquals(MesProdOrderStatusEnum.DRAFT.name(), txn.getFromStatus());
        assertEquals(MesProdOrderStatusEnum.RELEASED.name(), txn.getToStatus());
        assertEquals(MesBizTypeEnum.PROD_ORDER.name(), txn.getBizType());
        assertEquals("PO-0001", txn.getBizNo());
        assertEquals(4001L, txn.getFromOpTaskId());
        assertEquals(4002L, txn.getToOpTaskId());
        assertEquals(txnTime, txn.getTxnTime());
        assertEquals("UI:PO-0001:RELEASE_PROD_ORDER:0", txn.getIdempotencyKey());
        assertEquals("工单下达", txn.getRemark());
        assertEquals(1L, wipTxnMapper.selectCount());
    }

    @Test
    void testRecord_idempotent() {
        // 准备：同一个幂等键，第二次把数量和备注都改掉
        MesWipTxnBO first = buildOrderBO("UI:PO-0002:RELEASE_PROD_ORDER:0").setQty(new BigDecimal("100"));
        MesWipTxnBO second = buildOrderBO("UI:PO-0002:RELEASE_PROD_ORDER:0")
                .setQty(new BigDecimal("999"))
                .setRemark("重复提交");

        // 调用：同键两次
        WipTxnDO created = recorder.record(first);
        WipTxnDO repeated = recorder.record(second);

        // 断言：只产生一条记录，第二次返回原流水且内容未被覆盖
        assertEquals(1L, wipTxnMapper.selectCount());
        assertEquals(created.getId(), repeated.getId());
        assertEquals(created.getTxnNo(), repeated.getTxnNo());
        assertEquals(0, new BigDecimal("100").compareTo(repeated.getQty()));
    }

    @Test
    void testRecord_optionalDimensionFallbackToZero() {
        // 准备：可选维度与数量、时间全部不传
        MesWipTxnBO bo = buildOrderBO("UI:PO-0003:RELEASE_PROD_ORDER:0");

        // 调用
        WipTxnDO created = recorder.record(bo);

        // 断言：四个可选维度落 0 而不是 null，数量落 0，时间自动补当前时间
        WipTxnDO txn = wipTxnMapper.selectById(created.getId());
        assertEquals(MesConstants.NONE_ID, txn.getWorkOrderId());
        assertEquals(MesConstants.NONE_ID, txn.getOpTaskId());
        assertEquals(MesConstants.NONE_ID, txn.getFromOpTaskId());
        assertEquals(MesConstants.NONE_ID, txn.getToOpTaskId());
        assertEquals(0, BigDecimal.ZERO.compareTo(txn.getQty()));
        assertNotNull(txn.getTxnTime());
        assertNotNull(txn.getTxnNo());
    }

    @Test
    void testRecordStatusChange_dimensionsFromOpTask() {
        // 准备：工序任务挂在作业工单下，生产工单编号靠作业工单反查（mes_op_task 上没有这一列）
        WorkOrderDO workOrder = new WorkOrderDO().setWorkOrderNo("WO-0001").setProdOrderId(9001L);
        workOrderMapper.insert(workOrder);
        OpTaskDO opTask = new OpTaskDO()
                .setId(randomLongId())
                .setTaskNo("OPT-0001")
                .setWorkOrderId(workOrder.getId())
                .setStatus(MesOpTaskStatusEnum.READY.name());

        // 调用
        WipTxnDO created = recorder.recordStatusChange(opTask, MesWipTxnTypeEnum.START,
                MesOpTaskStatusEnum.READY.name(), MesOpTaskStatusEnum.RUNNING.name(),
                "UI:OPT-0001:START_OP_TASK:0");

        // 断言：三个追溯维度都从工序任务取，来源业务记工序任务
        WipTxnDO txn = wipTxnMapper.selectById(created.getId());
        assertEquals(9001L, txn.getProdOrderId());
        assertEquals(workOrder.getId(), txn.getWorkOrderId());
        assertEquals(opTask.getId(), txn.getOpTaskId());
        assertEquals(MesWipTxnTypeEnum.START.name(), txn.getTxnType());
        assertEquals(MesBizTypeEnum.OP_TASK.name(), txn.getBizType());
        assertEquals("OPT-0001", txn.getBizNo());
        assertEquals(MesOpTaskStatusEnum.READY.name(), txn.getFromStatus());
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), txn.getToStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(txn.getQty()));
        assertEquals(MesConstants.NONE_ID, txn.getFromOpTaskId());
    }

    @Test
    void testRecordReport_keepsStatusAndRecordsQty() {
        // 准备：报工不改状态
        WorkOrderDO workOrder = new WorkOrderDO().setWorkOrderNo("WO-0002").setProdOrderId(9002L);
        workOrderMapper.insert(workOrder);
        OpTaskDO opTask = new OpTaskDO()
                .setId(randomLongId())
                .setTaskNo("OPT-0002")
                .setWorkOrderId(workOrder.getId())
                .setStatus(MesOpTaskStatusEnum.RUNNING.name());

        // 调用
        WipTxnDO created = recorder.recordReport(opTask, new BigDecimal("30"), "UI:OPT-0002:REPORT:1");

        // 断言：类型为报工、数量为本次合格数、前后状态相同
        WipTxnDO txn = wipTxnMapper.selectById(created.getId());
        assertEquals(9002L, txn.getProdOrderId());
        assertEquals(MesWipTxnTypeEnum.REPORT.name(), txn.getTxnType());
        assertEquals(0, new BigDecimal("30").compareTo(txn.getQty()));
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), txn.getFromStatus());
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), txn.getToStatus());
    }

    /** 工单级流水的最小必填参数：可选维度一律不传，留给用例验证落 0。 */
    private MesWipTxnBO buildOrderBO(String idempotencyKey) {
        return new MesWipTxnBO()
                .setProdOrderId(1001L)
                .setTxnType(MesWipTxnTypeEnum.START)
                .setFromStatus(MesProdOrderStatusEnum.DRAFT.name())
                .setToStatus(MesProdOrderStatusEnum.RELEASED.name())
                .setBizType(MesBizTypeEnum.PROD_ORDER)
                .setBizNo("PO-0001")
                .setIdempotencyKey(idempotencyKey);
    }

}
