package com.iboot.pf.module.mes.service.disposition;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.bpm.api.task.BpmProcessInstanceApi;
import com.iboot.pf.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import com.iboot.pf.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.iboot.pf.module.mes.dal.dataobject.disposition.DispositionDO;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import com.iboot.pf.module.mes.dal.mysql.disposition.DispositionMapper;
import com.iboot.pf.module.mes.dal.mysql.qctask.QcTaskMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDispositionStatusEnum;
import com.iboot.pf.module.mes.enums.MesDispositionTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.service.bpm.MesBpmApprovalActionExecutor;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandServiceImpl;
import com.iboot.pf.module.mes.service.disposition.bo.MesDispositionActionBO;
import com.iboot.pf.module.mes.service.disposition.handler.ConcessionHandler;
import com.iboot.pf.module.mes.service.disposition.handler.DispositionInventoryResolver;
import com.iboot.pf.module.mes.service.disposition.handler.ScrapHandler;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryBalanceRespDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.QualityChangeReqDTO;
import com.iboot.pf.module.wms.api.outorder.OutOrderApi;
import com.iboot.pf.module.wms.api.outorder.dto.CreateOutOrderReqDTO;
import com.iboot.pf.module.wms.enums.WmsOutBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.DISPOSITION_CONCESSION_WITHOUT_REASON;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 让步接收与报废处置的审批接入测试（T3-03 / T3-04）。
 *
 * <p>要证明的是阶段 3 的立项目标本身：<b>审批期间不产生库存事实，审批通过才过账，重复投递回调不产生
 * 第二笔过账</b>。
 *
 * <p>"库存分文未动"的断言口径说明：MES 单测上下文里没有 {@code wms_inv_txn} 表（跨模块表不在同一个
 * H2 库里），因此按等价口径断言 <b>WMS 过账门面的调用次数</b>——让步接收的质量状态过账只有一个出口
 * {@code InventoryApi.changeQualityStatus}，报废出库只有 {@code OutOrderApi.createOutOrder} +
 * {@code confirmOutOrder}。门面上零调用与 {@code wms_inv_txn} 零新增是同一件事，且跨模块只走门面
 * 本来就是红线，不存在"绕过门面直接写表"的第二条路径。账实一致的真实过账由阶段 6 跨模块集成测试覆盖。
 *
 * <p>处置执行器装真实的两个（{@link ConcessionHandler}、{@link ScrapHandler}），审批通过之后走的
 * 就是生产的同一条链路；WMS 侧与编码规则 mock 掉。审批终态回写后的领域动作被
 * {@link MesBpmApprovalActionExecutor} 延迟到事务提交后，测试里方法返回即已执行完，断言不用等待。
 */
@Import({MesDispositionDomainServiceImpl.class, MesCommandServiceImpl.class, MesBpmApprovalActionExecutor.class,
        DispositionInventoryResolver.class, ConcessionHandler.class, ScrapHandler.class})
class MesDispositionApprovalServiceImplTest extends BaseDbUnitTest {

    private static final Long TASK_ID = 1L;
    private static final Long MAT_ID = 100L;
    private static final Long BATCH_ID = 10L;
    private static final String INSTANCE_ID = "concession-process-1";
    private static final BigDecimal QTY = new BigDecimal("5");

    @Resource
    private MesDispositionDomainService dispositionDomainService;
    @Resource
    private DispositionMapper dispositionMapper;
    @Resource
    private QcTaskMapper qcTaskMapper;

    @MockitoBean
    private MesCodeService codeService;
    @MockitoBean
    private BpmProcessInstanceApi processInstanceApi;
    @MockitoBean
    private InventoryApi inventoryApi;
    @MockitoBean
    private OutOrderApi outOrderApi;

    /** 质量状态过账次数，用计数而不是 verify 次数，好在断言失败时看到实际值。 */
    private final AtomicInteger qualityPostCount = new AtomicInteger();
    /** 报废出库的建单与过账次数。 */
    private final AtomicInteger scrapCreateCount = new AtomicInteger();
    private final AtomicInteger scrapConfirmCount = new AtomicInteger();

    @BeforeEach
    void mockExternalDependencies() {
        qualityPostCount.set(0);
        scrapCreateCount.set(0);
        scrapConfirmCount.set(0);
        when(codeService.generate(anyString(), any())).thenAnswer(invocation ->
                invocation.getArgument(1) == null ? "DSP0001" : invocation.getArgument(1));
        // 单测没有登录用户，流程发起人传 null 即可，重点是 businessKey 与流程 key
        when(processInstanceApi.createProcessInstance(any(), any())).thenReturn(INSTANCE_ID);
        when(inventoryApi.getInventoryBalanceList(any())).thenAnswer(invocation ->
                List.of(balance()));
        when(inventoryApi.changeQualityStatus(any())).thenAnswer(invocation -> {
            qualityPostCount.incrementAndGet();
            return 9001L;
        });
        when(outOrderApi.createOutOrder(any())).thenAnswer(invocation -> {
            scrapCreateCount.incrementAndGet();
            return 8001L;
        });
        org.mockito.Mockito.doAnswer(invocation -> {
            scrapConfirmCount.incrementAndGet();
            return null;
        }).when(outOrderApi).confirmOutOrder(any());
    }

    /** 铺一张草稿处置单与它引用的质检任务。 */
    private DispositionDO insertDisposition(MesDispositionTypeEnum type, String remark) {
        qcTaskMapper.insert(new QcTaskDO().setId(TASK_ID)
                .setTaskNo("QCT-0001")
                .setStatus(MesQcTaskStatusEnum.DONE.name())
                .setMatId(MAT_ID)
                .setBatchId(BATCH_ID)
                .setUomId(MesConstants.NONE_ID)
                .setQtyRejected(QTY));
        DispositionDO disposition = new DispositionDO()
                .setDispositionNo("DSP-" + type.name())
                .setTaskId(TASK_ID)
                .setDispositionType(type.name())
                .setQty(QTY)
                .setStatus(MesDispositionStatusEnum.DRAFT.name())
                .setApprovedBy(MesConstants.NONE_ID)
                .setRemark(remark);
        dispositionMapper.insert(disposition);
        return dispositionMapper.selectById(disposition.getId());
    }

    private InventoryBalanceRespDTO balance() {
        InventoryBalanceRespDTO balance = new InventoryBalanceRespDTO();
        balance.setInvOrgId(1L);
        balance.setPlantId(1L);
        balance.setWhId(1L);
        balance.setLocId(1L);
        balance.setMatId(MAT_ID);
        balance.setBatchId(BATCH_ID);
        balance.setContainerId(0L);
        balance.setProjectId(0L);
        balance.setProjectTaskId(0L);
        balance.setStockScope("COMMON");
        balance.setQualityStatus(WmsQualityStatusEnum.QC_PENDING.name());
        balance.setMatCode("M-001");
        balance.setQtyOnhand(QTY);
        return balance;
    }

    private MesDispositionActionBO actionBO(Long dispositionId) {
        MesDispositionActionBO bo = new MesDispositionActionBO();
        bo.setDispositionId(dispositionId);
        bo.setSourceModule("UI");
        return bo;
    }

    // ==================== T3-03 让步接收 ====================

    /** 验收①：提交后进入审批中，库存分文未动。 */
    @Test
    void testSubmitConcession_startsApprovalWithoutTouchingInventory() {
        DispositionDO disposition = insertDisposition(MesDispositionTypeEnum.CONCESSION, "客户已书面确认可让步");

        dispositionDomainService.submitDisposition(actionBO(disposition.getId()));

        DispositionDO submitted = dispositionMapper.selectById(disposition.getId());
        assertEquals(MesDispositionStatusEnum.SUBMITTED.name(), submitted.getStatus());
        assertEquals(BpmProcessInstanceStatusEnum.RUNNING.getStatus(), submitted.getApprovalStatus());
        assertEquals(INSTANCE_ID, submitted.getProcessInstanceId());
        // 审批中不产生任何库存事实：质量状态过账一次都没调
        assertEquals(0, qualityPostCount.get(), "审批期间质量状态过账必须为 0 次");
        assertNull(submitted.getTargetBizNo(), "审批期间不能留下联动业务凭据");
        // 流程 key 与 businessKey 都是硬约定：businessKey 传处置单 id，回调靠它找回单据
        ArgumentCaptor<BpmProcessInstanceCreateReqDTO> captor =
                ArgumentCaptor.forClass(BpmProcessInstanceCreateReqDTO.class);
        verify(processInstanceApi).createProcessInstance(any(), captor.capture());
        assertEquals(MesDispositionDomainServiceImpl.PROCESS_KEY_CONCESSION,
                captor.getValue().getProcessDefinitionKey());
        assertEquals(String.valueOf(disposition.getId()), captor.getValue().getBusinessKey());
    }

    /** 验收⑤：让步接收必须留存依据，remark 为空直接拒。 */
    @Test
    void testSubmitConcession_blankRemarkRejected() {
        DispositionDO disposition = insertDisposition(MesDispositionTypeEnum.CONCESSION, " ");

        assertServiceException(() -> dispositionDomainService.submitDisposition(actionBO(disposition.getId())),
                DISPOSITION_CONCESSION_WITHOUT_REASON);

        // 校验在推状态之前：被拦下时单据还停在草稿，什么都没发生
        DispositionDO unchanged = dispositionMapper.selectById(disposition.getId());
        assertEquals(MesDispositionStatusEnum.DRAFT.name(), unchanged.getStatus());
        assertNull(unchanged.getProcessInstanceId());
        verify(processInstanceApi, never()).createProcessInstance(any(), any());
    }

    /** 验收②：审批通过后质量状态过账产生，单据走到 DONE。 */
    @Test
    void testConcessionApproved_postsQualityChangeOnce() {
        Long id = insertDisposition(MesDispositionTypeEnum.CONCESSION, "非关键特性超差").getId();
        dispositionDomainService.submitDisposition(actionBO(id));

        dispositionDomainService.updateConcessionApprovalStatus(id, INSTANCE_ID,
                BpmProcessInstanceStatusEnum.APPROVE.getStatus(), "同意让步");

        DispositionDO approved = dispositionMapper.selectById(id);
        assertEquals(BpmProcessInstanceStatusEnum.APPROVE.getStatus(), approved.getApprovalStatus());
        assertNotNull(approved.getApprovalTime());
        assertEquals("同意让步", approved.getApprovalReason());
        assertEquals(MesDispositionStatusEnum.DONE.name(), approved.getStatus());
        // 过账真的发生了，而且只发生一次
        assertEquals(1, qualityPostCount.get());
        assertEquals(MesBizTypeEnum.INV_TXN.name(), approved.getTargetBizType());
        assertEquals("9001", approved.getTargetBizNo());
        // 过账方向：待检转合格，这就是"把不合格品当合格用"的库存后果
        ArgumentCaptor<QualityChangeReqDTO> captor = ArgumentCaptor.forClass(QualityChangeReqDTO.class);
        verify(inventoryApi).changeQualityStatus(captor.capture());
        assertEquals(WmsQualityStatusEnum.QUALIFIED.name(), captor.getValue().getQualityStatusTo());
        assertEquals(WmsQualityStatusEnum.QC_PENDING.name(), captor.getValue().getQualityStatusFrom());
    }

    /** 验收③：驳回后单据回草稿、可改可重提，库存未动。 */
    @Test
    void testConcessionRejected_backToDraftWithoutPosting() {
        Long id = insertDisposition(MesDispositionTypeEnum.CONCESSION, "客户已确认").getId();
        dispositionDomainService.submitDisposition(actionBO(id));

        dispositionDomainService.updateConcessionApprovalStatus(id, INSTANCE_ID,
                BpmProcessInstanceStatusEnum.REJECT.getStatus(), "缺少客户书面确认");

        DispositionDO rejected = dispositionMapper.selectById(id);
        // 状态机 SUBMITTED --approvalReturn--> DRAFT：驳回后能改能重提，不留半成品状态
        assertEquals(MesDispositionStatusEnum.DRAFT.name(), rejected.getStatus());
        assertEquals(BpmProcessInstanceStatusEnum.REJECT.getStatus(), rejected.getApprovalStatus());
        assertEquals("缺少客户书面确认", rejected.getApprovalReason());
        // 审批期间与驳回都不动库存
        assertEquals(0, qualityPostCount.get());
        assertNull(rejected.getTargetBizNo());
    }

    /** 驳回回草稿不是死路：改完依据可以重新提交，走第二条审批。 */
    @Test
    void testConcessionRejected_canResubmit() {
        Long id = insertDisposition(MesDispositionTypeEnum.CONCESSION, "客户口头同意").getId();
        dispositionDomainService.submitDisposition(actionBO(id));
        dispositionDomainService.updateConcessionApprovalStatus(id, INSTANCE_ID,
                BpmProcessInstanceStatusEnum.REJECT.getStatus(), "要书面确认");

        dispositionMapper.updateById(new DispositionDO().setId(id).setRemark("客户已出具书面确认函"));
        MesDispositionActionBO resubmit = actionBO(id);
        // 换幂等键才会真正执行动作：同键会被命令层当成重复提交直接返回
        resubmit.setIdempotencyKey("UI:DSP-CONCESSION:SUBMIT_DISPOSITION:1");
        dispositionDomainService.submitDisposition(resubmit);

        DispositionDO resubmitted = dispositionMapper.selectById(id);
        assertEquals(MesDispositionStatusEnum.SUBMITTED.name(), resubmitted.getStatus());
        assertEquals(BpmProcessInstanceStatusEnum.RUNNING.getStatus(), resubmitted.getApprovalStatus());
        verify(processInstanceApi, times(2)).createProcessInstance(any(), any());
    }

    /** 撤销（发起人撤回流程）与驳回同口径：回草稿、不动库存。 */
    @Test
    void testConcessionCancelled_backToDraftWithoutPosting() {
        Long id = insertDisposition(MesDispositionTypeEnum.CONCESSION, "有补偿措施").getId();
        dispositionDomainService.submitDisposition(actionBO(id));

        dispositionDomainService.updateConcessionApprovalStatus(id, INSTANCE_ID,
                BpmProcessInstanceStatusEnum.CANCEL.getStatus(), "发起人撤回");

        assertEquals(MesDispositionStatusEnum.DRAFT.name(), dispositionMapper.selectById(id).getStatus());
        assertEquals(0, qualityPostCount.get());
    }

    /** 验收④（本阶段立项红线）：回调重复投递不产生第二笔过账。 */
    @Test
    void testConcessionCallbackDuplicated_postsOnce() {
        Long id = insertDisposition(MesDispositionTypeEnum.CONCESSION, "客户已确认").getId();
        dispositionDomainService.submitDisposition(actionBO(id));

        dispositionDomainService.updateConcessionApprovalStatus(id, INSTANCE_ID,
                BpmProcessInstanceStatusEnum.APPROVE.getStatus(), "同意让步");
        // 同一个终态再投一次（事件重放、并发投递），第二次的第三层条件 UPDATE 拿不到行
        dispositionDomainService.updateConcessionApprovalStatus(id, INSTANCE_ID,
                BpmProcessInstanceStatusEnum.APPROVE.getStatus(), "同意让步");

        assertEquals(1, qualityPostCount.get(), "重复回调不得产生第二笔质量状态过账");
        assertEquals(MesDispositionStatusEnum.DONE.name(), dispositionMapper.selectById(id).getStatus());
    }

    /** 非终态与别的流程实例都不许改业务事实：第二层校验挡的是"迟到的旧事件"。 */
    @Test
    void testConcessionCallback_runningOrOtherInstanceIgnored() {
        Long id = insertDisposition(MesDispositionTypeEnum.CONCESSION, "客户已确认").getId();
        dispositionDomainService.submitDisposition(actionBO(id));

        // 审批中事件（第一层拦）
        dispositionDomainService.updateConcessionApprovalStatus(id, INSTANCE_ID,
                BpmProcessInstanceStatusEnum.RUNNING.getStatus(), null);
        // 别的流程实例的终态（第二层拦）：这张单重新提交过，旧实例的终态是迟到的
        dispositionDomainService.updateConcessionApprovalStatus(id, "stale-process-9",
                BpmProcessInstanceStatusEnum.APPROVE.getStatus(), "迟到事件");

        DispositionDO stillRunning = dispositionMapper.selectById(id);
        assertEquals(MesDispositionStatusEnum.SUBMITTED.name(), stillRunning.getStatus());
        assertEquals(BpmProcessInstanceStatusEnum.RUNNING.getStatus(), stillRunning.getApprovalStatus());
        assertEquals(0, qualityPostCount.get());
    }

    // ==================== T3-04 报废处置 ====================

    /** 报废走的是另一个流程 key，提交同样不动库存。 */
    @Test
    void testSubmitScrap_startsApprovalWithoutTouchingInventory() {
        DispositionDO disposition = insertDisposition(MesDispositionTypeEnum.SCRAP, "批次撞伤，无法返修");

        dispositionDomainService.submitDisposition(actionBO(disposition.getId()));

        DispositionDO submitted = dispositionMapper.selectById(disposition.getId());
        assertEquals(MesDispositionStatusEnum.SUBMITTED.name(), submitted.getStatus());
        assertEquals(BpmProcessInstanceStatusEnum.RUNNING.getStatus(), submitted.getApprovalStatus());
        assertEquals(0, scrapCreateCount.get(), "审批期间不得建报废出库单");
        assertEquals(0, scrapConfirmCount.get(), "审批期间不得过账");
        ArgumentCaptor<BpmProcessInstanceCreateReqDTO> captor =
                ArgumentCaptor.forClass(BpmProcessInstanceCreateReqDTO.class);
        verify(processInstanceApi).createProcessInstance(any(), captor.capture());
        assertEquals(MesDispositionDomainServiceImpl.PROCESS_KEY_SCRAP,
                captor.getValue().getProcessDefinitionKey());
        assertEquals(String.valueOf(disposition.getId()), captor.getValue().getBusinessKey());
        // 分档依据（数量）由流程定义的条件网关消费，Service 只负责把数量塞进变量。
        // 用 compareTo 而不是字符串比对：库里 decimal(20,6) 读出来是 5.000000，值相同写法不同
        assertEquals(0, new BigDecimal(captor.getValue().getVariables().get("qty").toString())
                .compareTo(QTY));
    }

    /** 验收：审批通过后产生报废出库过账。 */
    @Test
    void testScrapApproved_postsScrapOutOnce() {
        Long id = insertDisposition(MesDispositionTypeEnum.SCRAP, "撞伤无法返修").getId();
        dispositionDomainService.submitDisposition(actionBO(id));

        dispositionDomainService.updateScrapApprovalStatus(id, INSTANCE_ID,
                BpmProcessInstanceStatusEnum.APPROVE.getStatus(), "同意报废");

        DispositionDO approved = dispositionMapper.selectById(id);
        assertEquals(MesDispositionStatusEnum.DONE.name(), approved.getStatus());
        assertEquals(BpmProcessInstanceStatusEnum.APPROVE.getStatus(), approved.getApprovalStatus());
        assertEquals(1, scrapCreateCount.get());
        assertEquals(1, scrapConfirmCount.get(), "报废出库过账必须发生且只发生一次");
        assertEquals(MesBizTypeEnum.OUT_ORDER.name(), approved.getTargetBizType());
        ArgumentCaptor<CreateOutOrderReqDTO> captor = ArgumentCaptor.forClass(CreateOutOrderReqDTO.class);
        verify(outOrderApi).createOutOrder(captor.capture());
        assertEquals(WmsOutBizTypeEnum.SCRAP_OUT.name(), captor.getValue().getBizType());
        // 质量状态过账一次都不该发生：报废是减库存，不是转合格
        assertEquals(0, qualityPostCount.get());
    }

    /** 验收：报废驳回后回草稿、库存未动。 */
    @Test
    void testScrapRejected_backToDraftWithoutPosting() {
        Long id = insertDisposition(MesDispositionTypeEnum.SCRAP, "撞伤").getId();
        dispositionDomainService.submitDisposition(actionBO(id));

        dispositionDomainService.updateScrapApprovalStatus(id, INSTANCE_ID,
                BpmProcessInstanceStatusEnum.REJECT.getStatus(), "先做返工评估");

        DispositionDO rejected = dispositionMapper.selectById(id);
        assertEquals(MesDispositionStatusEnum.DRAFT.name(), rejected.getStatus());
        assertEquals("先做返工评估", rejected.getApprovalReason());
        assertEquals(0, scrapCreateCount.get());
        assertEquals(0, scrapConfirmCount.get());
    }

    /** 验收（本阶段立项红线）：报废回调重复投递不产生第二笔过账。 */
    @Test
    void testScrapCallbackDuplicated_postsOnce() {
        Long id = insertDisposition(MesDispositionTypeEnum.SCRAP, "撞伤").getId();
        dispositionDomainService.submitDisposition(actionBO(id));

        dispositionDomainService.updateScrapApprovalStatus(id, INSTANCE_ID,
                BpmProcessInstanceStatusEnum.APPROVE.getStatus(), "同意报废");
        dispositionDomainService.updateScrapApprovalStatus(id, INSTANCE_ID,
                BpmProcessInstanceStatusEnum.APPROVE.getStatus(), "同意报废");

        assertEquals(1, scrapConfirmCount.get(), "重复回调不得产生第二笔报废出库过账");
        assertEquals(1, scrapCreateCount.get(), "重复回调不得建第二张报废出库单");
        assertEquals(MesDispositionStatusEnum.DONE.name(), dispositionMapper.selectById(id).getStatus());
    }

    /**
     * 单据不存在时回写必须静默返回，不许抛异常。
     *
     * <p>回写方法跑在 Flowable 事务里：抛出去（哪怕被监听器的 try-catch 吞掉）事务已被标记
     * rollback-only，最外层提交仍会抛 {@code UnexpectedRollbackException} 把审批终态回滚。
     * 流程还在跑而单据被删（逻辑删）是可能发生的，这一例把"契约在 Service 层也成立"钉住，
     * 不只在监听器外面糊一层 catch。
     */
    @Test
    void testConcessionCallback_missingDispositionIgnoredSilently() {
        assertDoesNotThrow(() -> dispositionDomainService.updateConcessionApprovalStatus(randomLongId(),
                INSTANCE_ID, BpmProcessInstanceStatusEnum.APPROVE.getStatus(), "同意让步"));
        assertEquals(0, qualityPostCount.get());
    }

    /**
     * 两类审批共用同一张表的四个审批列，回写方法不能串。
     *
     * <p>让步接收的回写方法处理报废单（或反过来）在运行态是"同一个 businessKey 走错监听器"，
     * 现象是单据状态被推对但联动动作做错——所以两把钥匙各自只认自己那条流程的实例编号。
     */
    @Test
    void testConcessionAndScrapCallbacksDoNotCross() {
        Long concessionId = insertDisposition(MesDispositionTypeEnum.CONCESSION, "客户已确认").getId();
        dispositionDomainService.submitDisposition(actionBO(concessionId));

        // 用报废的回写方法处理让步单：实例编号对不上（不同流程），第二层直接忽略
        dispositionDomainService.updateScrapApprovalStatus(concessionId, "other-process-1",
                BpmProcessInstanceStatusEnum.APPROVE.getStatus(), "走错门");

        assertEquals(MesDispositionStatusEnum.SUBMITTED.name(),
                dispositionMapper.selectById(concessionId).getStatus());
        assertEquals(0, qualityPostCount.get());
    }

}
