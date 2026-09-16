package com.iboot.pf.server.integration.mes;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.module.mdm.controller.admin.bom.vo.BomSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.matalt.vo.MatAltSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.MatSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import com.iboot.pf.module.mdm.service.bom.BomService;
import com.iboot.pf.module.mdm.service.matalt.MatAltService;
import com.iboot.pf.module.mdm.service.mat.MatService;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.kitsnapshot.KitSnapshotDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitCheckScopeEnum;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.service.kitsnapshot.MesKitCheckService;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitCheckBO;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitCheckResultBO;
import com.iboot.pf.module.mes.service.prodorder.MesProdOrderDomainService;
import com.iboot.pf.module.mes.service.prodorder.ProdOrderService;
import com.iboot.pf.module.mes.service.prodorder.bo.MesReleaseOrderBO;
import com.iboot.pf.module.wms.api.inorder.InOrderApi;
import com.iboot.pf.module.wms.api.inorder.dto.CreateInOrderReqDTO;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryBalanceRespDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;
import com.iboot.pf.server.integration.BaseIntegrationTest;
import com.iboot.pf.server.integration.PilotLine;
import com.iboot.pf.server.integration.fixture.MdmFixture;
import com.iboot.pf.server.integration.fixture.WmsFixture;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T6-02 端到端用例 2：齐套检查。
 *
 * <p>依据 {@code dev-docs/业务流程/08-跨模块协作与端到端时序.md} 的"端到端主用例清单"第 2 条
 * 与 {@code 04-MES生产执行流程.md} 的"流程三：齐套检查"。
 *
 * <p>与模块内 {@code MesKitCheckServiceImplTest} 的本质区别：这里的库存是 WMS 侧<b>经真实过账产生的余额</b>
 * （{@code WmsFixture} 走 {@code InOrderApi} 建），需求量按 MDM 真实的 BOM / 路线工序用料算，
 * 走的是真实 Spring 上下文，不 mock 任何业务门面。
 *
 * <p>试点产线的 BOM（{@link MdmFixture} 建出）恰好覆盖三种发料方式：
 * <ul>
 *     <li>子件 A {@code PICK}，单位用量 2、损耗率 0.02 → 需求量 100 × 2 ÷ (1 − 0.02) = 204.081633</li>
 *     <li>子件 B {@code BACKFLUSH}，单位用量 1、无损耗 → 不计入齐套</li>
 *     <li>子件 C {@code PHANTOM}，单位用量 1 → 本体不计需求，向下展开其子件</li>
 * </ul>
 */
class KitCheckIntegrationTest extends BaseIntegrationTest {

    /** MES 动作的调用方来源模块：控制器固定传 {@code UI}，测试传 {@code INTEGRATION}。 */
    private static final String SOURCE_MODULE = "INTEGRATION";

    /**
     * 验收硬指标：100 × 2 ÷ (1 − 0.02) 精确到 6 位小数（{@code MesConstants.QTY_SCALE}，HALF_UP）。
     *
     * <p>200/0.98 = 204.081632653…，第 7 位是 6 不进位，故末位是 3。
     */
    private static final BigDecimal PICK_REQ_QTY = new BigDecimal("204.081633");

    /** 虚拟件子件的需求量：100 件 × 虚拟件用量 1 × 子件用量 3 = 300。 */
    private static final BigDecimal PHANTOM_CHILD_REQ_QTY = new BigDecimal("300");

    /** 路线工序用料的父项数量口径下：100 件 × 单位用量 2 = 200（路线用料无损耗率）。 */
    private static final BigDecimal ROUTE_OP_MAT_REQ_QTY = new BigDecimal("200");

    @Resource
    private ProdOrderService prodOrderService;

    @Resource
    private MesProdOrderDomainService domainService;

    @Resource
    private MesKitCheckService kitCheckService;

    @Resource
    private ProdOrderMapper prodOrderMapper;

    @Resource
    private WorkOrderMapper workOrderMapper;

    @Resource
    private OpTaskMapper opTaskMapper;

    @Resource
    private BomService bomService;

    @Resource
    private MatService matService;

    @Resource
    private MatAltService matAltService;

    @Resource
    private InventoryApi inventoryApi;

    @Resource
    private InOrderApi inOrderApi;

    @Resource
    private MdmFixture mdmFixture;

    @Resource
    private WmsFixture wmsFixture;

    private PilotLine line;

    /** WMS 仓储维度：{@code PilotLine} 上的三个 WMS 字段是 0 占位，真实编号由 fixture 的返回值带出。 */
    private WmsFixture.WmsStock stock;

    /** 虚拟件 C 的子件物料（本用例新建，挂在虚拟件的生效 BOM 下）。 */
    private Long phantomChildMatId;

    @BeforeEach
    void buildPilotLine() {
        line = mdmFixture.createPilotLine();
        // 初始库存：走 InOrderApi 真实过账，落 500 个子件 A（BOM 上的 PICK 件）
        stock = wmsFixture.createWarehouseWithStock(line);
        // 虚拟件必须有生效 BOM 与明细才展开得出子件；试点 BOM 只挂了虚拟件本体，子件 BOM 由本用例补
        phantomChildMatId = createPhantomChildBom();
    }

    @Test
    void testCheckKit_RequiredQtyByScrapRateFormulaWithSixDecimals() {
        // 准备：下达 + 锁 BOM 版本
        Long orderId = releaseOrder(PilotLine.DEMAND_QTY);

        // 调用：整单齐套（走 BOM 全展开）
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(orderId));

        // 断言 1（验收点名）：常规料按 父项数量 × 单位用量 ÷ (1 − 损耗率) 算需求量，精确到 6 位小数。
        // 用 compareTo + 期望常量比对，精度不符会直接失败（204.0816 或 204.08163 都不等于 204.081633）
        KitSnapshotDO pickLine = lineOf(result, line.anyComponentMatId());
        assertEquals(0, PICK_REQ_QTY.compareTo(pickLine.getReqQty()),
                "需求量必须是 100 × 2 ÷ (1 − 0.02) = 204.081633");
        assertEquals(6, pickLine.getReqQty().scale(), "需求量精度取 QTY_SCALE（6 位小数）");
        // 库存来自 WMS 真实过账（500），缺口为 0
        assertEquals(0, new BigDecimal("500").compareTo(pickLine.getAvailQty()));
        assertEquals(0, BigDecimal.ZERO.compareTo(pickLine.getShortQty()));
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), pickLine.getResult());
        // 快照落的是主数据的可读字段，不是原始 ID
        assertNotNull(pickLine.getMatCode());
        assertNotNull(pickLine.getMatName());
        assertNotNull(pickLine.getCheckNo());
        assertEquals(MesKitCheckScopeEnum.PROD_ORDER.name(), pickLine.getCheckScope());
        assertEquals(MesConstants.NONE_ID, pickLine.getWorkOrderId());
        assertEquals(MesConstants.NONE_ID, pickLine.getOpTaskId());
    }

    @Test
    void testCheckKit_PhantomNotRequiredButItsChildrenAre() {
        // 准备 + 调用
        Long orderId = releaseOrder(PilotLine.DEMAND_QTY);
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(orderId));

        // 断言 2：PHANTOM 虚拟件不产生需求，其子件产生需求
        Long phantomMatId = line.componentMatIds().get(2);
        assertTrue(!hasLineOf(result, phantomMatId),
                "虚拟件本体不产生需求行：它物理上不存在，库存永远是 0，算它齐套永远不通过");
        KitSnapshotDO childLine = lineOf(result, phantomChildMatId);
        assertEquals(0, PHANTOM_CHILD_REQ_QTY.compareTo(childLine.getReqQty()),
                "虚拟件的子件产生需求 100 × 1 × 3 = 300");
        // 子件没有库存，因此这一行是缺料：证明子件是真的进了齐套判定，不是被静默丢掉
        assertEquals(0, BigDecimal.ZERO.compareTo(childLine.getAvailQty()));
        assertEquals(0, PHANTOM_CHILD_REQ_QTY.compareTo(childLine.getShortQty()));
        assertEquals(MesKitStatusEnum.SHORTAGE.name(), childLine.getResult());
        // 常规料那行是齐套、子件这行是短缺，汇总结论是"部分齐套"——
        // 若虚拟件本体被当成常规料，它自己会成一行永远缺料的行，结论同样是部分齐套，
        // 但快照行数会多一行，因此下面按行数把"本体没有产生需求"钉死
        assertEquals(2, result.getLines().size(), "整单档应恰好两行：常规料 + 虚拟件子件");
        assertEquals(MesKitStatusEnum.PARTIAL_KIT.name(), result.getResult());
    }

    @Test
    void testCheckKit_BackflushExcludedFromSnapshot() {
        // 准备 + 调用
        Long orderId = releaseOrder(PilotLine.DEMAND_QTY);
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(orderId));

        // 断言 3：BACKFLUSH 倒冲件完全不出现在快照里。
        // 若被计入，它库存为 0 会把结论压成短缺，辅料没领就开不了工——04 文档点名的错法
        Long backflushMatId = line.componentMatIds().get(1);
        assertTrue(!hasLineOf(result, backflushMatId), "倒冲件报工时才反冲，不该计入齐套");
        assertTrue(result.getLines().stream().noneMatch(
                snapshot -> backflushMatId.equals(snapshot.getMatId())));
    }

    @Test
    void testCheckKit_AlternativeMaterialCoversShortage() {
        // 准备：主料取虚拟件的子件——它一颗库存都没有（虚拟件子件的 BOM 由本用例新建，
        // 从没入过库），需求量 300 全缺。给它配一个替代料（借用试点产线的 PICK 件，
        // 它有 500 的真实在库量），于是"主料不足、替代料充足"是真实账面事实，不是 mock 出来的
        Long noStockMatId = phantomChildMatId;
        Long altMatId = line.anyComponentMatId();
        MatAltSaveReqVO alt = new MatAltSaveReqVO();
        alt.setMatId(noStockMatId);
        alt.setAltMatId(altMatId);
        alt.setPriority(1);
        matAltService.createMatAlt(alt);

        Long orderId = releaseOrder(PilotLine.DEMAND_QTY);

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(orderId));

        // 断言 4（验收点名）：主料不足替代料充足时结论为 FULL_KIT 且 alt_mat_id 非 0
        KitSnapshotDO line = lineOf(result, noStockMatId);
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), line.getResult(),
                "替代料够用就该判齐套，否则能开的工被主料缺料挡住");
        assertNotEquals(MesConstants.NONE_ID, line.getAltMatId(), "命中的替代料编号必须落库");
        assertEquals(altMatId, line.getAltMatId());
        assertEquals(0, BigDecimal.ZERO.compareTo(line.getShortQty()));
        // avail_qty 仍记主料可用量（0）：换成替代料的量，事后就看不出主料缺了多少
        assertEquals(0, BigDecimal.ZERO.compareTo(line.getAvailQty()));
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), result.getResult());
    }

    @Test
    void testCheckKit_ThreeScopesCoverTheirOwnRequirementRange() {
        // 准备：下达后拿作业工单与工序任务，三档各查一次
        Long orderId = releaseOrder(PilotLine.DEMAND_QTY);
        WorkOrderDO workOrder = workOrdersOf(orderId).get(0);
        OpTaskDO firstTask = orderTasksBySeq(orderId).get(0);

        // 调用一：整单档，按 BOM 全展开
        MesKitCheckResultBO prodScope = kitCheckService.checkKit(prodOrderBO(orderId));
        // 断档一：整单档含 BOM 上全部计入的物料——常规料 204.081633 与虚拟件子件 300，两条
        assertEquals(2, prodScope.getLines().size(), "整单档展开 BOM：常规料 + 虚拟件子件");
        assertEquals(0, PICK_REQ_QTY.compareTo(lineOf(prodScope, line.anyComponentMatId()).getReqQty()));
        assertEquals(0, PHANTOM_CHILD_REQ_QTY.compareTo(lineOf(prodScope, phantomChildMatId).getReqQty()));

        // 调用二：作业工单档，按本段覆盖工序的路线用料展开
        MesKitCheckResultBO workOrderScope = kitCheckService.checkKit(workOrderBO(orderId, workOrder.getId()));
        // 断档二：需求改由 mdm_route_op_mat 出，只有工序一的一道用料（单位用量 2、无损耗率），
        // 需求量 100 × 2 = 200 —— 与整单档的 204.081633 不同，说明两档走的确实是不同的需求来源
        assertEquals(1, workOrderScope.getLines().size(), "本段只有一道工序挂了用料");
        KitSnapshotDO workOrderLine = lineOf(workOrderScope, line.anyComponentMatId());
        assertEquals(0, ROUTE_OP_MAT_REQ_QTY.compareTo(workOrderLine.getReqQty()),
                "作业工单档的父项数量取该段计划数量");
        assertEquals(MesKitCheckScopeEnum.WORK_ORDER.name(), workOrderLine.getCheckScope());
        assertEquals(workOrder.getId(), workOrderLine.getWorkOrderId());
        assertEquals(MesConstants.NONE_ID, workOrderLine.getOpTaskId());
        // 路线工序用料没有损耗率，因此需求里不含损耗放大
        assertTrue(PICK_REQ_QTY.compareTo(workOrderLine.getReqQty()) > 0,
                "BOM 档含损耗率、路线档不含，两档需求量必然不同");

        // 调用三：工序任务档，只查这一道工序的用料
        MesKitCheckResultBO opTaskScope = kitCheckService.checkKit(opTaskBO(orderId, firstTask.getId()));
        // 断档三：需求量与父项数量口径都落在单道工序上
        assertEquals(1, opTaskScope.getLines().size());
        KitSnapshotDO opTaskLine = lineOf(opTaskScope, line.anyComponentMatId());
        assertEquals(MesKitCheckScopeEnum.OP_TASK.name(), opTaskLine.getCheckScope());
        assertEquals(firstTask.getId(), opTaskLine.getOpTaskId());
        assertEquals(MesConstants.NONE_ID, opTaskLine.getWorkOrderId());
        assertEquals(0, ROUTE_OP_MAT_REQ_QTY.compareTo(opTaskLine.getReqQty()));
        // 工序任务档按工序任务自己的计划数量算，首道工序计划数量 = 工单数量
        assertEquals(0, firstTask.getPlannedQty().compareTo(PilotLine.DEMAND_QTY));
    }

    @Test
    void testCheckKit_DoesNotOccupyStock() {
        // 准备：把虚拟件子件的缺口也用真实过账补上，让整单判齐套（齐套通过是最该验"占不占库存"的场景）
        stockIn(phantomChildMatId, PHANTOM_CHILD_REQ_QTY);
        Long orderId = releaseOrder(PilotLine.DEMAND_QTY);
        Long pickMatId = line.anyComponentMatId();
        BigDecimal reservedBefore = reservedQtyOf(pickMatId);
        BigDecimal onhandBefore = onhandQtyOf(pickMatId);
        assertEquals(0, BigDecimal.ZERO.compareTo(reservedBefore), "初始库存没有预留");

        // 调用
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(orderId));
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), result.getResult());

        // 断言 6（验收点名）：齐套不占库存——检查前后 qty_reserved 未变，现存量也未变。
        // 快照是检查时点的口径证据，真正的占用发生在领料提交时的预留动作
        assertEquals(0, reservedBefore.compareTo(reservedQtyOf(pickMatId)), "齐套检查不许产生预留");
        assertEquals(0, onhandBefore.compareTo(onhandQtyOf(pickMatId)), "齐套检查不许动现存量");
        assertEquals(0, BigDecimal.ZERO.compareTo(reservedQtyOf(pickMatId)));
        // 快照上记的 reserved_qty 是"本工单已备下的量"，不是新产生的预留：本用例一件料都没领，因此是 0
        assertTrue(result.getLines().stream().allMatch(
                snapshot -> BigDecimal.ZERO.compareTo(snapshot.getReservedQty()) == 0));
    }

    @Test
    void testCheckKit_FullKitAdvancesProdOrderToReady() {
        // 准备：下达后的工单停在 RELEASED
        Long orderId = releaseOrder(PilotLine.DEMAND_QTY);
        assertEquals(MesProdOrderStatusEnum.RELEASED.name(), prodOrderMapper.selectById(orderId).getStatus());

        // 调用：齐套通过（把虚拟件子件的缺口用真实过账补上，让整单判齐套）
        stockIn(phantomChildMatId, PHANTOM_CHILD_REQ_QTY);
        MesKitCheckResultBO result = kitCheckService.checkKit(prodOrderBO(orderId));

        // 断言 7：FULL_KIT 时生产工单推进到 READY，齐套状态回写
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), result.getResult());
        ProdOrderDO reloaded = prodOrderMapper.selectById(orderId);
        assertEquals(MesProdOrderStatusEnum.READY.name(), reloaded.getStatus(),
                "READY 的含义是料已备好可以开工，少了这条推进车间永远开不了工");
        assertEquals(MesKitStatusEnum.FULL_KIT.name(), reloaded.getKitStatus());
        // 作业工单的齐套状态不回写：整单档只回写生产工单
        assertEquals(MesKitStatusEnum.UNCHECKED.name(),
                workOrdersOf(orderId).get(0).getKitStatus());
    }

    /* ------------------------------------------------------------------ 建数据 ------------------------------------------------------------------ */

    /** 建一张 DRAFT 工单并下达，返回工单编号（后续对齐套、开工门禁的前提）。 */
    private Long releaseOrder(BigDecimal qty) {
        Long orderId = createDraftOrder(line.fgMatId(), line.bomId(), qty);
        release(orderId);
        return orderId;
    }

    private Long createDraftOrder(Long fgMatId, Long bomId, BigDecimal qty) {
        ProdOrderSaveReqVO reqVO = new ProdOrderSaveReqVO();
        reqVO.setPlantId(line.plantId());
        reqVO.setWorkshopId(line.workshopId());
        reqVO.setProjectId(MesConstants.NONE_ID);
        reqVO.setProjectTaskId(MesConstants.NONE_ID);
        reqVO.setMatId(fgMatId);
        reqVO.setQty(qty);
        reqVO.setUomId(line.uomId());
        reqVO.setBomId(bomId);
        reqVO.setRouteId(line.routeId());
        reqVO.setDueDate(LocalDateTime.of(2026, 12, 31, 18, 0));
        reqVO.setPriority(0);
        return prodOrderService.createProdOrder(reqVO);
    }

    /** 真实下达：锁生效版本并展开作业工单与工序任务。 */
    private void release(Long orderId) {
        MesReleaseOrderBO bo = new MesReleaseOrderBO();
        bo.setProdOrderId(orderId);
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(prodOrderMapper.selectById(orderId).getOrderNo());
        domainService.releaseProdOrder(bo);
    }

    /**
     * 经 {@code InOrderApi} 真实过账补一批库存。
     *
     * <p>{@code uom_code} 等快照列是调用方责任，Service 不回填（T6-01 自查点名），故显式带上。
     */
    private void stockIn(Long matId, BigDecimal qty) {
        CreateInOrderReqDTO req = new CreateInOrderReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-02-KIT-STOCK");
        // 幂等键带上物料编号：一个用例内可能给多个物料补料
        req.setIdempotencyKey("INTEGRATION:T6-02-KIT-STOCK:CREATE_IN:" + matId);
        req.setBizType("PO_IN");
        req.setInvOrgId(line.invOrgId());
        req.setPlantId(line.plantId());
        req.setWhId(stock.whId());
        req.setQualityStatus("QUALIFIED");

        CreateInOrderReqDTO.Line inLine = new CreateInOrderReqDTO.Line();
        inLine.setLineNo(10);
        inLine.setMatId(matId);
        inLine.setUomId(line.uomId());
        inLine.setUomCode(MdmFixture.UOM_CODE);
        inLine.setLocId(stock.locId());
        inLine.setPlanQty(qty);
        req.setLines(List.of(inLine));

        Long inOrderId = inOrderApi.createInOrder(req);
        ConfirmOrderReqDTO confirm = new ConfirmOrderReqDTO();
        confirm.setSourceModule(SOURCE_MODULE);
        confirm.setSourceBizNo("T6-02-KIT-STOCK");
        confirm.setIdempotencyKey("INTEGRATION:T6-02-KIT-STOCK:CONFIRM_IN:" + inOrderId);
        confirm.setOrderId(inOrderId);
        inOrderApi.confirmInOrder(confirm);
    }

    /**
     * 给虚拟件建一份生效 BOM，下挂一个新建的真实子件（单位用量 3）。
     *
     * <p>试点 BOM 只把虚拟件挂了进来，虚拟件自己的 BOM 得由用例补——没有生效 BOM 时
     * 齐套会抛 {@code KIT_CHECK_PHANTOM_BOM_MISSING}（这是 04 文档要求的显式报错，不是静默跳过）。
     */
    private Long createPhantomChildBom() {
        Long childMatId = createMaterial(line, "集成测试虚拟件子件");
        BomSaveReqVO bom = new BomSaveReqVO();
        bom.setMatId(line.componentMatIds().get(2));
        bom.setVersion("V1.0");
        bom.setStatus(CommonStatusEnum.DISABLE.getStatus());
        bom.setEffectiveDate(LocalDate.of(2020, 1, 1));
        Long bomId = bomService.createBom(bom);
        bomService.createBomLine(BomLineDO.builder()
                .bomId(bomId)
                .lineNo(10)
                .matId(childMatId)
                .qty(new BigDecimal("3"))
                .scrapRate(BigDecimal.ZERO)
                .issueMethod("PICK")
                .uomId(line.uomId())
                .effectiveDate(LocalDate.of(2020, 1, 1))
                .build());
        bom.setId(bomId);
        bom.setStatus(CommonStatusEnum.ENABLE.getStatus());
        bomService.updateBom(bom);
        return childMatId;
    }

    /** 建一个成品物料（走真实 Service，编码由编码规则分配）。 */
    private Long createMaterial(PilotLine pilotLine, String name) {
        MatSaveReqVO mat = new MatSaveReqVO();
        mat.setCatId(pilotLine.matCatId());
        mat.setName(name);
        mat.setMatType("FG");
        mat.setBaseUomId(pilotLine.uomId());
        mat.setBatchFlag(false);
        mat.setSnFlag(false);
        mat.setPlanStrategy("MTO");
        mat.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return matService.createMat(mat);
    }

    /* ------------------------------------------------------------------ 断言辅助 ------------------------------------------------------------------ */

    private MesKitCheckBO prodOrderBO(Long orderId) {
        return baseBO(orderId).setCheckScope(MesKitCheckScopeEnum.PROD_ORDER.name());
    }

    private MesKitCheckBO workOrderBO(Long orderId, Long workOrderId) {
        return baseBO(orderId).setCheckScope(MesKitCheckScopeEnum.WORK_ORDER.name())
                .setWorkOrderId(workOrderId);
    }

    private MesKitCheckBO opTaskBO(Long orderId, Long opTaskId) {
        return baseBO(orderId).setCheckScope(MesKitCheckScopeEnum.OP_TASK.name())
                .setOpTaskId(opTaskId);
    }

    /** 基础参数：来源模块必填（幂等键第一段），幂等键留空走服务端派生。 */
    private MesKitCheckBO baseBO(Long orderId) {
        MesKitCheckBO bo = new MesKitCheckBO();
        bo.setProdOrderId(orderId);
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(prodOrderMapper.selectById(orderId).getOrderNo());
        bo.setWhId(stock.whId());
        return bo;
    }

    /** 按物料定位快照行，不按下标，避免断言被生成顺序绑住。 */
    private KitSnapshotDO lineOf(MesKitCheckResultBO result, Long matId) {
        return result.getLines().stream()
                .filter(line -> matId.equals(line.getMatId())).findFirst().orElseThrow(
                        () -> new AssertionError("快照里没有物料 " + matId + " 的需求行"));
    }

    private boolean hasLineOf(MesKitCheckResultBO result, Long matId) {
        return result.getLines().stream().anyMatch(line -> matId.equals(line.getMatId()));
    }

    private List<WorkOrderDO> workOrdersOf(Long prodOrderId) {
        return workOrderMapper.selectListByProdOrderIds(List.of(prodOrderId));
    }

    private List<OpTaskDO> orderTasksBySeq(Long prodOrderId) {
        List<Long> workOrderIds = workOrdersOf(prodOrderId).stream().map(WorkOrderDO::getId).toList();
        return opTaskMapper.selectListByWorkOrderIds(workOrderIds).stream()
                .sorted(Comparator.comparing(OpTaskDO::getSeq).thenComparing(OpTaskDO::getId)).toList();
    }

    /** 某个物料全部余额行的预留量合计。 */
    private BigDecimal reservedQtyOf(Long matId) {
        return sumOf(matId, InventoryBalanceRespDTO::getQtyReserved);
    }

    /** 某个物料全部余额行的现存量合计。 */
    private BigDecimal onhandQtyOf(Long matId) {
        return sumOf(matId, InventoryBalanceRespDTO::getQtyOnhand);
    }

    private BigDecimal sumOf(Long matId, java.util.function.Function<InventoryBalanceRespDTO, BigDecimal> getter) {
        InventoryQueryReqDTO query = new InventoryQueryReqDTO();
        query.setMatId(matId);
        return inventoryApi.getInventoryBalanceList(query).stream()
                .map(getter)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
