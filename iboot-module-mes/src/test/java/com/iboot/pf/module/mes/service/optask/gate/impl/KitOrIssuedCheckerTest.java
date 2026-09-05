package com.iboot.pf.module.mes.service.optask.gate.impl;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpMatRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesIssueMethodEnum;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqLineStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.kitsnapshot.MesKitRequirementExpander;
import com.iboot.pf.module.mes.service.optask.gate.MesStartGateContext;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.OP_TASK_NOT_KITTED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 门禁二齐套或已发料的测试。
 *
 * <p>判定口径是"必发料的净发料量达到本工序需求量"，用例分别锁死四件事：只拦必发料、
 * 按 {@code processed_qty} 累计而不是只认 {@code DONE} 行、三档申请都算覆盖、退料要扣回去。
 *
 * <p>需求量走的是齐套与领料共用的展开器，所以倒冲件不参与判定这件事也一并锁死——
 * 倒冲件报工时反冲，要求它开工前发料等于永远开不了工。
 */
@Import({KitOrIssuedChecker.class, MesKitRequirementExpander.class})
class KitOrIssuedCheckerTest extends BaseDbUnitTest {

    private static final Long MAT_A = 1001L;
    private static final Long MAT_BACKFLUSH = 1005L;
    private static final Long ROUTE_OP_ID = 101L;
    private static final Long ROUTE_OP_MAT_A = 401L;
    private static final Long UOM_ID = 1L;
    private static final BigDecimal PLANNED_QTY = new BigDecimal("100");

    @Resource
    private KitOrIssuedChecker checker;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private MaterialReqMapper materialReqMapper;
    @Resource
    private MaterialReqLineMapper materialReqLineMapper;

    @MockitoBean
    private RouteApi routeApi;
    @MockitoBean
    private BomApi bomApi;
    @MockitoBean
    private MaterialApi materialApi;

    @BeforeEach
    void mockMdm() {
        when(materialApi.getMaterialMap(any())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return ids.stream().distinct().collect(Collectors.toMap(id -> id, id -> material((Long) id)));
        });
    }

    @Test
    void testCheck_mustIssueNotIssuedRejectedWithMatCodeAndShortQty() {
        // 准备：工序要 100 个必发料，一点没发
        OpTaskDO task = insertTask();
        givenRouteOpMat(routeOpMat(MAT_A, "1", MesIssueMethodEnum.PICK, true));

        // 调用并断言：消息带物料编码、名称与缺口量，车间照着编码就能去仓库催料
        assertServiceException(() -> checker.check(task, new MesStartGateContext()),
                OP_TASK_NOT_KITTED, matText(MAT_A) + " 还缺 100");
    }

    @Test
    void testCheck_fullyIssuedPassed() {
        // 准备：工序级领料单已全额发料
        OpTaskDO task = insertTask();
        givenRouteOpMat(routeOpMat(MAT_A, "1", MesIssueMethodEnum.PICK, true));
        MaterialReqDO req = insertReq(MesMaterialReqTypeEnum.ISSUE, task.getWorkOrderId(), task.getId());
        insertLine(req, MAT_A, "100", "100", MesMaterialReqLineStatusEnum.DONE);

        // 调用：不抛异常即通过
        checker.check(task, new MesStartGateContext());
    }

    @Test
    void testCheck_partiallyIssuedCountsProcessedQty() {
        // 准备：申请 100 只发了 80，行状态是部分完成
        OpTaskDO task = insertTask();
        givenRouteOpMat(routeOpMat(MAT_A, "1", MesIssueMethodEnum.PICK, true));
        MaterialReqDO req = insertReq(MesMaterialReqTypeEnum.ISSUE, task.getWorkOrderId(), task.getId());
        insertLine(req, MAT_A, "100", "80", MesMaterialReqLineStatusEnum.PARTIAL);

        // 调用并断言：缺口是 20 而不是 100。只认 status=DONE 的行会把已发的 80 算成 0，
        // 缺口虚高，把本来只差一点的工彻底挡住
        assertServiceException(() -> checker.check(task, new MesStartGateContext()),
                OP_TASK_NOT_KITTED, matText(MAT_A) + " 还缺 20");
    }

    @Test
    void testCheck_workOrderLevelReqCoversOpTask() {
        // 准备：按整单领的料，申请上没有工序任务编号
        OpTaskDO task = insertTask();
        givenRouteOpMat(routeOpMat(MAT_A, "1", MesIssueMethodEnum.PICK, true));
        MaterialReqDO req = insertReq(MesMaterialReqTypeEnum.ISSUE, task.getWorkOrderId(),
                MesConstants.NONE_ID);
        insertLine(req, MAT_A, "100", "100", MesMaterialReqLineStatusEnum.DONE);

        // 调用：只看工序级申请的话，料明明在车间却被判缺料
        checker.check(task, new MesStartGateContext());
    }

    @Test
    void testCheck_prodOrderLevelReqCoversOpTask() {
        // 准备：工单级申请，既没有作业工单也没有工序任务
        OpTaskDO task = insertTask();
        givenRouteOpMat(routeOpMat(MAT_A, "1", MesIssueMethodEnum.PICK, true));
        MaterialReqDO req = insertReq(MesMaterialReqTypeEnum.ISSUE, MesConstants.NONE_ID,
                MesConstants.NONE_ID);
        insertLine(req, MAT_A, "100", "100", MesMaterialReqLineStatusEnum.DONE);

        checker.check(task, new MesStartGateContext());
    }

    @Test
    void testCheck_notMustIssueNeverBlocks() {
        // 准备：这个料不要求开工前发料，且一点没发
        OpTaskDO task = insertTask();
        givenRouteOpMat(routeOpMat(MAT_A, "1", MesIssueMethodEnum.PICK, false));

        // 调用：边做边领的料不该挡开工，它的齐套性由齐套检查负责
        checker.check(task, new MesStartGateContext());
    }

    @Test
    void testCheck_returnedQtyDeducted() {
        // 准备：领了 100 又退回 30，车间手上只剩 70
        OpTaskDO task = insertTask();
        givenRouteOpMat(routeOpMat(MAT_A, "1", MesIssueMethodEnum.PICK, true));
        MaterialReqDO issueReq = insertReq(MesMaterialReqTypeEnum.ISSUE, task.getWorkOrderId(), task.getId());
        insertLine(issueReq, MAT_A, "100", "100", MesMaterialReqLineStatusEnum.DONE);
        MaterialReqDO returnReq = insertReq(MesMaterialReqTypeEnum.RETURN, task.getWorkOrderId(),
                task.getId());
        insertLine(returnReq, MAT_A, "30", "30", MesMaterialReqLineStatusEnum.DONE);

        // 调用并断言：退回仓库的料不在工位上了，开工时用不了，必须算缺 30
        assertServiceException(() -> checker.check(task, new MesStartGateContext()),
                OP_TASK_NOT_KITTED, matText(MAT_A) + " 还缺 30");
    }

    @Test
    void testCheck_backflushMaterialNotRequired() {
        // 准备：工序只有倒冲件用料，一点没发
        OpTaskDO task = insertTask();
        givenRouteOpMat(routeOpMat(MAT_BACKFLUSH, "5", MesIssueMethodEnum.BACKFLUSH, true));

        // 调用：倒冲件报工时反冲，要求它开工前发料等于永远开不了工。
        // 需求量走齐套与领料共用的展开器，那里已经把倒冲件剔除了
        checker.check(task, new MesStartGateContext());
    }

    @Test
    void testCheck_scrapRateCountedInRequirement() {
        // 准备：单位用量 2、损耗率 5%，需求量是 100 × 2 ÷ 0.95 = 210.526316，只发了 200
        OpTaskDO task = insertTask();
        givenRouteOpMat(routeOpMatWithScrap(MAT_A, "2", "0.05", true));
        MaterialReqDO req = insertReq(MesMaterialReqTypeEnum.ISSUE, task.getWorkOrderId(), task.getId());
        insertLine(req, MAT_A, "210.526316", "200", MesMaterialReqLineStatusEnum.PARTIAL);

        // 调用并断言：门禁的需求量与齐套、领料同一个公式，不会出现"齐套判齐了而门禁按另一个数算缺料"
        assertServiceException(() -> checker.check(task, new MesStartGateContext()),
                OP_TASK_NOT_KITTED, matText(MAT_A) + " 还缺 10.526316");
    }

    private void givenRouteOpMat(RouteOpMatRespDTO... opMats) {
        when(routeApi.getRouteOpMatList(any())).thenReturn(List.of(opMats));
    }

    private RouteOpMatRespDTO routeOpMat(Long matId, String qty, MesIssueMethodEnum issueMethod,
                                        boolean mustIssue) {
        return routeOpMatOf(matId, qty, "0", issueMethod, mustIssue);
    }

    private RouteOpMatRespDTO routeOpMatWithScrap(Long matId, String qty, String scrapRate,
                                                  boolean mustIssue) {
        return routeOpMatOf(matId, qty, scrapRate, MesIssueMethodEnum.PICK, mustIssue);
    }

    private RouteOpMatRespDTO routeOpMatOf(Long matId, String qty, String scrapRate,
                                           MesIssueMethodEnum issueMethod, boolean mustIssue) {
        RouteOpMatRespDTO opMat = new RouteOpMatRespDTO();
        opMat.setId(ROUTE_OP_MAT_A);
        opMat.setRouteOpId(ROUTE_OP_ID);
        opMat.setMatId(matId);
        opMat.setQty(new BigDecimal(qty));
        opMat.setScrapRate(new BigDecimal(scrapRate));
        opMat.setIssueMethod(issueMethod.name());
        opMat.setMustIssueBeforeStart(mustIssue);
        opMat.setUomId(UOM_ID);
        return opMat;
    }

    private MaterialRespDTO material(Long matId) {
        MaterialRespDTO material = new MaterialRespDTO();
        material.setId(matId);
        material.setCode("MAT-" + matId);
        material.setName("物料" + matId);
        material.setBaseUomId(UOM_ID);
        return material;
    }

    /** 错误消息里的物料描述，与 checker 的拼法保持一致。 */
    private String matText(Long matId) {
        return "MAT-" + matId + " 物料" + matId;
    }

    /** 一道工序任务，连带它所属的作业工单——门禁要顺着作业工单找到生产工单才能查整单级申请。 */
    private OpTaskDO insertTask() {
        WorkOrderDO workOrder = WorkOrderDO.builder()
                .workOrderNo("WO-0001")
                .prodOrderId(8001L)
                .prodOrderNo("PO-0001")
                .plantId(1L)
                .workshopId(2L)
                .wcId(201L)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .plannedQty(PLANNED_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .kitStatus(MesKitStatusEnum.UNCHECKED.name())
                .status(MesWorkOrderStatusEnum.PENDING.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        workOrderMapper.insert(workOrder);
        OpTaskDO task = OpTaskDO.builder()
                .taskNo("OT-0001")
                .workOrderId(workOrder.getId())
                .workOrderNo(workOrder.getWorkOrderNo())
                .routeOpId(ROUTE_OP_ID)
                .opId(201L)
                .opCode("OP010")
                .opName("下料")
                .seq(10)
                .prevTaskId(MesConstants.NONE_ID)
                .nextTaskId(MesConstants.NONE_ID)
                .plannedQty(PLANNED_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .resId(MesConstants.NONE_ID)
                .keyFlag(Boolean.FALSE)
                .status(MesOpTaskStatusEnum.READY.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        opTaskMapper.insert(task);
        return task;
    }

    private MaterialReqDO insertReq(MesMaterialReqTypeEnum reqType, Long workOrderId, Long opTaskId) {
        MaterialReqDO req = MaterialReqDO.builder()
                .reqNo("MR-" + System.nanoTime())
                .prodOrderId(8001L)
                .prodOrderNo("PO-0001")
                .workOrderId(workOrderId)
                .opTaskId(opTaskId)
                .reqType(reqType.name())
                .invOrgId(1L)
                .whId(1L)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .status(MesMaterialReqStatusEnum.DONE.name())
                .idempotencyKey("UI:PO-0001:CREATE_REQ:" + System.nanoTime())
                .build();
        materialReqMapper.insert(req);
        return req;
    }

    private void insertLine(MaterialReqDO req, Long matId, String qty, String processedQty,
                            MesMaterialReqLineStatusEnum status) {
        materialReqLineMapper.insert(MaterialReqLineDO.builder()
                .reqId(req.getId())
                .lineNo(MesConstants.FIRST_LINE_NO)
                .routeOpMatId(ROUTE_OP_MAT_A)
                .matId(matId)
                .matCode("MAT-" + matId)
                .matName("物料" + matId)
                .qty(new BigDecimal(qty))
                .processedQty(new BigDecimal(processedQty))
                .uomId(UOM_ID)
                .uomCode("PCS")
                .batchId(MesConstants.NONE_ID)
                .reservationId(MesConstants.NONE_ID)
                .mustIssueBeforeStart(Boolean.TRUE)
                .status(status.name())
                .build());
    }

}



