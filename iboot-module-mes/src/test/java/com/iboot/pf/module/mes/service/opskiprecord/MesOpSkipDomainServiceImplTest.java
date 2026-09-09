package com.iboot.pf.module.mes.service.opskiprecord;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.dal.dataobject.opskiprecord.OpSkipRecordDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.command.MesCommandRecordMapper;
import com.iboot.pf.module.mes.dal.mysql.opskiprecord.OpSkipRecordMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesSkipApproveStatusEnum;
import com.iboot.pf.module.mes.enums.MesSkipPolicyEnum;
import com.iboot.pf.module.mes.enums.MesSkipReasonTypeEnum;
import com.iboot.pf.module.mes.enums.MesSkipRecordStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandServiceImpl;
import com.iboot.pf.module.mes.service.opskiprecord.bo.MesOpSkipBO;
import com.iboot.pf.module.mes.service.opskiprecord.bo.MesRevokeSkipBO;
import com.iboot.pf.module.mes.service.optask.MesOpTaskDomainService;
import com.iboot.pf.module.mes.service.optask.MesOpTaskDomainServiceImpl;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateChecker;
import com.iboot.pf.module.mes.service.prodorder.MesFinishInboundService;
import com.iboot.pf.module.mes.service.prodreport.MesProgressPropagator;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_EFFECT_REMARK_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_NOT_ALLOWED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_REASON_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_RECORD_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_SKIP_RECORD_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 跳序测试。三种 {@code skip_policy} 各一例、撤销一例、跳序后后序推进一例，
 * 外加"需审批时工序状态不变"这条点名验收。
 *
 * <p>入口一律走 {@code MesOpTaskDomainService.skipOpTask}（跳序域不负责命令幂等，也不推后序），
 * 因此上下文里装的是 T1-13 的工序动作实现 + 本任务的跳序域，开工门禁与完工入库 mock 掉。
 *
 * <p>每个用例都显式给工序任务的 {@code skip_policy}：下达时一律落 {@code NOT_ALLOWED}
 * （{@code MesOrderExpandHelper} 写死的），不改它的话所有跳序都会被拒。
 */
@Import({MesOpSkipDomainServiceImpl.class, MesOpTaskDomainServiceImpl.class, MesProgressPropagator.class,
        MesCommandServiceImpl.class, MesWipTxnRecorder.class})
class MesOpSkipDomainServiceImplTest extends BaseDbUnitTest {

    private static final String SOURCE_MODULE = "UI";
    private static final String ORDER_NO = "PO-0001";
    private static final BigDecimal PLAN_QTY = new BigDecimal("100");
    private static final String GROUP_A = "A";
    private static final Long WC_MACHINE = 201L;
    private static final Long WC_PAINT = 202L;
    private static final Long WC_MARK = 203L;
    private static final Long OPERATOR_ID = 77L;
    private static final String SKIP_REASON = "该型号不需要这道工序";
    private static final String EFFECT_REMARK = "后序尺寸不受影响，免首检";

    /** 工序任务号计数器，保证同一个用例里落的任务号互不相同。 */
    private final AtomicInteger taskSeq = new AtomicInteger();

    @Resource
    private MesOpTaskDomainService opTaskDomainService;
    @Resource
    private MesOpSkipDomainService skipDomainService;
    @Resource
    private OpSkipRecordMapper opSkipRecordMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private WipTxnMapper wipTxnMapper;
    @Resource
    private MesCommandRecordMapper commandRecordMapper;

    @MockitoBean
    private MesStartGateChecker startGateChecker;
    @MockitoBean
    private MesFinishInboundService finishInboundService;
    @MockitoBean
    private MesCodeService codeService;

    @BeforeEach
    void mockExternalDependencies() {
        Map<String, AtomicInteger> counters = new HashMap<>();
        when(codeService.generate(anyString(), any())).thenAnswer(invocation -> {
            String ruleCode = invocation.getArgument(0);
            return invocation.getArgument(1) != null ? invocation.getArgument(1)
                    : ruleCode + "-" + counters.computeIfAbsent(ruleCode, key -> new AtomicInteger())
                    .incrementAndGet();
        });
    }

    @Test
    void testSkipOpTask_notAllowedRejected() {
        // 准备：策略是不允许跳序，这也是下达时的缺省值
        OpTaskDO task = readyTask(MesSkipPolicyEnum.NOT_ALLOWED);

        assertServiceException(() -> opTaskDomainService.skipOpTask(skipBO(task, SKIP_REASON, null)),
                OP_SKIP_NOT_ALLOWED);

        // 断言：连记录都不留。留一条会让人以为跳序申请上去了，其实根本不允许
        assertEquals(0L, opSkipRecordMapper.selectCount());
        assertEquals(MesOpTaskStatusEnum.READY.name(), opTaskMapper.selectById(task.getId()).getStatus());
        assertEquals(0L, wipTxnMapper.selectCount());
        assertEquals(0L, commandRecordMapper.selectCount());
    }

    @Test
    void testSkipOpTask_unknownPolicyRejected() {
        // 准备：策略值是脏数据。库列默认就是 NOT_ALLOWED，未识别的取值按拒绝处理——
        // 跳序是绕过工艺，配置缺失时放行比拦下危险得多
        OpTaskDO task = readyTask(MesSkipPolicyEnum.NOT_ALLOWED);
        opTaskMapper.updateById(new OpTaskDO().setId(task.getId()).setSkipPolicy("WHATEVER"));

        assertServiceException(() -> opTaskDomainService.skipOpTask(skipBO(task, SKIP_REASON, null)),
                OP_SKIP_NOT_ALLOWED);
        assertEquals(0L, opSkipRecordMapper.selectCount());
    }

    @Test
    void testSkipOpTask_allowWithReasonTakesEffect() {
        // 准备：填原因即可跳序
        OpTaskDO task = readyTask(MesSkipPolicyEnum.ALLOW_WITH_REASON);

        // 调用
        opTaskDomainService.skipOpTask(skipBO(task, SKIP_REASON, null));

        // 断言：记录已生效 + 无需审批，实际跳过时间落下来
        OpSkipRecordDO record = onlyRecord();
        assertEquals(task.getId(), record.getOpTaskId());
        assertEquals(MesSkipRecordStatusEnum.EFFECTIVE.name(), record.getStatus());
        assertEquals(MesSkipApproveStatusEnum.NOT_REQUIRED.name(), record.getApproveStatus());
        assertEquals(MesSkipReasonTypeEnum.NOT_APPLICABLE.name(), record.getSkipReasonType());
        assertEquals(SKIP_REASON, record.getSkipReason());
        assertEquals(OPERATOR_ID, record.getOperatorId());
        assertEquals(MesConstants.NONE_ID, record.getApprovedBy());
        assertNotNull(record.getSkipTime());
        // 工序转已跳过，版本自增（走的是带 version 条件的更新）
        OpTaskDO skipped = opTaskMapper.selectById(task.getId());
        assertEquals(MesOpTaskStatusEnum.SKIPPED.name(), skipped.getStatus());
        assertEquals(MesConstants.INIT_VERSION + 1, skipped.getVersion());
        // SKIP 流水：数量记 0，前后状态与前后序工序都落上
        WipTxnDO txn = onlySkipTxn();
        assertEquals(MesOpTaskStatusEnum.READY.name(), txn.getFromStatus());
        assertEquals(MesOpTaskStatusEnum.SKIPPED.name(), txn.getToStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(txn.getQty()));
        assertEquals(MesBizTypeEnum.OP_TASK.name(), txn.getBizType());
        assertEquals(task.getTaskNo(), txn.getBizNo());
        assertEquals(SKIP_REASON, txn.getRemark());
        // 幂等键按 UI:{taskNo}:SKIP_OP_TASK:0 派生
        assertEquals(1L, commandRecordMapper.selectCount());
        assertEquals(SOURCE_MODULE + ":" + task.getTaskNo() + ":SKIP_OP_TASK:0",
                commandRecordMapper.selectList().get(0).getIdempotencyKey());
    }

    @Test
    void testSkipOpTask_approvalRequiredKeepsOpTaskStatus() {
        // 准备（验收点名的构造）：需审批才能跳序
        OpTaskDO task = readyTask(MesSkipPolicyEnum.APPROVAL_REQUIRED);

        // 调用
        opTaskDomainService.skipOpTask(skipBO(task, SKIP_REASON, EFFECT_REMARK));

        // 断言（验收点名）：工序任务状态一行不动。审批没过就把工序置成已跳过，
        // 等于审批人还没看车间就已经跳过去了，驳回也追不回来
        OpTaskDO unchanged = opTaskMapper.selectById(task.getId());
        assertEquals(MesOpTaskStatusEnum.READY.name(), unchanged.getStatus());
        assertEquals(MesConstants.INIT_VERSION, unchanged.getVersion());
        // 记录落待生效 + 待审批，实际跳过时间留空（那一刻工序还没跳过，填上就是假数据）
        OpSkipRecordDO record = onlyRecord();
        assertEquals(MesSkipRecordStatusEnum.PENDING.name(), record.getStatus());
        assertEquals(MesSkipApproveStatusEnum.PENDING.name(), record.getApproveStatus());
        assertNull(record.getSkipTime());
        assertEquals(EFFECT_REMARK, record.getEffectRemark());
        // 还没生效，不写在制流水
        assertEquals(0L, wipTxnMapper.selectCount());
    }

    @Test
    void testSkipOpTask_approvalRequiredEffectRemarkRequired() {
        // 准备：需审批却不写影响说明。审批人只看到一句"设备故障"没人敢批
        OpTaskDO task = readyTask(MesSkipPolicyEnum.APPROVAL_REQUIRED);

        assertServiceException(() -> opTaskDomainService.skipOpTask(skipBO(task, SKIP_REASON, null)),
                OP_SKIP_EFFECT_REMARK_REQUIRED);
        assertEquals(0L, opSkipRecordMapper.selectCount());
    }

    @Test
    void testSkipOpTask_reasonRequired() {
        // 准备：原因说明为空。待审批的记录没有原因，审批人无从下手
        OpTaskDO task = readyTask(MesSkipPolicyEnum.ALLOW_WITH_REASON);

        assertServiceException(() -> opTaskDomainService.skipOpTask(skipBO(task, null, null)),
                OP_SKIP_REASON_REQUIRED);
        assertEquals(0L, opSkipRecordMapper.selectCount());
    }

    @Test
    void testSkipOpTask_runningTaskRejected() {
        // 准备：已开工的工序。状态机里只有 PENDING 与 READY 有 skip 出边
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null,
                MesOpTaskStatusEnum.RUNNING, MesSkipPolicyEnum.ALLOW_WITH_REASON);

        assertServiceException(() -> opTaskDomainService.skipOpTask(skipBO(task, SKIP_REASON, null)),
                OP_TASK_STATUS_INVALID);

        // 断言：状态机先裁决再落记录，因此连待审批记录都不该留——
        // 留了会让人以为在等审批，其实这道工序正在加工
        assertEquals(0L, opSkipRecordMapper.selectCount());
        assertEquals(MesOpTaskStatusEnum.RUNNING.name(), opTaskMapper.selectById(task.getId()).getStatus());
    }

    @Test
    void testSkipOpTask_pendingTaskAllowed() {
        // 准备：待排产的工序也能跳（状态机里 PENDING 有 skip 出边）
        OpTaskDO task = insertTask(runningWorkOrder(), 10, "下料", null,
                MesOpTaskStatusEnum.PENDING, MesSkipPolicyEnum.ALLOW_WITH_REASON);

        opTaskDomainService.skipOpTask(skipBO(task, SKIP_REASON, null));

        assertEquals(MesOpTaskStatusEnum.SKIPPED.name(), opTaskMapper.selectById(task.getId()).getStatus());
        assertEquals(MesOpTaskStatusEnum.PENDING.name(), onlySkipTxn().getFromStatus());
    }

    @Test
    void testSkipOpTask_idempotent() {
        // 准备
        OpTaskDO task = readyTask(MesSkipPolicyEnum.ALLOW_WITH_REASON);

        // 调用：同键两次。第二次若真执行会多一条跳过记录，工序上就有两次跳序说不清哪次为准
        opTaskDomainService.skipOpTask(skipBO(task, SKIP_REASON, null));
        opTaskDomainService.skipOpTask(skipBO(task, SKIP_REASON, null));

        assertEquals(1L, opSkipRecordMapper.selectCount());
        assertEquals(1L, commandRecordMapper.selectCount());
        assertEquals(MesConstants.INIT_VERSION + 1, opTaskMapper.selectById(task.getId()).getVersion());
        // 跳序生效会触发上卷（doSkip 接 MesProgressPropagator）：本段唯一工序被跳过，作业工单
        // 按"本段全部完工或跳序"同步收尾，因此流水是 1 条 SKIP + 1 条作业工单完工；幂等第二次不追加
        assertEquals(2L, wipTxnMapper.selectCount());
        assertEquals(MesWorkOrderStatusEnum.DONE.name(),
                workOrderMapper.selectById(task.getWorkOrderId()).getStatus());
    }

    @Test
    void testSkipOpTask_parallelGroupNextAdvancedAsWholeGroup() {
        // 准备：04 原型链 加工 -> （喷漆 ‖ 标识）-> 装配，喷漆与标识在两张不同作业工单上。
        // 跳掉加工，后序整组要一起转待开工——next_task_id 是单值列，只指向组内代表工序
        ParallelChain chain = parallelChain();

        // 调用
        opTaskDomainService.skipOpTask(skipBO(chain.machine, SKIP_REASON, null));

        // 断言：并行组两道都转待开工，一道都不能落下（PENDING 没有 start 出边，落下的那道谁也干不了）
        assertEquals(MesOpTaskStatusEnum.SKIPPED.name(),
                opTaskMapper.selectById(chain.machine.getId()).getStatus());
        assertEquals(MesOpTaskStatusEnum.READY.name(), opTaskMapper.selectById(chain.paint.getId()).getStatus());
        assertEquals(MesOpTaskStatusEnum.READY.name(), opTaskMapper.selectById(chain.mark.getId()).getStatus());
        // 装配还在后面，不受这一跳影响
        assertEquals(MesOpTaskStatusEnum.PENDING.name(),
                opTaskMapper.selectById(chain.assemble.getId()).getStatus());
        // 两条 MOVE 流水，一条 SKIP 流水
        assertEquals(2, txnsOfType(MesWipTxnTypeEnum.MOVE).size());
        assertEquals(1, txnsOfType(MesWipTxnTypeEnum.SKIP).size());
    }

    @Test
    void testRevokeSkip_effectiveBackToReady() {
        // 准备：一道已经跳掉的工序，后序已被带到待开工
        OpTaskDO task = readyTask(MesSkipPolicyEnum.ALLOW_WITH_REASON);
        opTaskDomainService.skipOpTask(skipBO(task, SKIP_REASON, null));
        OpSkipRecordDO record = onlyRecord();

        // 调用
        skipDomainService.revokeSkip(revokeBO(task, record, "跳错了"));

        // 断言：记录置已撤销，工序从已跳过回到待开工
        assertEquals(MesSkipRecordStatusEnum.REVOKED.name(),
                opSkipRecordMapper.selectById(record.getId()).getStatus());
        OpTaskDO reverted = opTaskMapper.selectById(task.getId());
        assertEquals(MesOpTaskStatusEnum.READY.name(), reverted.getStatus());
        // 两次状态变更，版本各自 +1
        assertEquals(MesConstants.INIT_VERSION + 2, reverted.getVersion());
        // 反向流水：SKIP 类型，from/to 正好与跳序那条相反
        List<WipTxnDO> skipTxns = txnsOfType(MesWipTxnTypeEnum.SKIP);
        assertEquals(2, skipTxns.size());
        WipTxnDO reverse = skipTxns.stream()
                .filter(txn -> MesOpTaskStatusEnum.SKIPPED.name().equals(txn.getFromStatus()))
                .findFirst().orElseThrow();
        assertEquals(MesOpTaskStatusEnum.READY.name(), reverse.getToStatus());
        assertEquals("跳错了", reverse.getRemark());
    }

    @Test
    void testRevokeSkip_pendingRecordOnlyFlipsRecord() {
        // 准备：待审批的记录。撤回申请时工序从来没变过 SKIPPED
        OpTaskDO task = readyTask(MesSkipPolicyEnum.APPROVAL_REQUIRED);
        opTaskDomainService.skipOpTask(skipBO(task, SKIP_REASON, EFFECT_REMARK));
        OpSkipRecordDO record = onlyRecord();

        skipDomainService.revokeSkip(revokeBO(task, record, "不跳了"));

        // 断言：记录置已撤销，工序状态与版本都不动
        assertEquals(MesSkipRecordStatusEnum.REVOKED.name(),
                opSkipRecordMapper.selectById(record.getId()).getStatus());
        OpTaskDO unchanged = opTaskMapper.selectById(task.getId());
        assertEquals(MesOpTaskStatusEnum.READY.name(), unchanged.getStatus());
        assertEquals(MesConstants.INIT_VERSION, unchanged.getVersion());
        // 撤销也留痕，前后状态相同，记的是"这道工序上发生过一次撤销"
        assertEquals(1, txnsOfType(MesWipTxnTypeEnum.SKIP).size());
    }

    @Test
    void testRevokeSkip_idempotent() {
        // 准备
        OpTaskDO task = readyTask(MesSkipPolicyEnum.ALLOW_WITH_REASON);
        opTaskDomainService.skipOpTask(skipBO(task, SKIP_REASON, null));
        OpSkipRecordDO record = onlyRecord();

        // 调用：同键两次。第二次若真执行，工序会被再推一次版本、流水多一条
        skipDomainService.revokeSkip(revokeBO(task, record, "跳错了"));
        skipDomainService.revokeSkip(revokeBO(task, record, "跳错了"));

        assertEquals(MesConstants.INIT_VERSION + 2, opTaskMapper.selectById(task.getId()).getVersion());
        assertEquals(2, txnsOfType(MesWipTxnTypeEnum.SKIP).size());
        // 跳序 + 撤销各一条命令记录
        assertEquals(2L, commandRecordMapper.selectCount());
    }

    @Test
    void testRevokeSkip_alreadyRevokedRejected() {
        // 准备：换个键再撤一次同一条已撤销的记录
        OpTaskDO task = readyTask(MesSkipPolicyEnum.ALLOW_WITH_REASON);
        opTaskDomainService.skipOpTask(skipBO(task, SKIP_REASON, null));
        OpSkipRecordDO record = onlyRecord();
        skipDomainService.revokeSkip(revokeBO(task, record, "跳错了"));

        MesRevokeSkipBO bo = revokeBO(task, record, "再撤一次");
        bo.setIdempotencyKey("UI:" + task.getTaskNo() + ":REVOKE_OP_SKIP:1");

        // 断言：记录已是终态，撤不动。条件更新 0 行就是它的乐观锁
        assertServiceException(() -> skipDomainService.revokeSkip(bo), OP_SKIP_RECORD_STATUS_INVALID);
        assertEquals(MesOpTaskStatusEnum.READY.name(), opTaskMapper.selectById(task.getId()).getStatus());
    }

    @Test
    void testRevokeSkip_recordNotExists() {
        MesRevokeSkipBO bo = new MesRevokeSkipBO();
        bo.setSkipRecordId(randomLongId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("OT-0001");

        assertServiceException(() -> skipDomainService.revokeSkip(bo), OP_SKIP_RECORD_NOT_EXISTS);
    }

    @Test
    void testSkipOpTask_serialNextAdvancedAndSkippedCountsAsDone() {
        // 准备：下料（跳序）-> 折弯（待排产）。跳掉的工序对后序等价于已完工，
        // 这是"跳序是把这道工序从链上摘掉"的全部含义
        WorkOrderDO workOrder = runningWorkOrder();
        OpTaskDO cut = insertTask(workOrder, 10, "下料", null,
                MesOpTaskStatusEnum.READY, MesSkipPolicyEnum.ALLOW_WITH_REASON);
        OpTaskDO bend = insertTask(workOrder, 20, "折弯", null,
                MesOpTaskStatusEnum.PENDING, MesSkipPolicyEnum.NOT_ALLOWED);
        chain(cut, MesConstants.NONE_ID, bend.getId());
        chain(bend, cut.getId(), MesConstants.NONE_ID);

        opTaskDomainService.skipOpTask(skipBO(cut, SKIP_REASON, null));

        assertEquals(MesOpTaskStatusEnum.READY.name(), opTaskMapper.selectById(bend.getId()).getStatus());
        WipTxnDO move = txnsOfType(MesWipTxnTypeEnum.MOVE).get(0);
        assertEquals(cut.getId(), move.getFromOpTaskId());
        assertEquals(bend.getId(), move.getToOpTaskId());
        assertTrue(move.getRemark().contains("下料"));
    }

    /** 一道待开工的工序，连带上两级都在产。 */
    private OpTaskDO readyTask(MesSkipPolicyEnum policy) {
        return insertTask(runningWorkOrder(), 10, "下料", null, MesOpTaskStatusEnum.READY, policy);
    }

    private WorkOrderDO runningWorkOrder() {
        return insertWorkOrder(insertOrder(), WC_MACHINE);
    }

    private ProdOrderDO insertOrder() {
        ProdOrderDO order = ProdOrderDO.builder()
                .orderNo(ORDER_NO)
                .demandId(MesConstants.NONE_ID)
                .plantId(1L)
                .workshopId(2L)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .matId(1L)
                .matCode("MAT-001")
                .qty(PLAN_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .uomId(1L)
                .priority(0)
                .kitStatus(MesKitStatusEnum.FULL_KIT.name())
                .status(MesProdOrderStatusEnum.RUNNING.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        prodOrderMapper.insert(order);
        return order;
    }

    private WorkOrderDO insertWorkOrder(ProdOrderDO order, Long wcId) {
        WorkOrderDO workOrder = WorkOrderDO.builder()
                .workOrderNo("WO-" + wcId)
                .prodOrderId(order.getId())
                .prodOrderNo(order.getOrderNo())
                .plantId(order.getPlantId())
                .workshopId(order.getWorkshopId())
                .wcId(wcId)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .plannedQty(PLAN_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .kitStatus(MesKitStatusEnum.FULL_KIT.name())
                .status(MesWorkOrderStatusEnum.RUNNING.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        workOrderMapper.insert(workOrder);
        return workOrder;
    }

    private OpTaskDO insertTask(WorkOrderDO workOrder, int seq, String opName, String parallelGroup,
                                MesOpTaskStatusEnum status, MesSkipPolicyEnum skipPolicy) {
        OpTaskDO task = OpTaskDO.builder()
                .taskNo("OT-" + taskSeq.incrementAndGet())
                .workOrderId(workOrder.getId())
                .workOrderNo(workOrder.getWorkOrderNo())
                .routeOpId(100L + seq)
                .opId(40L + seq)
                .opCode("OP" + seq)
                .opName(opName)
                .seq(seq)
                .parallelGroup(parallelGroup)
                .prevTaskId(MesConstants.NONE_ID)
                .nextTaskId(MesConstants.NONE_ID)
                .skipPolicy(skipPolicy.name())
                .keyFlag(Boolean.FALSE)
                .plannedQty(PLAN_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .resId(MesConstants.NONE_ID)
                .status(status.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        opTaskMapper.insert(task);
        return task;
    }

    /** 串前后序。前后序是单值列，并行组指向组内代表工序，与下达时的串联口径一致。 */
    private void chain(OpTaskDO task, Long prevTaskId, Long nextTaskId) {
        task.setPrevTaskId(prevTaskId).setNextTaskId(nextTaskId);
        opTaskMapper.updateById(new OpTaskDO().setId(task.getId())
                .setPrevTaskId(prevTaskId).setNextTaskId(nextTaskId));
    }

    /** 04 文档"工序串联规则"的原型链：加工 -&gt; （喷漆 ‖ 标识）-&gt; 装配，并行组跨两张作业工单。 */
    private ParallelChain parallelChain() {
        ProdOrderDO order = insertOrder();
        WorkOrderDO machineWorkOrder = insertWorkOrder(order, WC_MACHINE);
        WorkOrderDO paintWorkOrder = insertWorkOrder(order, WC_PAINT);
        WorkOrderDO markWorkOrder = insertWorkOrder(order, WC_MARK);
        ParallelChain chain = new ParallelChain();
        chain.machine = insertTask(machineWorkOrder, 20, "加工", null,
                MesOpTaskStatusEnum.READY, MesSkipPolicyEnum.ALLOW_WITH_REASON);
        chain.paint = insertTask(paintWorkOrder, 30, "喷漆", GROUP_A,
                MesOpTaskStatusEnum.PENDING, MesSkipPolicyEnum.NOT_ALLOWED);
        chain.mark = insertTask(markWorkOrder, 30, "标识", GROUP_A,
                MesOpTaskStatusEnum.PENDING, MesSkipPolicyEnum.NOT_ALLOWED);
        chain.assemble = insertTask(machineWorkOrder, 40, "装配", null,
                MesOpTaskStatusEnum.PENDING, MesSkipPolicyEnum.NOT_ALLOWED);
        // next_task_id 指向并行步骤的代表工序（组内先建的那道），与下达时的串联口径一致
        chain(chain.machine, MesConstants.NONE_ID, chain.paint.getId());
        chain(chain.paint, chain.machine.getId(), chain.assemble.getId());
        chain(chain.mark, chain.machine.getId(), chain.assemble.getId());
        chain(chain.assemble, chain.paint.getId(), MesConstants.NONE_ID);
        return chain;
    }

    private MesOpSkipBO skipBO(OpTaskDO task, String reason, String effectRemark) {
        MesOpSkipBO bo = new MesOpSkipBO();
        bo.setOpTaskId(task.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(task.getTaskNo());
        bo.setOperatorId(OPERATOR_ID);
        bo.setSkipReasonType(MesSkipReasonTypeEnum.NOT_APPLICABLE.name());
        bo.setReason(reason);
        bo.setEffectRemark(effectRemark);
        return bo;
    }

    private MesRevokeSkipBO revokeBO(OpTaskDO task, OpSkipRecordDO record, String reason) {
        MesRevokeSkipBO bo = new MesRevokeSkipBO();
        bo.setSkipRecordId(record.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(task.getTaskNo());
        bo.setReason(reason);
        return bo;
    }

    private OpSkipRecordDO onlyRecord() {
        List<OpSkipRecordDO> records = opSkipRecordMapper.selectList();
        assertEquals(1, records.size());
        return records.get(0);
    }

    private WipTxnDO onlySkipTxn() {
        List<WipTxnDO> txns = txnsOfType(MesWipTxnTypeEnum.SKIP);
        assertEquals(1, txns.size());
        return txns.get(0);
    }

    private List<WipTxnDO> txnsOfType(MesWipTxnTypeEnum txnType) {
        return wipTxnMapper.selectList().stream()
                .filter(txn -> txnType.name().equals(txn.getTxnType())).toList();
    }

    /** 并行原型链的载体，避免用下标取值。 */
    private static final class ParallelChain {

        private OpTaskDO machine;
        private OpTaskDO paint;
        private OpTaskDO mark;
        private OpTaskDO assemble;

    }

}
