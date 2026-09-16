package com.iboot.pf.server.integration.mes;

import com.iboot.pf.module.mes.api.materialreq.MaterialReqApi;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.exception.ErrorCode;
import com.iboot.pf.framework.common.exception.ServiceException;
import com.iboot.pf.module.mdm.controller.admin.bom.vo.BomSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import com.iboot.pf.module.mdm.service.bom.BomService;
import com.iboot.pf.module.mes.api.materialreq.dto.CreateMaterialReqReqDTO;
import com.iboot.pf.module.mes.api.order.ProdOrderApi;
import com.iboot.pf.module.mes.api.order.dto.CreateProdOrderReqDTO;
import com.iboot.pf.module.mes.api.order.dto.ProdOrderActionReqDTO;
import com.iboot.pf.module.mes.dal.dataobject.materialreq.MaterialReqDO;
import com.iboot.pf.module.mes.dal.dataobject.materialreqline.MaterialReqLineDO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.materialreq.MaterialReqMapper;
import com.iboot.pf.module.mes.dal.mysql.materialreqline.MaterialReqLineMapper;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.ErrorCodeConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesMaterialReqLineStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqStatusEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesSourceModuleEnum;
import com.iboot.pf.module.mes.service.materialreq.MesMaterialReqDomainService;
import com.iboot.pf.module.mes.service.materialreq.bo.MesConfirmMaterialReqBO;
import com.iboot.pf.module.mes.service.materialreq.bo.MesMaterialReqActionBO;
import com.iboot.pf.module.wms.api.inorder.InOrderApi;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryBalanceRespDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import com.iboot.pf.module.wms.api.outorder.OutOrderApi;
import com.iboot.pf.module.wms.api.outorder.dto.OutOrderRespDTO;
import com.iboot.pf.module.wms.api.reservation.InventoryReservationApi;
import com.iboot.pf.module.wms.api.reservation.dto.ReservationRespDTO;
import com.iboot.pf.module.wms.enums.WmsReservationStatusEnum;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T6-03 端到端用例 3：领料三段。
 *
 * <p>依据 {@code 08-跨模块协作与端到端时序.md} 的"端到端主用例清单"第 3 条与
 * {@code 04-MES生产执行流程.md} 的"流程四：领料三段"。
 *
 * <p>核心断言是 04 文档那张三段库存状态对照表（初始现存 500、领 100）：
 *
 * <pre>
 * 阶段     qty_onhand   qty_reserved   可用量
 * 申请后      500            0          500
 * 提交后      500          100          400
 * 确认后      400            0          400
 * </pre>
 *
 * <p><b>"确认后可用量仍为 400"是本类存在的意义</b>：如果确认后变成 300，说明出库过账扣了现存、
 * 而预留没被核销，可用量被扣了两次，就是 03 里说的双重占用。这是最容易在跨模块联调中悄悄写错的一处。
 *
 * <p>库存余额、流水与单据全部走真实门面（{@link MaterialReqApi} → WMS 预留/出库门面），
 * 不 mock 任何业务门面：验证的是"跨模块闭环真的成立"，不是"本模块调了门面"。
 */
class MaterialIssueIntegrationTest extends BaseIntegrationTest {

    /** MES 动作的调用方来源模块：控制器固定传 {@code UI}，测试传 {@code INTEGRATION}（T6-01 自查的约定）。 */
    private static final String SOURCE_MODULE = MesSourceModuleEnum.INTEGRATION.name();

    /** 领料数量：与 04 文档的对照表一致（初始现存 500，领 100）。 */
    private static final BigDecimal ISSUE_QTY = new BigDecimal("100");

    /** 幂等键序号：同一测试类内多次建单/动作时保证键不撞。 */
    private static final AtomicInteger KEY_SEQ = new AtomicInteger();

    @Resource
    private MaterialReqApi materialReqApi;

    @Resource
    private MesMaterialReqDomainService materialReqDomainService;

    @Resource
    private ProdOrderApi prodOrderApi;

    @Resource
    private InventoryApi inventoryApi;

    @Resource
    private InventoryReservationApi reservationApi;

    @Resource
    private OutOrderApi outOrderApi;

    @Resource
    private InOrderApi inOrderApi;

    @Resource
    private MaterialReqMapper materialReqMapper;

    @Resource
    private MaterialReqLineMapper materialReqLineMapper;

    @Resource
    private OpTaskMapper opTaskMapper;

    @Resource
    private WorkOrderMapper workOrderMapper;

    @Resource
    private MdmFixture mdmFixture;

    @Resource
    private WmsFixture wmsFixture;

    @Resource
    private BomService bomService;

    private PilotLine line;

    /** 首道工序任务：路线工序用料挂在它上面，领料按它展开。 */
    private OpTaskDO firstTask;

    @BeforeEach
    void buildPilotLineAndOrder() {
        // 主数据 + 仓库初始库存（500，经 InOrderApi 真实过账）
        line = mdmFixture.createPilotLine();
        WmsFixture.WmsStock stock = wmsFixture.createWarehouseWithStock(line);
        line = withStock(line, stock);
        // 领料提交会按整单 BOM 展开做超发判定，fixture 的 BOM 含 PHANTOM 件而它没有子件 BOM，
        // 展开必抛 KIT_CHECK_PHANTOM_BOM_MISSING。本类只关心 PICK 件的三段库存后果，
        // 因此另起一版当期生效 BOM（不挂虚拟件），把无关分支摘掉
        line = withCleanBom(line);

        // 建单并下达，拿到工序任务——领料必须挂在工序任务上才走"按用料关系展开"那条路
        Long orderId = createAndReleaseOrder();
        firstTask = orderTasksBySeq(orderId).get(0);
    }

    /**
     * 为试点成品另建一版当期生效 BOM：只挂 PICK 子件，不含 PHANTOM。
     *
     * <p>生效日期取 2020-03-01，比 fixture 的 BOM（2020-01-01）新，因此建单时锁的是这一版。
     */
    private PilotLine withCleanBom(PilotLine pilotLine) {
        BomSaveReqVO bom = new BomSaveReqVO();
        bom.setMatId(pilotLine.fgMatId());
        bom.setVersion("V3.0");
        bom.setStatus(CommonStatusEnum.DISABLE.getStatus());
        bom.setEffectiveDate(LocalDate.of(2020, 3, 1));
        Long bomId = bomService.createBom(bom);
        bomService.createBomLine(BomLineDO.builder()
                .bomId(bomId).lineNo(10).matId(pilotLine.anyComponentMatId())
                .qty(new BigDecimal("2")).scrapRate(new BigDecimal("0.02"))
                .issueMethod("PICK").uomId(pilotLine.uomId())
                .effectiveDate(LocalDate.of(2020, 3, 1)).build());
        bom.setId(bomId);
        bom.setStatus(CommonStatusEnum.ENABLE.getStatus());
        bomService.updateBom(bom);
        return new PilotLine(pilotLine.invOrgId(), pilotLine.plantId(), pilotLine.workshopId(),
                pilotLine.wcId(), pilotLine.uomId(), pilotLine.fgMatId(), pilotLine.componentMatIds(),
                pilotLine.matCatId(), pilotLine.shiftId(), pilotLine.calendarId(), pilotLine.resId(),
                pilotLine.resCapabilityId(), pilotLine.resCalendarId(), pilotLine.opIds(),
                pilotLine.routeId(), pilotLine.routeOpIds(), pilotLine.routeOpMatIds(), bomId,
                pilotLine.whId(), pilotLine.zoneId(), pilotLine.locId(), pilotLine.initialInOrderIds(),
                pilotLine.qcPlanId(), pilotLine.qcItemIds(), pilotLine.qcRuleIds());
    }

    // ==================== 用例 3：领料三段 ====================

    /**
     * 领料三段：申请 → 提交 → 确认，逐阶段断言库存账的升降。
     *
     * <p>断言链（按 04 的对照表）：
     * <ol>
     *     <li>申请后：现存 500、预留 0，可用量 500 —— 申请阶段<b>无库存后果</b>；</li>
     *     <li>提交后：现存 500、预留 100，可用量 400 —— 预留只占可用量，不动现存；</li>
     *     <li>确认后：现存 400、预留 0，可用量 400 —— 出库扣现存、核销消预留，<b>可用量不许再降</b>。</li>
     * </ol>
     */
    @Test
    void testIssueThreeStage_StockMovesExactlyOnce() {
        // ---------- 阶段一：申请（DRAFT，无库存后果） ----------
        Long reqId = createIssueReq(ISSUE_QTY);

        MaterialReqDO draft = materialReqMapper.selectById(reqId);
        assertEquals(MesMaterialReqStatusEnum.DRAFT.name(), draft.getStatus());
        Balance afterApply = loadIssueMatBalance();
        assertQty("申请后现存", WmsFixture.INITIAL_QTY, afterApply.onhand());
        assertQty("申请后预留", BigDecimal.ZERO, afterApply.reserved());
        assertQty("申请后可用量", WmsFixture.INITIAL_QTY, afterApply.available());

        // ---------- 阶段二：提交（SUBMITTED，占预留不动现存） ----------
        submitIssue(reqId);

        MaterialReqDO submitted = materialReqMapper.selectById(reqId);
        assertEquals(MesMaterialReqStatusEnum.SUBMITTED.name(), submitted.getStatus());
        Balance afterSubmit = loadIssueMatBalance();
        assertQty("提交后现存：预留不动现存", WmsFixture.INITIAL_QTY, afterSubmit.onhand());
        assertQty("提交后预留：料被留住了", ISSUE_QTY, afterSubmit.reserved());
        assertQty("提交后可用量", WmsFixture.INITIAL_QTY.subtract(ISSUE_QTY), afterSubmit.available());

        List<MaterialReqLineDO> lines = linesOf(reqId);
        assertEquals(1, lines.size(), "首道工序的路线用料只有一行（PICK 件）");
        assertTrue(!MesConstants.NONE_ID.equals(lines.get(0).getReservationId()),
                "提交后行上必须记下 WMS 预留编号，确认阶段要靠它核销");
        assertEquals(MesMaterialReqLineStatusEnum.SUBMITTED.name(), lines.get(0).getStatus());

        // ---------- 阶段三：确认（DONE，出库过账 + 核销预留） ----------
        materialReqDomainService.confirmMaterialReq(confirmBO(reqId));

        MaterialReqDO done = materialReqMapper.selectById(reqId);
        assertEquals(MesMaterialReqStatusEnum.DONE.name(), done.getStatus());

        Balance afterConfirm = loadIssueMatBalance();
        assertQty("确认后现存：出库过账真扣了现存",
                WmsFixture.INITIAL_QTY.subtract(ISSUE_QTY), afterConfirm.onhand());
        assertQty("确认后预留：预留已被核销", BigDecimal.ZERO, afterConfirm.reserved());
        // ← 本类的守门断言：可用量 400，不是 300
        assertQty("确认后可用量必须仍是 400（核销了预留，只扣一次）",
                WmsFixture.INITIAL_QTY.subtract(ISSUE_QTY), afterConfirm.available());

        // ---------- 附带断言：预留终态、回写、单据类型、行上预留 ----------
        MaterialReqLineDO confirmedLine = linesOf(reqId).get(0);
        assertQty("确认后行的已处理量", ISSUE_QTY, confirmedLine.getProcessedQty());
        assertEquals(MesMaterialReqLineStatusEnum.DONE.name(), confirmedLine.getStatus());

        ReservationRespDTO reservation = reservationApi.getReservation(confirmedLine.getReservationId());
        assertNotNull(reservation, "预留记录必须还在（它是核销事实的载体）");
        assertEquals(WmsReservationStatusEnum.CONSUMED.name(), reservation.getStatus(),
                "确认出库后预留是 CONSUMED，不是 RELEASED");
        assertQty("核销量等于领料量", ISSUE_QTY, nvl(reservation.getReleasedQty()));

        assertNotNull(done.getWmsBizNo(), "确认后 wms_biz_no 已回写");
        assertTrue(!done.getWmsBizNo().isBlank());

        Long outOrderId = Long.valueOf(done.getWmsBizNo());
        OutOrderRespDTO outOrder = outOrderApi.getOutOrderMap(List.of(outOrderId)).get(outOrderId);
        assertNotNull(outOrder, "回写的 WMS 单据编号必须能反查出库单");
        assertEquals("PROD_ISSUE_OUT", outOrder.getBizType(), "领料出库单的业务类型是 PROD_ISSUE_OUT");

        assertTrue(!MesConstants.NONE_ID.equals(confirmedLine.getReservationId()),
                "出库行必须带预留编号：缺了它 WMS 过账后不核销，可用量会永久偏低");
    }

    /**
     * 退料：{@code RETURN} 类型确认后现存量上升，入库单 {@code biz_type} 为 {@code PROD_RETURN_IN}。
     *
     * <p>退料与领料的技术路径不同点有二：提交阶段<b>不预留</b>（退料是往仓库还货，占可用量没有意义），
     * 确认阶段建的是入库单。这里完整走三段，把这两点都锁住。
     */
    @Test
    void testReturn_IncreasesOnhandAndPostsReturnInOrder() {
        // 先正常领出 100，车间手上有料才谈得上退
        Long issueId = createIssueReq(ISSUE_QTY);
        submitIssue(issueId);
        materialReqDomainService.confirmMaterialReq(confirmBO(issueId));
        Balance afterIssue = loadIssueMatBalance();
        assertQty("领料后现存", WmsFixture.INITIAL_QTY.subtract(ISSUE_QTY), afterIssue.onhand());

        // 退料 40，显式给行（退料没有用料关系可推，必须调用方给行，且退料行必须指定批次）
        BigDecimal returnQty = new BigDecimal("40");
        Long returnId = createReturnReq(returnQty);

        // 提交：退料不预留，可用量不动
        submitIssue(returnId);
        Balance afterReturnSubmit = loadIssueMatBalance();
        assertQty("退料提交后现存未变", afterIssue.onhand(), afterReturnSubmit.onhand());
        assertQty("退料提交后预留仍为 0（退料不预留）", BigDecimal.ZERO, afterReturnSubmit.reserved());
        MaterialReqLineDO returnLine = linesOf(returnId).get(0);
        assertEquals(MesConstants.NONE_ID, returnLine.getReservationId(),
                "退料行不该有预留编号：退料是往仓库还货，占用可用量没有意义");

        // 确认：入库过账，现存上升
        materialReqDomainService.confirmMaterialReq(confirmBO(returnId));

        MaterialReqDO done = materialReqMapper.selectById(returnId);
        assertEquals(MesMaterialReqStatusEnum.DONE.name(), done.getStatus());
        Balance afterReturnConfirm = loadIssueMatBalance();
        assertQty("退料确认后现存上升",
                WmsFixture.INITIAL_QTY.subtract(ISSUE_QTY).add(returnQty), afterReturnConfirm.onhand());
        assertQty("退料确认后可用量同步上升",
                afterReturnConfirm.onhand().subtract(afterReturnConfirm.reserved()),
                afterReturnConfirm.available());

        Long inOrderId = Long.valueOf(done.getWmsBizNo());
        assertEquals("PROD_RETURN_IN", inOrderApiBizType(inOrderId),
                "退料入库单的业务类型是 PROD_RETURN_IN");
    }

    /**
     * 可用量不足：提交时整单回滚，预留表无残留。
     *
     * <p>为什么"整单回滚"是硬要求：留一半预留比全不留更难收拾——车间无法从"部分提交"的单据
     * 判断能不能开工，而被占住的可用量别的工单也领不走。
     *
     * <p>错误码断言之外，必须回头查预留表没有多出记录：只断言抛异常不够，
     * 抛异常之前已经建了预留、只是事务没配 {@code rollbackFor} 的话，异常照样抛、预留照样留。
     */
    @Test
    void testIssue_InsufficientAvailableQtyRollsBackWholeOrder() {
        BigDecimal beforeReserved = loadIssueMatBalance().reserved();
        int reservationsBefore = reservationCountOfIssueMat();

        // 申请量超过现存（500）：申请阶段不校验库存，提交阶段才拦
        Long reqId = createIssueReq(WmsFixture.INITIAL_QTY.add(new BigDecimal("1")));

        assertErrorCode(
                () -> materialReqDomainService.submitMaterialReq(actionBO(reqId, "SUBMIT_MATERIAL_REQ")),
                ErrorCodeConstants.MATERIAL_REQ_AVAILABLE_QTY_INSUFFICIENT);

        // 整单回滚：单据仍在 DRAFT，行上无预留，预留表无残留
        MaterialReqDO reloaded = materialReqMapper.selectById(reqId);
        assertEquals(MesMaterialReqStatusEnum.DRAFT.name(), reloaded.getStatus(),
                "提交失败后单据必须回滚到 DRAFT");
        assertEquals(MesConstants.NONE_ID, linesOf(reqId).get(0).getReservationId(),
                "回滚后行上不许留下预留编号");
        assertQty("失败提交后预留量未变", beforeReserved, loadIssueMatBalance().reserved());
        assertEquals(reservationsBefore, reservationCountOfIssueMat(),
                "提交失败不许在预留表留下残留记录");
    }

    // ==================== 建数据 ====================

    /** 建一张"首道工序领 {@code qty}"的领料申请（DRAFT）。明细留空由服务端按工序用料展开。 */
    private Long createIssueReq(BigDecimal qty) {
        CreateMaterialReqReqDTO req = new CreateMaterialReqReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-03-ISSUE");
        req.setIdempotencyKey(nextKey("CREATE_REQ"));
        req.setProdOrderId(prodOrderIdOf(firstTask));
        req.setWorkOrderId(firstTask.getWorkOrderId());
        req.setOpTaskId(firstTask.getId());
        req.setReqType(MesMaterialReqTypeEnum.ISSUE.name());
        req.setInvOrgId(line.invOrgId());
        req.setWhId(line.whId());
        // 明细留空：服务端按本工序的路线用料展开，与齐套/门禁同一套需求量口径
        Long reqId = materialReqApi.createMaterialReq(req);

        // 展开出来的行数量与服务端口径一致：这里按需求量改行数量，让"领 100"成为事实
        MaterialReqLineDO line = linesOf(reqId).get(0);
        materialReqLineMapper.updateById(new MaterialReqLineDO()
                .setId(line.getId())
                .setQty(qty));
        return reqId;
    }

    /** 建一张退料申请（DRAFT），显式给行——退料没有用料关系可推。 */
    private Long createReturnReq(BigDecimal qty) {
        CreateMaterialReqReqDTO req = new CreateMaterialReqReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-03-RETURN");
        req.setIdempotencyKey(nextKey("CREATE_REQ"));
        req.setProdOrderId(prodOrderIdOf(firstTask));
        req.setWorkOrderId(firstTask.getWorkOrderId());
        req.setOpTaskId(MesConstants.NONE_ID);
        req.setReqType(MesMaterialReqTypeEnum.RETURN.name());
        req.setInvOrgId(line.invOrgId());
        req.setWhId(line.whId());

        CreateMaterialReqReqDTO.Line reqLine = new CreateMaterialReqReqDTO.Line();
        reqLine.setMatId(line.anyComponentMatId());
        reqLine.setQty(qty);
        reqLine.setUomId(line.uomId());
        req.setLines(List.of(reqLine));
        return materialReqApi.createMaterialReq(req);
    }

    /** 建生产工单并下达，返回工单编号。 */
    private Long createAndReleaseOrder() {
        CreateProdOrderReqDTO req = new CreateProdOrderReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-03-ORDER");
        req.setIdempotencyKey(nextKey("CREATE_ORDER"));
        req.setPlantId(line.plantId());
        req.setWorkshopId(line.workshopId());
        req.setProjectId(MesConstants.NONE_ID);
        req.setProjectTaskId(MesConstants.NONE_ID);
        req.setMatId(line.fgMatId());
        req.setQty(PilotLine.DEMAND_QTY);
        req.setUomId(line.uomId());
        req.setDueDate(LocalDateTime.of(2026, 12, 31, 18, 0));
        req.setPriority(0);
        Long orderId = prodOrderApi.createProdOrder(req);

        ProdOrderActionReqDTO release = new ProdOrderActionReqDTO();
        release.setSourceModule(SOURCE_MODULE);
        release.setSourceBizNo("T6-03-ORDER");
        release.setIdempotencyKey(nextKey("RELEASE_ORDER"));
        release.setProdOrderId(orderId);
        prodOrderApi.releaseProdOrder(release);
        return orderId;
    }

    /* ------------------------------------------------------------------ 断言辅助 ------------------------------------------------------------------ */

    /** 领料物料的库存余额。领料占的是成品的第一个子件（BOM 上的 PICK 件）。 */
    private Balance loadIssueMatBalance() {
        InventoryQueryReqDTO query = new InventoryQueryReqDTO();
        query.setInvOrgId(line.invOrgId());
        query.setWhId(line.whId());
        query.setMatId(line.anyComponentMatId());
        query.setQualifiedOnly(Boolean.TRUE);
        List<InventoryBalanceRespDTO> balances = inventoryApi.getInventoryBalanceList(query);
        BigDecimal onhand = balances.stream().map(InventoryBalanceRespDTO::getQtyOnhand)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal reserved = balances.stream().map(InventoryBalanceRespDTO::getQtyReserved)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal available = balances.stream().map(InventoryBalanceRespDTO::getQtyAvailable)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Balance(onhand, reserved, available);
    }

    /** 领料物料上的预留记录数：用来断言"失败提交不留残留"。 */
    private int reservationCountOfIssueMat() {
        return materialReqLineMapper.selectList(MaterialReqLineDO::getMatId, line.anyComponentMatId())
                .stream().map(MaterialReqLineDO::getReservationId)
                .filter(id -> id != null && !MesConstants.NONE_ID.equals(id))
                .toList().size();
    }

    /** 数量断言：库列是 decimal(20,6)，用 compareTo 而不是 equals，避免标度差异导致假红。 */
    private void assertQty(String message, BigDecimal expected, BigDecimal actual) {
        assertNotNull(actual, message + "：实际值为 null");
        assertEquals(0, expected.compareTo(actual),
                message + "：期望 " + expected.toPlainString() + "，实际 " + actual.toPlainString());
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Long prodOrderIdOf(OpTaskDO task) {
        return workOrderMapper.selectById(task.getWorkOrderId()).getProdOrderId();
    }

    private List<MaterialReqLineDO> linesOf(Long reqId) {
        return materialReqLineMapper.selectList(MaterialReqLineDO::getReqId, reqId).stream()
                .sorted(Comparator.comparingInt(MaterialReqLineDO::getLineNo)).toList();
    }

    private List<OpTaskDO> orderTasksBySeq(Long prodOrderId) {
        List<Long> workOrderIds = workOrderMapper.selectListByProdOrderIds(List.of(prodOrderId))
                .stream().map(WorkOrderDO::getId).toList();
        return opTaskMapper.selectListByWorkOrderIds(workOrderIds).stream()
                .filter(task -> MesOpTaskStatusEnum.PENDING.name().equals(task.getStatus())
                        || MesOpTaskStatusEnum.READY.name().equals(task.getStatus()))
                .sorted(Comparator.comparing(OpTaskDO::getSeq).thenComparing(OpTaskDO::getId)).toList();
    }

    /**
     * 提交领料申请。
     *
     * <p>原缺陷（已由调度方在 WMS 侧整改）：MES 的 {@code MesMaterialReqDomainServiceImpl.reserve()}
     * 不给 {@code CreateReservationReqDTO} 设 {@code locId}，而 {@code wms_inv_reservation.loc_id}
     * 是 NOT NULL，WMS 的 insert 当时只做直通复制不做库位解析，导致任何不带批次的领料提交都必然失败。
     * 现由 {@code WmsInventoryReservationServiceImpl.reserve()} 在调用方未指定库位时按可用量最大的
     * 库位兜底解析，本方法保留异常转换只为让落库失败有一句能直接定位的失败信息。
     */
    private void submitIssue(Long reqId) {
        try {
            materialReqDomainService.submitMaterialReq(actionBO(reqId, "SUBMIT_MATERIAL_REQ"));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new IllegalStateException("领料提交失败：预留落库失败，原始异常：" + ex.getMessage(), ex);
        }
    }

    /** 提交动作命令。 */
    private MesMaterialReqActionBO actionBO(Long reqId, String action) {
        MesMaterialReqActionBO bo = new MesMaterialReqActionBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-03-" + action);
        bo.setIdempotencyKey(SOURCE_MODULE + ":" + reqId + ":" + action + ":0");
        bo.setMaterialReqId(reqId);
        return bo;
    }

    /** 确认命令，lines 留空表示全部行按剩余量全额确认。 */
    private MesConfirmMaterialReqBO confirmBO(Long reqId) {
        MesConfirmMaterialReqBO bo = new MesConfirmMaterialReqBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-03-CONFIRM_MATERIAL_REQ");
        bo.setIdempotencyKey(SOURCE_MODULE + ":" + reqId + ":CONFIRM_MATERIAL_REQ:0");
        bo.setMaterialReqId(reqId);
        return bo;
    }

    private String nextKey(String action) {
        return SOURCE_MODULE + ":T6-03:" + action + ":" + KEY_SEQ.incrementAndGet();
    }

    private PilotLine withStock(PilotLine pilotLine, WmsFixture.WmsStock stock) {
        return new PilotLine(pilotLine.invOrgId(), pilotLine.plantId(), pilotLine.workshopId(),
                pilotLine.wcId(), pilotLine.uomId(), pilotLine.fgMatId(), pilotLine.componentMatIds(),
                pilotLine.matCatId(), pilotLine.shiftId(), pilotLine.calendarId(), pilotLine.resId(),
                pilotLine.resCapabilityId(), pilotLine.resCalendarId(), pilotLine.opIds(),
                pilotLine.routeId(), pilotLine.routeOpIds(), pilotLine.routeOpMatIds(), pilotLine.bomId(),
                stock.whId(), stock.zoneId(), stock.locId(), stock.inOrderIds(),
                pilotLine.qcPlanId(), pilotLine.qcItemIds(), pilotLine.qcRuleIds());
    }

    /** 取入库单的业务类型；经 {@code InOrderApi} 的门面读取，不直接查库。 */
    private String inOrderApiBizType(Long inOrderId) {
        return inOrderApi.getInOrderMap(List.of(inOrderId)).get(inOrderId).getBizType();
    }

    /** 库存余额三元组。 */
    private record Balance(BigDecimal onhand, BigDecimal reserved, BigDecimal available) {
    }


    /**
     * 断言抛出的是指定错误码。
     *
     * <p>不用 {@code AssertUtils.assertServiceException}：它还会把消息与传入的占位参数逐个比对，
     * 而门禁与库存校验的消息参数都是运行期数据（工序号、物料编码、缺口量），
     * 在测试里再拼一遍等于把被测逻辑抄一份。这里只锁错误码——它是这条链唯一的对外契约，
     * 消息内容由各用例另行用 contains 断言。
     */
    private void assertErrorCode(org.junit.jupiter.api.function.Executable executable, ErrorCode expected) {
        ServiceException ex = assertThrows(ServiceException.class, executable);
        assertEquals(expected.getCode(), ex.getCode(),
                "错误码不匹配，实际消息：" + ex.getMessage());
    }

}
