package com.iboot.pf.module.mes.service.kitsnapshot;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.bom.dto.BomLineRespDTO;
import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;
import com.iboot.pf.module.mdm.api.matalt.MatAltApi;
import com.iboot.pf.module.mdm.api.matalt.dto.MatAltRespDTO;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.mdm.api.material.dto.MaterialRespDTO;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpMatRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.kitsnapshot.KitSnapshotDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.command.MesCommandRecordMapper;
import com.iboot.pf.module.mes.dal.mysql.kitsnapshot.KitSnapshotMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesIssueMethodEnum;
import com.iboot.pf.module.mes.enums.MesKitCheckScopeEnum;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqLineStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandServiceImpl;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitCheckBO;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitCheckResultBO;
import com.iboot.pf.module.mes.service.wiptxn.MesWipTxnRecorder;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.BOM_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.KIT_CHECK_BOM_DEPTH_EXCEEDED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.KIT_CHECK_PHANTOM_BOM_MISSING;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.KIT_CHECK_SCOPE_TARGET_REQUIRED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_ORDER_NOT_EXISTS;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.WORK_ORDER_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 齐套检查测试。
 *
 * <p>库存侧用一个可编程的"账面"替身：{@link #stock} 是物料到可用量的映射，
 * {@code InventoryApi} 的 mock 按查询条件从它取数。这样断言的是 MES 侧的判定与编排是否正确，
 * 真实的 WMS 账实一致由阶段 6 的跨模块集成测试覆盖，单模块单测里没有 wms 表可用。
 *
 * <p>四个验收硬指标各有专属用例：损耗率 210.526316、虚拟件、倒冲件、替代料。
 */
@Import({MesKitCheckServiceImpl.class, MesKitRequirementExpander.class,
        MesCommandServiceImpl.class, MesWipTxnRecorder.class})
class MesKitCheckServiceImplTest extends BaseDbUnitTest {

    private static final String SOURCE_MODULE = "UI";
    private static final String ORDER_NO = "PO-0001";
    private static final BigDecimal ORDER_QTY = new BigDecimal("100");

    /** 工单下达时锁定的 BOM 编号。 */
    private static final Long BOM_ID = 11L;
    /** 虚拟件自己的生效 BOM 编号。 */
    private static final Long PHANTOM_BOM_ID = 12L;

    private static final Long MAT_A = 1001L;
    private static final Long MAT_B = 1002L;
    private static final Long MAT_CHILD = 1003L;
    private static final Long MAT_PHANTOM = 1004L;
    private static final Long MAT_BACKFLUSH = 1005L;
    private static final Long MAT_ALT_FIRST = 1006L;
    private static final Long MAT_ALT_SECOND = 1007L;

    private static final Long ROUTE_OP_CUT = 101L;
    private static final Long ROUTE_OP_MACHINE = 102L;
    private static final Long UOM_ID = 1L;

    /** 损耗率场景的期望需求量：100 × 2 ÷ (1 − 0.05)，与 04 文档的数字一致。 */
    private static final BigDecimal SCRAP_REQ_QTY = new BigDecimal("210.526316");

    @Resource
    private MesKitCheckService kitCheckService;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private WorkOrderMapper workOrderMapper;
    @Resource
    private OpTaskMapper opTaskMapper;
    @Resource
    private KitSnapshotMapper kitSnapshotMapper;
    @Resource
    private MaterialReqMapper materialReqMapper;
    @Resource
    private MaterialReqLineMapper materialReqLineMapper;
    @Resource
    private WipTxnMapper wipTxnMapper;
    @Resource
    private MesCommandRecordMapper commandRecordMapper;

    @MockitoBean
    private BomApi bomApi;
    @MockitoBean
    private RouteApi routeApi;
    @MockitoBean
    private MaterialApi materialApi;
    @MockitoBean
    private InventoryApi inventoryApi;
    @MockitoBean
    private MatAltApi matAltApi;
    @MockitoBean
    private MesCodeService codeService;

    /** 物料到可用量的账面，用例按需塞值，未塞的物料可用量为 0。 */
    private final Map<Long, BigDecimal> stock = new HashMap<>();

    @BeforeEach
    void mockExternalDependencies() {
        stock.clear();
        Map<String, AtomicInteger> counters = new HashMap<>();
        when(codeService.generate(anyString(), any())).thenAnswer(invocation -> {
            String ruleCode = invocation.getArgument(0);
            return invocation.getArgument(1) != null ? invocation.getArgument(1)
                    : ruleCode + "-" + counters.computeIfAbsent(ruleCode, key -> new AtomicInteger())
                    .incrementAndGet();
        });
        // 物料快照：编码与名称按编号派生，用于断言快照确实落了主数据的可读字段
        when(materialApi.getMaterialMap(any())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return ids.stream().distinct().collect(Collectors.toMap(id -> id, id -> material((Long) id)));
        });
        // 可用量：按查询条件里的物料集合从账面取，缺的物料不产生键（与真实门面口径一致）
        when(inventoryApi.getAvailableQtyMap(any())).thenAnswer(invocation -> {
            InventoryQueryReqDTO query = invocation.getArgument(0);
            Map<Long, BigDecimal> result = new LinkedHashMap<>();
            query.getMatIds().forEach(matId -> {
                BigDecimal qty = stock.get(matId);
                if (qty != null) {
                    result.put(matId, qty);
                }
            });
            return result;
        });
        when(matAltApi.getAltMap(any())).thenReturn(Map.of());
        when(routeApi.getRouteOpMatList(any())).thenReturn(List.of());
        when(bomApi.getBomLineList(any())).thenReturn(List.of());
    }

    @Test
    void testCheckKit_prodOrderScopeWithScrapRate() {
        // 准备：整单 BOM 一行，单位用量 2、损耗率 0.05，工单 100 件；库存充足
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_A, "2", "0.05", MesIssueMethodEnum.PICK));
        stock.put(MAT_A, new BigDecimal("300"));

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(order));

        // 断言：验收硬指标——需求量 100 × 2 ÷ (1 − 0.05) = 210.526316，与 04 文档的数字一致
        assertEquals(1, snapshots().size());
        KitSnapshotDO line = snapshotOf(MAT_A);
        assertEquals(0, SCRAP_REQ_QTY.compareTo(line.getReqQty()));
        assertEquals(0, new BigDecimal("300").compareTo(line.getAvailQty()));
        assertEquals(0, BigDecimal.ZERO.compareTo(line.getShortQty()));
        assertEquals(0, BigDecimal.ZERO.compareTo(line.getReservedQty()));
        assertEquals(0, BigDecimal.ZERO.compareTo(line.getIssuedQty()));
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), line.getResult());
        assertEquals(MesConstants.NONE_ID, line.getAltMatId());
        // 三档定位：整单检查时作业工单与工序任务维度落 0 不落 null
        assertEquals(MesKitCheckScopeEnum.PROD_ORDER.name(), line.getCheckScope());
        assertEquals(order.getId(), line.getProdOrderId());
        assertEquals(MesConstants.NONE_ID, line.getWorkOrderId());
        assertEquals(MesConstants.NONE_ID, line.getOpTaskId());
        assertEquals(MesConstants.FIRST_LINE_NO, line.getLineNo());
        // 物料编码与名称是快照，来自主数据门面而不是回查
        assertEquals("MAT-" + MAT_A, line.getMatCode());
        assertEquals("物料" + MAT_A, line.getMatName());
        assertNotNull(line.getCheckTime());
        // 检查编号走编码规则，不是拼串
        assertEquals("MES_KIT_CHECK_NO-1", line.getCheckNo());
        // 返回值：整体结论与落库行一致
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), result.getResult());
        assertEquals(line.getCheckNo(), result.getCheckNo());
        assertEquals(1, result.getLines().size());
        // 齐套通过：工单齐套状态回写，状态从已下达推到就绪，乐观锁版本自增
        ProdOrderDO checked = reloadOrder(order);
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), checked.getKitStatus());
        assertEquals(MesProdOrderStatusEnum.READY.name(), checked.getStatus());
        assertEquals(MesConstants.INIT_VERSION + 1, checked.getVersion());
    }

    @Test
    void testCheckKit_readyAdvanceWritesWipTxn() {
        // 准备：齐套必然通过的最简 BOM
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK));
        stock.put(MAT_A, new BigDecimal("100"));

        // 调用
        kitCheckService.checkKit(prodOrderBO(order));

        // 断言：转就绪留一条在制流水，前后状态是状态机执行轨迹的唯一凭据
        assertEquals(1L, wipTxnMapper.selectCount());
        WipTxnDO txn = wipTxnMapper.selectList().get(0);
        assertEquals(order.getId(), txn.getProdOrderId());
        assertEquals(MesWipTxnTypeEnum.START.name(), txn.getTxnType());
        assertEquals(MesProdOrderStatusEnum.RELEASED.name(), txn.getFromStatus());
        assertEquals(MesProdOrderStatusEnum.READY.name(), txn.getToStatus());
        assertEquals(MesBizTypeEnum.PROD_ORDER.name(), txn.getBizType());
        assertEquals(ORDER_NO, txn.getBizNo());
        assertEquals(0, BigDecimal.ZERO.compareTo(txn.getQty()));
        // 工单级流水没有作业工单与工序任务维度
        assertEquals(MesConstants.NONE_ID, txn.getWorkOrderId());
        assertEquals(MesConstants.NONE_ID, txn.getOpTaskId());
        // 命令记录留痕，幂等键按 {来源模块}:{工单号}:KIT_CHECK:{检查编号} 派生，流水与命令同键
        String expectedKey = SOURCE_MODULE + ":" + ORDER_NO + ":"
                + MesCommandTypeEnum.KIT_CHECK.name() + ":MES_KIT_CHECK_NO-1";
        assertEquals(expectedKey, txn.getIdempotencyKey());
        assertNotNull(commandRecordMapper.selectByIdempotencyKey(expectedKey));
    }

    @Test
    void testCheckKit_phantomNotCountedChildrenCounted() {
        // 准备：整单 BOM 只有一行虚拟件，虚拟件自己的 BOM 下挂一个真实子件，用量 3
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_PHANTOM, "1", "0", MesIssueMethodEnum.PHANTOM));
        givenPhantomBom(bomLine(1, MAT_CHILD, "3", "0", MesIssueMethodEnum.PICK));
        // 虚拟件物理上不存在，库存永远是 0；子件库存充足
        stock.put(MAT_CHILD, new BigDecimal("500"));

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(order));

        // 断言：虚拟件不产生需求行，子件产生需求 100 × 1 × 3 = 300
        assertEquals(1, snapshots().size());
        assertTrue(!hasSnapshotOf(MAT_PHANTOM), "虚拟件不该出现在快照里，它库存永远是 0，算它齐套永远不通过");
        assertEquals(0, new BigDecimal("300").compareTo(snapshotOf(MAT_CHILD).getReqQty()));
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), result.getResult());
    }

    @Test
    void testCheckKit_backflushExcluded() {
        // 准备：一行常规料（库存刚好够）、一行倒冲件（库存为 0）
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK),
                bomLine(2, MAT_BACKFLUSH, "5", "0", MesIssueMethodEnum.BACKFLUSH));
        stock.put(MAT_A, new BigDecimal("100"));

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(order));

        // 断言：倒冲件完全不出现在快照里。若被计入，它库存 0 会把整体结论压成 PARTIAL_KIT，
        // 辅料没领就开不了工——这正是 04 文档点名的错法
        assertEquals(1, snapshots().size());
        assertTrue(!hasSnapshotOf(MAT_BACKFLUSH), "倒冲件报工时才反冲，不该计入齐套");
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), result.getResult());
        assertEquals(MesProdOrderStatusEnum.READY.name(), reloadOrder(order).getStatus());
    }

    @Test
    void testCheckKit_backflushOnlyBomTreatedAsFullKit() {
        // 准备：整单 BOM 全是倒冲件，展开后没有任何需求行
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_BACKFLUSH, "5", "0", MesIssueMethodEnum.BACKFLUSH));

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(order));

        // 断言：没有需求就没有缺料，结论为齐套，工单照样转就绪
        assertEquals(0, snapshots().size());
        assertTrue(result.getLines().isEmpty());
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), result.getResult());
        assertEquals(MesProdOrderStatusEnum.READY.name(), reloadOrder(order).getStatus());
    }

    @Test
    void testCheckKit_alternativeCoversShortage() {
        // 准备：主料一颗不剩（需求 210.526316），替代料 300 充足
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_A, "2", "0.05", MesIssueMethodEnum.PICK));
        stock.put(MAT_A, BigDecimal.ZERO);
        stock.put(MAT_ALT_FIRST, new BigDecimal("300"));
        when(matAltApi.getAltMap(any())).thenReturn(Map.of(MAT_A, List.of(alt(MAT_A, MAT_ALT_FIRST, 1))));

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(order));

        // 断言：验收硬指标——行结果改判齐套且记下采用的替代料
        KitSnapshotDO line = snapshotOf(MAT_A);
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), line.getResult());
        assertEquals(MAT_ALT_FIRST, line.getAltMatId());
        assertNotEquals(MesConstants.NONE_ID, line.getAltMatId());
        assertEquals(0, BigDecimal.ZERO.compareTo(line.getShortQty()));
        // avail_qty 仍记主料可用量：换成替代料的量，事后就看不出主料到底缺了多少
        assertEquals(0, BigDecimal.ZERO.compareTo(line.getAvailQty()));
        assertEquals(0, SCRAP_REQ_QTY.compareTo(line.getReqQty()));
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), result.getResult());
        assertEquals(MesProdOrderStatusEnum.READY.name(), reloadOrder(order).getStatus());
    }

    @Test
    void testCheckKit_alternativePickedByPriorityNotByStock() {
        // 准备：两个替代料，优先级 1 的库存刚够、优先级 2 的库存更多
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_A, "2", "0.05", MesIssueMethodEnum.PICK));
        stock.put(MAT_A, BigDecimal.ZERO);
        stock.put(MAT_ALT_FIRST, new BigDecimal("220"));
        stock.put(MAT_ALT_SECOND, new BigDecimal("999"));
        when(matAltApi.getAltMap(any())).thenReturn(Map.of(MAT_A,
                List.of(alt(MAT_A, MAT_ALT_FIRST, 1), alt(MAT_A, MAT_ALT_SECOND, 2))));

        // 调用
        kitCheckService.checkKit(prodOrderBO(order));

        // 断言：按 priority 升序取第一个够用的，不是库存最多的那个
        assertEquals(MAT_ALT_FIRST, snapshotOf(MAT_A).getAltMatId());
    }

    @Test
    void testCheckKit_alternativeNotEnoughStaysShortage() {
        // 准备：主料 50，替代料 100，需求 210.526316，缺口 160.526316 谁都补不上
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_A, "2", "0.05", MesIssueMethodEnum.PICK));
        stock.put(MAT_A, new BigDecimal("50"));
        stock.put(MAT_ALT_FIRST, new BigDecimal("100"));
        when(matAltApi.getAltMap(any())).thenReturn(Map.of(MAT_A, List.of(alt(MAT_A, MAT_ALT_FIRST, 1))));

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(order));

        // 断言：结论短缺，缺口按主料算，替代料编号不落
        KitSnapshotDO line = snapshotOf(MAT_A);
        assertEquals(MesKitStatusEnum.SHORTAGE.name(), line.getResult());
        assertEquals(MesConstants.NONE_ID, line.getAltMatId());
        assertEquals(0, new BigDecimal("160.526316").compareTo(line.getShortQty()));
        assertEquals(MesKitStatusEnum.SHORTAGE.name(), result.getResult());
        // 缺料不推进：工单停在已下达，齐套状态回写为短缺
        ProdOrderDO checked = reloadOrder(order);
        assertEquals(MesKitStatusEnum.SHORTAGE.name(), checked.getKitStatus());
        assertEquals(MesProdOrderStatusEnum.RELEASED.name(), checked.getStatus());
        assertEquals(0L, wipTxnMapper.selectCount());
    }

    @Test
    void testCheckKit_partialKitWhenSomeShort() {
        // 准备：两行料，一行够一行不够
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK),
                bomLine(2, MAT_B, "1", "0", MesIssueMethodEnum.PICK));
        stock.put(MAT_A, new BigDecimal("100"));
        stock.put(MAT_B, new BigDecimal("40"));

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(order));

        // 断言：有齐有缺汇总成部分齐套
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), snapshotOf(MAT_A).getResult());
        assertEquals(MesKitStatusEnum.SHORTAGE.name(), snapshotOf(MAT_B).getResult());
        assertEquals(0, new BigDecimal("60").compareTo(snapshotOf(MAT_B).getShortQty()));
        assertEquals(MesKitStatusEnum.PARTIAL_KIT.name(), result.getResult());
        assertEquals(MesKitStatusEnum.PARTIAL_KIT.name(), reloadOrder(order).getKitStatus());
        assertEquals(MesProdOrderStatusEnum.RELEASED.name(), reloadOrder(order).getStatus());
    }

    @Test
    void testCheckKit_workOrderScope() {
        // 准备：一张作业工单下两道工序，各自一种用料
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        WorkOrderDO workOrder = insertWorkOrder(order);
        insertOpTask(workOrder, ROUTE_OP_CUT, 10);
        insertOpTask(workOrder, ROUTE_OP_MACHINE, 20);
        givenRouteOpMats(
                routeOpMat(401L, ROUTE_OP_CUT, MAT_A, "1", "0", MesIssueMethodEnum.PICK, true),
                routeOpMat(402L, ROUTE_OP_MACHINE, MAT_B, "2", "0", MesIssueMethodEnum.PICK, false));
        stock.put(MAT_A, new BigDecimal("100"));
        stock.put(MAT_B, new BigDecimal("200"));

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(workOrderBO(order, workOrder));

        // 断言：需求来自本段覆盖工序的用料，父项数量取作业工单计划数量
        assertEquals(2, snapshots().size());
        assertEquals(0, new BigDecimal("100").compareTo(snapshotOf(MAT_A).getReqQty()));
        assertEquals(0, new BigDecimal("200").compareTo(snapshotOf(MAT_B).getReqQty()));
        assertEquals(MesKitCheckScopeEnum.WORK_ORDER.name(), snapshotOf(MAT_A).getCheckScope());
        assertEquals(workOrder.getId(), snapshotOf(MAT_A).getWorkOrderId());
        assertEquals(MesConstants.NONE_ID, snapshotOf(MAT_A).getOpTaskId());
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), result.getResult());
        // 齐套状态回写到被检查的那一层：作业工单变了，生产工单的齐套状态不动
        assertEquals(MesKitStatusEnum.FULL_KIT.name(),
                workOrderMapper.selectById(workOrder.getId()).getKitStatus());
        assertEquals(MesKitStatusEnum.UNCHECKED.name(), reloadOrder(order).getKitStatus());
        // 生产工单仍要转就绪：这一段的料齐了，车间可以开工
        assertEquals(MesProdOrderStatusEnum.READY.name(), reloadOrder(order).getStatus());
    }

    @Test
    void testCheckKit_opTaskScope() {
        // 准备：单道工序，只查它自己的用料
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        WorkOrderDO workOrder = insertWorkOrder(order);
        OpTaskDO opTask = insertOpTask(workOrder, ROUTE_OP_CUT, 10);
        givenRouteOpMats(routeOpMat(401L, ROUTE_OP_CUT, MAT_A, "1", "0", MesIssueMethodEnum.PICK, true));
        stock.put(MAT_A, new BigDecimal("100"));

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(opTaskBO(order, opTask));

        // 断言：一行需求，工序任务维度落在快照上
        assertEquals(1, snapshots().size());
        KitSnapshotDO line = snapshotOf(MAT_A);
        assertEquals(MesKitCheckScopeEnum.OP_TASK.name(), line.getCheckScope());
        assertEquals(opTask.getId(), line.getOpTaskId());
        assertEquals(MesConstants.NONE_ID, line.getWorkOrderId());
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), result.getResult());
        // 工序级结论不回写作业工单：一道工序齐套不代表整段齐套
        assertEquals(MesKitStatusEnum.UNCHECKED.name(),
                workOrderMapper.selectById(workOrder.getId()).getKitStatus());
        assertEquals(MesKitStatusEnum.UNCHECKED.name(), reloadOrder(order).getKitStatus());
    }

    @Test
    void testCheckKit_sameMaterialMergedAcrossOps() {
        // 准备：两道工序都用同一个物料，各要 100，库存只有 150
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        WorkOrderDO workOrder = insertWorkOrder(order);
        insertOpTask(workOrder, ROUTE_OP_CUT, 10);
        insertOpTask(workOrder, ROUTE_OP_MACHINE, 20);
        givenRouteOpMats(
                routeOpMat(401L, ROUTE_OP_CUT, MAT_A, "1", "0", MesIssueMethodEnum.PICK, false),
                routeOpMat(402L, ROUTE_OP_MACHINE, MAT_A, "1", "0", MesIssueMethodEnum.PICK, true));
        stock.put(MAT_A, new BigDecimal("150"));

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(workOrderBO(order, workOrder));

        // 断言：合并成一行、需求 200、短缺 50。不合并的话两行各要 100、库存 150，
        // 两行都会判齐套，实际开不了工
        assertEquals(1, snapshots().size());
        KitSnapshotDO line = snapshotOf(MAT_A);
        assertEquals(0, new BigDecimal("200").compareTo(line.getReqQty()));
        assertEquals(0, new BigDecimal("50").compareTo(line.getShortQty()));
        assertEquals(MesKitStatusEnum.SHORTAGE.name(), result.getResult());
    }

    @Test
    void testCheckKit_reservedAndIssuedOffsetShortage() {
        // 准备：需求 100，仓库可用量只剩 20；本工单已有一张部分完成的领料单：
        // 申请 100、已出库 60（issued），剩下 40 还被预留占着（reserved）
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK));
        stock.put(MAT_A, new BigDecimal("20"));
        MaterialReqDO issueReq = insertMaterialReq(order, MesMaterialReqTypeEnum.ISSUE,
                MesMaterialReqStatusEnum.PARTIAL);
        insertMaterialReqLine(issueReq, MAT_A, "100", "60", MesMaterialReqLineStatusEnum.PARTIAL);
        // 一张草稿单不该被算进来：它还没有任何库存后果
        MaterialReqDO draftReq = insertMaterialReq(order, MesMaterialReqTypeEnum.ISSUE,
                MesMaterialReqStatusEnum.DRAFT);
        insertMaterialReqLine(draftReq, MAT_A, "999", "0", MesMaterialReqLineStatusEnum.DRAFT);

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(order));

        // 断言：20 可用 + 40 预留 + 60 已领 = 120 >= 100，判齐套。
        // 只看可用量 20 会判短缺——工单刚备好料就被自己的预留压回缺料状态，READY 再也回不去
        KitSnapshotDO line = snapshotOf(MAT_A);
        assertEquals(0, new BigDecimal("40").compareTo(line.getReservedQty()));
        assertEquals(0, new BigDecimal("60").compareTo(line.getIssuedQty()));
        assertEquals(0, BigDecimal.ZERO.compareTo(line.getShortQty()));
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), result.getResult());
    }

    @Test
    void testCheckKit_returnReqNotOffset() {
        // 准备：需求 100、可用量 0，本工单有一张退料单（申请 100、已处理 100）
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK));
        stock.put(MAT_A, BigDecimal.ZERO);
        MaterialReqDO returnReq = insertMaterialReq(order, MesMaterialReqTypeEnum.RETURN,
                MesMaterialReqStatusEnum.DONE);
        insertMaterialReqLine(returnReq, MAT_A, "100", "100", MesMaterialReqLineStatusEnum.DONE);

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(order));

        // 断言：退料是把料还回仓库，还回去的量已经体现在可用量里，再当成"本工单已备"算一遍就是重复计入
        KitSnapshotDO line = snapshotOf(MAT_A);
        assertEquals(0, BigDecimal.ZERO.compareTo(line.getIssuedQty()));
        assertEquals(0, BigDecimal.ZERO.compareTo(line.getReservedQty()));
        assertEquals(0, new BigDecimal("100").compareTo(line.getShortQty()));
        assertEquals(MesKitStatusEnum.SHORTAGE.name(), result.getResult());
    }

    @Test
    void testCheckKit_idempotentSameKeyReturnsSameSnapshot() {
        // 准备
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK));
        stock.put(MAT_A, new BigDecimal("100"));
        MesKitCheckBO bo = prodOrderBO(order);
        bo.setIdempotencyKey("UI:PO-0001:KIT_CHECK:FIXED");

        // 调用：同一个键提交两次
        MesKitCheckResultBO first = kitCheckService.checkKit(bo);
        MesKitCheckResultBO second = kitCheckService.checkKit(bo);

        // 断言：不产生第二份快照，重复提交拿到与首次完全相同的那一份
        assertEquals(1, snapshots().size());
        assertEquals(first.getCheckNo(), second.getCheckNo());
        assertEquals(first.getResult(), second.getResult());
        assertEquals(1, second.getLines().size());
        assertEquals(1L, commandRecordMapper.selectCount());
        // 状态只推进一次，流水只留一条
        assertEquals(MesConstants.INIT_VERSION + 1, reloadOrder(order).getVersion());
        assertEquals(1L, wipTxnMapper.selectCount());
    }

    @Test
    void testCheckKit_repeatableProducesNewSnapshot() {
        // 准备
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK));
        stock.put(MAT_A, new BigDecimal("100"));

        // 调用：不传幂等键，连续查两次
        MesKitCheckResultBO first = kitCheckService.checkKit(prodOrderBO(order));
        MesKitCheckResultBO second = kitCheckService.checkKit(prodOrderBO(order));

        // 断言：齐套是可反复执行的查询型动作，两次各产生一份快照，检查编号不同
        assertNotEquals(first.getCheckNo(), second.getCheckNo());
        assertEquals(2, snapshots().size());
        assertEquals(1, second.getLines().size());
        assertEquals(2L, commandRecordMapper.selectCount());
        // 第二次工单已经是 READY，不再推进，因此只有一条转就绪流水
        assertEquals(MesProdOrderStatusEnum.READY.name(), reloadOrder(order).getStatus());
        assertEquals(MesConstants.INIT_VERSION + 1, reloadOrder(order).getVersion());
        assertEquals(1L, wipTxnMapper.selectCount());
    }

    @Test
    void testCheckKit_runningOrderNotAdvanced() {
        // 准备：已开工的工单再查一次齐套
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RUNNING, BOM_ID);
        givenBom(bomLine(1, MAT_A, "1", "0", MesIssueMethodEnum.PICK));
        stock.put(MAT_A, new BigDecimal("100"));

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(order));

        // 断言：不报错也不推进。齐套是查询型动作，为"工单已经开工"这件事让整次检查失败没有道理；
        // 状态机里 RUNNING 也没有 ready 出边，硬推会抛错
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), result.getResult());
        ProdOrderDO checked = reloadOrder(order);
        assertEquals(MesProdOrderStatusEnum.RUNNING.name(), checked.getStatus());
        assertEquals(MesConstants.INIT_VERSION, checked.getVersion());
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), checked.getKitStatus());
        assertEquals(0L, wipTxnMapper.selectCount());
    }

    @Test
    void testCheckKit_phantomCycleRejected() {
        // 准备：虚拟件的 BOM 里又是它自己，形成环
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_PHANTOM, "1", "0", MesIssueMethodEnum.PHANTOM));
        givenPhantomBom(bomLine(1, MAT_PHANTOM, "1", "0", MesIssueMethodEnum.PHANTOM));

        // 调用并断言：递归到第 10 层就地报错，而不是栈溢出
        assertServiceException(() -> kitCheckService.checkKit(prodOrderBO(order)),
                KIT_CHECK_BOM_DEPTH_EXCEEDED, 10);
        assertEquals(0, snapshots().size());
        assertEquals(MesKitStatusEnum.UNCHECKED.name(), reloadOrder(order).getKitStatus());
    }

    @Test
    void testCheckKit_phantomWithoutBomRejected() {
        // 准备：虚拟件没有生效 BOM，展开不出子件
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        givenBom(bomLine(1, MAT_PHANTOM, "1", "0", MesIssueMethodEnum.PHANTOM));

        // 调用并断言：静默跳过会让这部分需求凭空消失、齐套永远判通过，因此直接报错
        assertServiceException(() -> kitCheckService.checkKit(prodOrderBO(order)),
                KIT_CHECK_PHANTOM_BOM_MISSING, MAT_PHANTOM);
        assertEquals(0, snapshots().size());
    }

    @Test
    void testCheckKit_bomNotLockedRejected() {
        // 准备：还没下达的工单，四个版本快照都是 0
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.DRAFT, MesConstants.NONE_ID);

        // 调用并断言：空 BOM 展开不出需求行，会静默判成齐套，所以拒绝检查
        assertServiceException(() -> kitCheckService.checkKit(prodOrderBO(order)), BOM_NOT_EXISTS);
    }

    @Test
    void testCheckKit_scopeInvalidRejected() {
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        MesKitCheckBO bo = prodOrderBO(order);
        bo.setCheckScope("WHOLE_PLANT");

        assertServiceException(() -> kitCheckService.checkKit(bo), KIT_CHECK_SCOPE_TARGET_REQUIRED, "WHOLE_PLANT");
    }

    @Test
    void testCheckKit_workOrderScopeWithoutWorkOrderRejected() {
        ProdOrderDO order = insertOrder(MesProdOrderStatusEnum.RELEASED, BOM_ID);
        MesKitCheckBO bo = prodOrderBO(order);
        bo.setCheckScope(MesKitCheckScopeEnum.WORK_ORDER.name());

        assertServiceException(() -> kitCheckService.checkKit(bo), WORK_ORDER_NOT_EXISTS);
    }

    @Test
    void testCheckKit_prodOrderNotExists() {
        MesKitCheckBO bo = new MesKitCheckBO();
        bo.setProdOrderId(randomLongId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setCheckScope(MesKitCheckScopeEnum.PROD_ORDER.name());

        assertServiceException(() -> kitCheckService.checkKit(bo), PROD_ORDER_NOT_EXISTS);
    }

    private MaterialRespDTO material(Long matId) {
        MaterialRespDTO material = new MaterialRespDTO();
        material.setId(matId);
        material.setCode("MAT-" + matId);
        material.setName("物料" + matId);
        material.setSpec("规格" + matId);
        material.setBaseUomId(UOM_ID);
        return material;
    }

    /** 挂主件 BOM 的明细行。 */
    private void givenBom(BomLineRespDTO... lines) {
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(lines));
    }

    /** 挂虚拟件自己的生效 BOM 与明细，用于断言向下展开。 */
    private void givenPhantomBom(BomLineRespDTO... lines) {
        BomRespDTO phantomBom = new BomRespDTO();
        phantomBom.setId(PHANTOM_BOM_ID);
        phantomBom.setVersion("P1.0");
        when(bomApi.getEffectiveBom(eq(MAT_PHANTOM), any())).thenReturn(phantomBom);
        when(bomApi.getBomLineList(PHANTOM_BOM_ID)).thenReturn(List.of(lines));
    }

    private void givenRouteOpMats(RouteOpMatRespDTO... mats) {
        when(routeApi.getRouteOpMatList(any())).thenReturn(List.of(mats));
    }

    private BomLineRespDTO bomLine(int lineNo, Long matId, String qty, String scrapRate,
                                   MesIssueMethodEnum issueMethod) {
        BomLineRespDTO line = new BomLineRespDTO();
        line.setId(200L + lineNo);
        line.setBomId(BOM_ID);
        line.setLineNo(lineNo);
        line.setMatId(matId);
        line.setQty(new BigDecimal(qty));
        line.setScrapRate(new BigDecimal(scrapRate));
        line.setIssueMethod(issueMethod.name());
        line.setUomId(UOM_ID);
        return line;
    }

    private RouteOpMatRespDTO routeOpMat(Long id, Long routeOpId, Long matId, String qty, String scrapRate,
                                         MesIssueMethodEnum issueMethod, boolean mustIssue) {
        RouteOpMatRespDTO opMat = new RouteOpMatRespDTO();
        opMat.setId(id);
        opMat.setRouteOpId(routeOpId);
        opMat.setMatId(matId);
        opMat.setQty(new BigDecimal(qty));
        opMat.setScrapRate(new BigDecimal(scrapRate));
        opMat.setIssueMethod(issueMethod.name());
        opMat.setMustIssueBeforeStart(mustIssue);
        opMat.setUomId(UOM_ID);
        return opMat;
    }

    private MatAltRespDTO alt(Long matId, Long altMatId, int priority) {
        MatAltRespDTO matAlt = new MatAltRespDTO();
        matAlt.setId(300L + priority);
        matAlt.setMatId(matId);
        matAlt.setAltMatId(altMatId);
        matAlt.setPriority(priority);
        return matAlt;
    }

    /**
     * 落一张已下达、已锁 BOM 版本的生产工单。
     *
     * <p>{@code version} 显式给初始值：带 {@code WHERE version = ?} 条件的更新碰上 NULL 版本会一行都更不到。
     */
    private ProdOrderDO insertOrder(MesProdOrderStatusEnum status, Long bomId) {
        ProdOrderDO order = ProdOrderDO.builder()
                .orderNo(ORDER_NO)
                .demandId(MesConstants.NONE_ID)
                .plantId(1L)
                .workshopId(2L)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .matId(1L)
                .matCode("MAT-001")
                .qty(ORDER_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .uomId(UOM_ID)
                .bomId(bomId)
                .bomVersion("B1.0")
                .routeId(21L)
                .routeVersion("R1.0")
                .priority(0)
                .kitStatus(MesKitStatusEnum.UNCHECKED.name())
                .status(status.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        prodOrderMapper.insert(order);
        return order;
    }

    private WorkOrderDO insertWorkOrder(ProdOrderDO order) {
        WorkOrderDO workOrder = WorkOrderDO.builder()
                .workOrderNo("WO-0001")
                .prodOrderId(order.getId())
                .prodOrderNo(order.getOrderNo())
                .plantId(order.getPlantId())
                .workshopId(order.getWorkshopId())
                .wcId(201L)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .plannedQty(ORDER_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .kitStatus(MesKitStatusEnum.UNCHECKED.name())
                .status(MesWorkOrderStatusEnum.PENDING.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        workOrderMapper.insert(workOrder);
        return workOrder;
    }

    private OpTaskDO insertOpTask(WorkOrderDO workOrder, Long routeOpId, int seq) {
        OpTaskDO opTask = OpTaskDO.builder()
                .taskNo("OT-" + routeOpId)
                .workOrderId(workOrder.getId())
                .workOrderNo(workOrder.getWorkOrderNo())
                .routeOpId(routeOpId)
                .opId(routeOpId + 100)
                .opCode("OP" + seq)
                .opName("工序" + seq)
                .seq(seq)
                .prevTaskId(MesConstants.NONE_ID)
                .nextTaskId(MesConstants.NONE_ID)
                .plannedQty(ORDER_QTY)
                .completedQty(MesConstants.ZERO_QTY)
                .scrapQty(MesConstants.ZERO_QTY)
                .resId(MesConstants.NONE_ID)
                .keyFlag(Boolean.FALSE)
                .status(MesOpTaskStatusEnum.PENDING.name())
                .version(MesConstants.INIT_VERSION)
                .build();
        opTaskMapper.insert(opTask);
        return opTask;
    }

    /** 落一张领料申请，用于验证已预留与已领用的抵扣口径。 */
    private MaterialReqDO insertMaterialReq(ProdOrderDO order, MesMaterialReqTypeEnum reqType,
                                            MesMaterialReqStatusEnum status) {
        MaterialReqDO req = MaterialReqDO.builder()
                .reqNo("MR-" + status.name())
                .prodOrderId(order.getId())
                .prodOrderNo(order.getOrderNo())
                .workOrderId(MesConstants.NONE_ID)
                .opTaskId(MesConstants.NONE_ID)
                .reqType(reqType.name())
                .invOrgId(1L)
                .whId(1L)
                .projectId(MesConstants.NONE_ID)
                .projectTaskId(MesConstants.NONE_ID)
                .status(status.name())
                .build();
        materialReqMapper.insert(req);
        return req;
    }

    private void insertMaterialReqLine(MaterialReqDO req, Long matId, String qty, String processedQty,
                                       MesMaterialReqLineStatusEnum status) {
        materialReqLineMapper.insert(MaterialReqLineDO.builder()
                .reqId(req.getId())
                .lineNo(MesConstants.FIRST_LINE_NO)
                .routeOpMatId(MesConstants.NONE_ID)
                .matId(matId)
                .matCode("MAT-" + matId)
                .matName("物料" + matId)
                .qty(new BigDecimal(qty))
                .processedQty(new BigDecimal(processedQty))
                .uomId(UOM_ID)
                .uomCode("PCS")
                .batchId(MesConstants.NONE_ID)
                .mustIssueBeforeStart(Boolean.TRUE)
                .status(status.name())
                .build());
    }

    private MesKitCheckBO prodOrderBO(ProdOrderDO order) {
        return baseBO(order).setCheckScope(MesKitCheckScopeEnum.PROD_ORDER.name());
    }

    private MesKitCheckBO workOrderBO(ProdOrderDO order, WorkOrderDO workOrder) {
        return baseBO(order).setCheckScope(MesKitCheckScopeEnum.WORK_ORDER.name())
                .setWorkOrderId(workOrder.getId());
    }

    private MesKitCheckBO opTaskBO(ProdOrderDO order, OpTaskDO opTask) {
        return baseBO(order).setCheckScope(MesKitCheckScopeEnum.OP_TASK.name())
                .setOpTaskId(opTask.getId());
    }

    /** 基础参数：不带幂等键，走服务端按四段格式派生的那条路。 */
    private MesKitCheckBO baseBO(ProdOrderDO order) {
        MesKitCheckBO bo = new MesKitCheckBO();
        bo.setProdOrderId(order.getId());
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(order.getOrderNo());
        bo.setWhId(1L);
        return bo;
    }

    private List<KitSnapshotDO> snapshots() {
        return kitSnapshotMapper.selectList();
    }

    /** 按物料定位快照行，不按下标，避免断言被生成顺序绑住。 */
    private KitSnapshotDO snapshotOf(Long matId) {
        return snapshots().stream().filter(line -> matId.equals(line.getMatId())).findFirst().orElseThrow();
    }

    private boolean hasSnapshotOf(Long matId) {
        return snapshots().stream().anyMatch(line -> matId.equals(line.getMatId()));
    }

    private ProdOrderDO reloadOrder(ProdOrderDO order) {
        return prodOrderMapper.selectById(order.getId());
    }

}
