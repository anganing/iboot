package com.iboot.pf.server.integration.mes;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.exception.ServiceException;
import com.iboot.pf.module.mdm.controller.admin.bom.vo.BomSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.MatRespVO;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.MatSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.op.vo.OpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.ResCalendarBatchSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteOpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import com.iboot.pf.module.mdm.service.bom.BomService;
import com.iboot.pf.module.mdm.service.mat.MatService;
import com.iboot.pf.module.mdm.service.op.OpService;
import com.iboot.pf.module.mdm.service.rescalendar.ResCalendarService;
import com.iboot.pf.module.mdm.service.route.RouteService;
import com.iboot.pf.module.mes.api.materialreq.MaterialReqApi;
import com.iboot.pf.module.mes.api.materialreq.dto.CreateMaterialReqReqDTO;
import com.iboot.pf.module.mes.api.optask.OpTaskApi;
import com.iboot.pf.module.mes.api.optask.dto.ApplyScheduleReqDTO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderSaveReqVO;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.dataobject.prodreport.ProdReportDO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import com.iboot.pf.module.mes.dal.mysql.optask.OpTaskMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.dal.mysql.prodreport.ProdReportMapper;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.module.mes.dal.mysql.workorder.WorkOrderMapper;
import com.iboot.pf.module.mes.enums.ErrorCodeConstants;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.enums.MesWipTxnTypeEnum;
import com.iboot.pf.module.mes.api.materialreq.MaterialReqApi;
import com.iboot.pf.module.mes.api.materialreq.dto.CreateMaterialReqReqDTO;
import com.iboot.pf.module.mes.dal.mysql.proddemand.ProdDemandMapper;
import com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum;
import com.iboot.pf.module.mes.service.materialreq.MesMaterialReqDomainService;
import com.iboot.pf.module.mes.service.materialreq.bo.MesMaterialReqActionBO;
import com.iboot.pf.module.mes.service.optask.MesOpTaskDomainService;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskActionBO;
import com.iboot.pf.module.mes.service.optask.bo.MesOpTaskFinishBO;
import com.iboot.pf.module.mes.service.prodorder.MesFinishInboundService;
import com.iboot.pf.module.mes.service.prodorder.MesProdOrderDomainService;
import com.iboot.pf.module.mes.service.prodorder.ProdOrderService;
import com.iboot.pf.module.mes.service.prodorder.bo.MesCloseOrderBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesFinishInboundBO;
import com.iboot.pf.module.mes.service.prodorder.bo.MesReleaseOrderBO;
import com.iboot.pf.module.mes.service.prodreport.MesProdReportDomainService;
import com.iboot.pf.module.mes.service.prodreport.bo.MesSubmitReportBO;
import com.iboot.pf.module.wms.api.inorder.InOrderApi;
import com.iboot.pf.module.wms.api.inorder.dto.CreateInOrderReqDTO;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.order.dto.ConfirmOrderReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryBalanceRespDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnQueryReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderDO;
import com.iboot.pf.module.wms.dal.mysql.batch.WmsBatchMapper;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderMapper;
import com.iboot.pf.module.wms.enums.WmsInBizTypeEnum;
import com.iboot.pf.server.integration.BaseIntegrationTest;
import com.iboot.pf.server.integration.PilotLine;
import com.iboot.pf.server.integration.fixture.MdmFixture;
import com.iboot.pf.server.integration.fixture.MesFixture;
import com.iboot.pf.server.integration.fixture.WmsFixture;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * T6-04 端到端用例 9 与 10：完工入库与工单关闭。
 *
 * <p>依据 {@code dev-docs/业务流程/08-跨模块协作与端到端时序.md} 的"端到端主用例清单"第 9、10 条
 * 与 {@code 04-MES生产执行流程.md} 的"流程七：完工入库与工单关闭"。
 *
 * <p>本类盯的是<b>跨模块的账能不能对上</b>：MES 侧的完工数量怎么变成 WMS 侧的成品余额，
 * 关闭时预留如何释放、倒冲件如何反冲、需求如何收尾。这三件事任何一件没做，
 * 现场看不到成品、仓库里的可用量被一张已收尾的工单永久占着、倒冲件的消耗一分钱不入账。
 *
 * <p>成品物料的默认仓库与默认库位：MES 不挑库位（那是 WMS 上架策略的职责），
 * 入库时回落 {@code mdm_mat.default_wh_id / default_loc_id}，因此本类在 {@code @BeforeEach}
 * 里把试点成品的这两个字段补上——不补时自动入库会静默跳过，手工入库会报
 * {@code PROD_ORDER_INBOUND_STOCK_REQUIRED}（这两条路各自都有用例验）。
 */
class FinishInboundIntegrationTest extends BaseIntegrationTest {

    /** MES 动作的调用方来源模块：控制器固定传 {@code UI}，测试传 {@code INTEGRATION}。 */
    private static final String SOURCE_MODULE = "INTEGRATION";

    /** 单道工序的计划数量（本类只用 1 道工序的路线，让"末道"没有歧义）。 */
    private static final BigDecimal PLANNED_QTY = new BigDecimal("100");

    private static final AtomicInteger KEY_SEQ = new AtomicInteger();

    /** 本类新建主数据的生效日期：写成过去日期才能被 {@code getEffectiveRoute} 命中。 */
    private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2020, 1, 1);

    @Resource
    private ProdOrderService prodOrderService;

    @Resource
    private MesProdOrderDomainService prodOrderDomainService;

    @Resource
    private MesProdReportDomainService reportDomainService;

    @Resource
    private MesOpTaskDomainService opTaskDomainService;

    @Resource
    private MesFinishInboundService finishInboundService;

    @Resource
    private ProdOrderMapper prodOrderMapper;

    @Resource
    private WorkOrderMapper workOrderMapper;

    @Resource
    private OpTaskMapper opTaskMapper;

    @Resource
    private ProdReportMapper prodReportMapper;

    @Resource
    private WipTxnMapper wipTxnMapper;

    @Resource
    private WmsInOrderMapper inOrderMapper;

    @Resource
    private WmsBatchMapper batchMapper;

    @Resource
    private InventoryApi inventoryApi;

    @Resource
    private InOrderApi inOrderApi;

    @Resource
    private MatService matService;

    @Resource
    private BomService bomService;

    @Resource
    private RouteService routeService;

    @Resource
    private OpService opService;

    @Resource
    private OpTaskApi opTaskApi;

    @Resource
    private ResCalendarService resCalendarService;

    @Resource
    private MdmFixture mdmFixture;

    @Resource
    private MesFixture mesFixture;

    @Resource
    private WmsFixture wmsFixture;

    @Resource
    private MaterialReqApi materialReqApi;

    @Resource
    private MesMaterialReqDomainService materialReqDomainService;

    @Resource
    private ProdDemandMapper demandMapper;

    private PilotLine line;

    /** WMS 仓储维度：{@code PilotLine} 上的三个 WMS 字段是 0 占位，真实编号由 fixture 返回值带出。 */
    private WmsFixture.WmsStock stock;

    @BeforeEach
    void buildPilotLine() {
        line = mdmFixture.createPilotLine();
        stock = wmsFixture.createWarehouseWithStock(line);
        // 试点 BOM 只挂了虚拟件本体，虚拟件自己的生效 BOM 得由用例补：领料提交时的超发判定会对整单
        // BOM 调 expandByBom，展开不到子件就抛 KIT_CHECK_PHANTOM_BOM_MISSING（T6-02/T6-03 同一坑）
        createPhantomChildBom();
    }

    /** 给虚拟件建一份生效 BOM 并挂一个真实子件，让整单 BOM 能完整向下展开。 */
    private void createPhantomChildBom() {
        Long childMatId = createMaterial("T6-04 虚拟件子件");
        BomSaveReqVO bom = new BomSaveReqVO();
        bom.setMatId(line.componentMatIds().get(2));
        bom.setVersion("V1.0");
        bom.setStatus(CommonStatusEnum.DISABLE.getStatus());
        bom.setEffectiveDate(EFFECTIVE_DATE);
        Long bomId = bomService.createBom(bom);
        bomService.createBomLine(BomLineDO.builder()
                .bomId(bomId)
                .lineNo(10)
                .matId(childMatId)
                .qty(BigDecimal.ONE)
                .scrapRate(BigDecimal.ZERO)
                .issueMethod("PICK")
                .uomId(line.uomId())
                .effectiveDate(EFFECTIVE_DATE)
                .build());
        bom.setId(bomId);
        bom.setStatus(CommonStatusEnum.ENABLE.getStatus());
        bomService.updateBom(bom);
    }

    /** 建一个物料（走真实 Service，编码由编码规则分配）。 */
    private Long createMaterial(String name) {
        MatSaveReqVO mat = new MatSaveReqVO();
        mat.setCatId(line.matCatId());
        mat.setName(name);
        mat.setMatType("RM");
        mat.setBaseUomId(line.uomId());
        mat.setBatchFlag(false);
        mat.setSnFlag(false);
        mat.setPlanStrategy("MTO");
        mat.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return matService.createMat(mat);
    }

    @Test
    void testFinishInbound_RegistersBatchAndIncreasesFinishedGoodsStock() {
        // 准备：先下单完工，再给成品配上默认仓库与库位。
        // 顺序不能反：完工那一刻会自动入库，配好默认库位后自动那一步就已经把 100 件入完了，
        // 手工入库就只剩 0 可入。因此"手工入库"这条路径要在默认库位缺失的前提下走到完工
        Long orderId = completeOrder(PLANNED_QTY);
        setDefaultStockOfFg(line.fgMatId(), stock.whId(), stock.locId());

        // 调用：手工完工入库，显式指定数量 100
        MesFinishInboundBO bo = inboundBO(orderId, PLANNED_QTY);
        Long inOrderId = finishInboundService.createFinishInbound(bo);

        // 断言 1：入库单 biz_type 为 PROD_FINISH_IN，带 prod_order_no
        WmsInOrderDO inOrder = inOrderMapper.selectById(inOrderId);
        assertNotNull(inOrder);
        assertEquals(WmsInBizTypeEnum.PROD_FINISH_IN.name(), inOrder.getBizType());
        assertEquals(prodOrderMapper.selectById(orderId).getOrderNo(), inOrder.getProdOrderNo());
        assertEquals(stock.whId(), inOrder.getWhId());

        // 断言 2：成品批次已注册（wms_batch 有记录，supplier_id 为 0——成品不是采购来的）
        String lotNo = prodOrderMapper.selectById(orderId).getOrderNo() + "-"
                + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        WmsBatchDO batch = batchMapper.selectByMatIdAndLotNo(line.fgMatId(), lotNo);
        assertNotNull(batch, "成品批次必须先注册，否则成品进库房时没有批次台账，追溯链在成品这一段断掉");
        assertEquals(line.fgMatId(), batch.getMatId());
        assertEquals(0L, batch.getSupplierId(), "成品批次无供应商，0 是「无来源」的口径");

        // 断言 3：成品库余额增加，数量等于合格数
        assertEquals(0, PLANNED_QTY.compareTo(onhandQtyOf(line.fgMatId(), stock.whId())),
                "过账后成品余额必须等于合格数");

        // 断言 4：MES 侧记一条 FINISH_INBOUND 流水，累计入库量由它推导
        assertEquals(0, PLANNED_QTY.compareTo(finishInboundService.inboundedQty(orderId)));
        List<WipTxnDO> inboundTxns = inboundTxnsOf(orderId);
        assertEquals(1, inboundTxns.size());
        assertEquals(MesWipTxnTypeEnum.FINISH_INBOUND.name(), inboundTxns.get(0).getTxnType());
        assertEquals(0, PLANNED_QTY.compareTo(inboundTxns.get(0).getQty()));

        // 断言 5：重复下发同一张单返回首次那张，余额只涨一次
        Long again = finishInboundService.createFinishInbound(bo);
        assertEquals(inOrderId, again, "同幂等键重复下发返回首次那张单");
        assertEquals(0, PLANNED_QTY.compareTo(onhandQtyOf(line.fgMatId(), stock.whId())),
                "重复下发不许第二次过账");
    }

    @Test
    void testFinishInbound_TwoBatchesNeverExceedCompletedQty() {
        // 准备：完工 100（默认库位缺失，自动入库静默跳过），随后配上默认库位走手工分批
        Long orderId = completeOrder(PLANNED_QTY);
        setDefaultStockOfFg(line.fgMatId(), stock.whId(), stock.locId());

        // 调用：分两批入库 60 + 40
        Long firstInOrderId = finishInboundService.createFinishInbound(inboundBO(orderId, new BigDecimal("60")));
        Long secondInOrderId = finishInboundService.createFinishInbound(inboundBO(orderId, new BigDecimal("40")));

        // 断言 4（分批）：两次各建一张单，累计不超过 completed_qty
        assertTrue(!firstInOrderId.equals(secondInOrderId), "分批入库每次一个幂等键，各建一张单");
        assertEquals(0, PLANNED_QTY.compareTo(finishInboundService.inboundedQty(orderId)));
        assertEquals(0, PLANNED_QTY.compareTo(onhandQtyOf(line.fgMatId(), stock.whId())));
        assertEquals(2, inboundTxnsOf(orderId).size(), "每次入库写一条流水");
        // 分批入库用同一个批次号会落到同一个批次上——"一张工单的成品属于同一批"的语义
        String lotNo = prodOrderMapper.selectById(orderId).getOrderNo() + "-"
                + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertNotNull(batchMapper.selectByMatIdAndLotNo(line.fgMatId(), lotNo));

        // 断言：入满之后再入不产生第三张单（剩余量为零，静默返回）
        Long third = finishInboundService.createFinishInbound(inboundBO(orderId, null));
        assertEquals(null, third, "剩余可入库量为零时不再建单");
        assertEquals(0, PLANNED_QTY.compareTo(onhandQtyOf(line.fgMatId(), stock.whId())),
                "余额不许凭空多出成品");

        // 断言：显式给一个超过已完工数量的值必须拦下——入库量超过已完工数量等于凭空多出成品
        MesFinishInboundBO over = inboundBO(orderId, new BigDecimal("1"));
        ServiceException exceeded = assertThrows(ServiceException.class,
                () -> finishInboundService.createFinishInbound(over));
        assertEquals(ErrorCodeConstants.PROD_ORDER_INBOUND_QTY_EXCEEDED.getCode(), exceeded.getCode());
    }

    @Test
    void testFinishInbound_ManualWithoutDefaultStockRejectedAndAutoSilentlySkipped() {
        // 准备：成品不配默认仓库与库位（模拟主数据配置缺失），走到完工
        Long orderId = completeOrder(PLANNED_QTY);

        // 断言：手工入库是人发起的动作，配置缺失必须报错，把问题暴露给发起人才能被修好
        ServiceException missing = assertThrows(ServiceException.class,
                () -> finishInboundService.createFinishInbound(inboundBO(orderId, PLANNED_QTY)));
        assertEquals(ErrorCodeConstants.PROD_ORDER_INBOUND_STOCK_REQUIRED.getCode(), missing.getCode());
        assertEquals(0, BigDecimal.ZERO.compareTo(finishInboundService.inboundedQty(orderId)),
                "报错时不落任何入库流水");

        // 断言：自动入库（完工带动）配置缺失时静默跳过——完工是车间现场动作，
        // 不该因为成品物料没配默认仓库而完不了工；没入的量留在差额里，事后手工补入
        assertEquals(MesProdOrderStatusEnum.DONE.name(), prodOrderMapper.selectById(orderId).getStatus(),
                "自动入库跳过不影响完工本身");
        assertEquals(0, BigDecimal.ZERO.compareTo(finishInboundService.inboundedQty(orderId)),
                "自动入库静默跳过，一条流水都不落");
        assertEquals(0, BigDecimal.ZERO.compareTo(onhandQtyOf(line.fgMatId(), stock.whId())),
                "跳过时成品库不动");
    }

    @Test
    void testFinishInbound_AutoInboundOnOrderDone() {
        // 准备：给成品配好默认仓库与库位，下单并推到末道完工
        setDefaultStockOfFg(line.fgMatId(), stock.whId(), stock.locId());
        Long orderId = releaseSingleOpOrder();

        // 调用：完工动作本身就会触发自动入库（04 文档流程七：工单一转 DONE 就入成品）
        Long autoInOrderId = completeOrderAndCaptureAutoInbound(orderId, PLANNED_QTY);

        // 断言：自动入库产出的入库单与手工入库同形，且带作业工单号快照
        assertNotNull(autoInOrderId, "完工带动入库必须真的建出单据");
        WmsInOrderDO inOrder = inOrderMapper.selectById(autoInOrderId);
        assertEquals(WmsInBizTypeEnum.PROD_FINISH_IN.name(), inOrder.getBizType());
        assertEquals(prodOrderMapper.selectById(orderId).getOrderNo(), inOrder.getProdOrderNo());
        assertEquals(workOrdersOf(orderId).get(0).getWorkOrderNo(), inOrder.getWorkOrderNo(),
                "作业工单号是追溯快照，取带动完工的那一段");
        assertEquals(0, PLANNED_QTY.compareTo(onhandQtyOf(line.fgMatId(), stock.whId())));
        // 自动入库与手工入库共用同一套账本：流水的 biz_no 记 WMS 入库单编号，能反查回这张单
        WipTxnDO inboundTxn = wipTxnOf(orderId, MesWipTxnTypeEnum.FINISH_INBOUND);
        assertNotNull(inboundTxn);
        assertEquals(String.valueOf(autoInOrderId), inboundTxn.getBizNo(),
                "流水要与 WMS 入库单对上，否则事后查不出这批成品是哪张单进来的");
        assertEquals("IN_ORDER", inboundTxn.getBizType(), "完工入库的来源业务是 WMS 入库单");
    }

    @Test
    void testCloseOrder_ReleasesReservationBackflushesAndCompletesDemand() {
        // 准备：带需求、带倒冲件的工单，完工 100。倒冲件要能反冲，必须配好它的默认仓库与库位
        // （倒冲件从来不预留，没有"预留占住的库位"可以回落，只能走物料默认值）
        setDefaultStockOfFg(line.componentMatIds().get(1), stock.whId(), stock.locId());
        stockInBackflushMaterial(new BigDecimal("500"));
        Long demandId = mesFixture.createProdDemand(line);
        Long orderId = completeOrder(PLANNED_QTY, demandId);

        // 断言：需求初始停在 DRAFT（fixture 建的草稿需求）
        assertEquals("DRAFT", demandStatusOf(demandId));

        // 调用：关闭
        MesCloseOrderBO bo = new MesCloseOrderBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(prodOrderMapper.selectById(orderId).getOrderNo());
        bo.setProdOrderId(orderId);
        bo.setWhId(stock.whId());
        bo.setLocId(stock.locId());
        prodOrderDomainService.closeProdOrder(bo);

        // 断言 1：状态推到 CLOSED
        assertEquals(MesProdOrderStatusEnum.CLOSED.name(), prodOrderMapper.selectById(orderId).getStatus());

        // 断言 2：倒冲件产生出库过账，数量按实际产量算（BOM 上倒冲件单位用量 1，无损耗 → 100）
        BigDecimal backflushQty = backflushIssuedQtyOf(line.componentMatIds().get(1));
        assertEquals(0, PLANNED_QTY.compareTo(backflushQty),
                "倒冲件按整单实际产量一次扣，避免多次小额过账把库存流水刷爆");

        // 断言 3：需求状态回写为 DONE——需求下全部工单收尾时收尾需求
        assertEquals("DONE", demandStatusOf(demandId));
    }

    @Test
    void testCloseOrder_RejectsNonDoneOrder() {
        // 准备：工单只下达不完工，停在 RUNNING 之前
        Long orderId = releaseSingleOpOrder();

        // 调用 + 断言：非 DONE 状态关闭抛错，且一笔预留都不释放、需求不回写
        MesCloseOrderBO bo = new MesCloseOrderBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(prodOrderMapper.selectById(orderId).getOrderNo());
        bo.setProdOrderId(orderId);
        ServiceException notDone = assertThrows(ServiceException.class,
                () -> prodOrderDomainService.closeProdOrder(bo));
        assertEquals(ErrorCodeConstants.PROD_ORDER_STATUS_INVALID.getCode(), notDone.getCode(),
                "状态机里只有 DONE 有 close 出边，其他状态必须拦下");
        assertEquals(MesProdOrderStatusEnum.RELEASED.name(), prodOrderMapper.selectById(orderId).getStatus(),
                "被拦下的关闭不推进状态");
    }

    @Test
    void testCloseOrder_ReleasesSubmittedReservationAndRestoresAvailableQty() {
        // 准备：一张已完工的工单，外加一张"提交了预留但没确认也没取消"的领料申请。
        // 倒冲件也要配默认库位，否则关闭会先卡在反冲那一步
        setDefaultStockOfFg(line.componentMatIds().get(1), stock.whId(), stock.locId());
        stockInBackflushMaterial(new BigDecimal("500"));
        Long orderId = completeOrder(PLANNED_QTY);
        Long reqId = submitMaterialReq(orderId);
        BigDecimal reservedBefore = reservedQtyOf(line.anyComponentMatId());
        BigDecimal onhandBefore = onhandQtyOf(line.anyComponentMatId(), stock.whId());
        assertTrue(reservedBefore.signum() > 0, "提交后必须真的有预留在占可用量，否则这条断言没有意义");

        // 调用：关闭（关闭这一支处理的是停在 SUBMITTED 的申请）
        MesCloseOrderBO bo = new MesCloseOrderBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(prodOrderMapper.selectById(orderId).getOrderNo());
        bo.setProdOrderId(orderId);
        bo.setWhId(stock.whId());
        bo.setLocId(stock.locId());
        prodOrderDomainService.closeProdOrder(bo);

        // 断言 1（验收点名）：剩余预留状态为 RELEASED、可用量回升
        assertEquals(0, BigDecimal.ZERO.compareTo(reservedQtyOf(line.anyComponentMatId())),
                "不释放预留，那部分可用量会被一张已收尾的工单永久占着");
        assertEquals(0, onhandBefore.compareTo(onhandQtyOf(line.anyComponentMatId(), stock.whId())),
                "释放预留只还可用量，不动现存量——料一件都没搬出去");
        assertTrue(reqId > 0);
    }

    /* ------------------------------------------------------------------ 建数据 ------------------------------------------------------------------ */

    /** 完工到 DONE 并返回自动入库单编号；不关心自动入库结果时用 {@link #completeOrder}。 */
    private Long completeOrderAndCaptureAutoInbound(Long orderId, BigDecimal qty) {
        OpTaskDO task = tasksBySeq(orderId).get(0);
        startTask(task);
        report(task, qty, BigDecimal.ZERO);
        finish(task);
        return autoInboundInOrderIdOf(orderId);
    }

    /** 一路做到完工（无需求关联），返回工单编号。 */
    private Long completeOrder(BigDecimal qty) {
        return completeOrder(qty, MesConstants.NONE_ID);
    }

    /** 一路做到完工（可关联需求），返回工单编号。 */
    private Long completeOrder(BigDecimal qty, Long demandId) {
        Long orderId = releaseSingleOpOrder(demandId);
        completeOrderAndCaptureAutoInbound(orderId, qty);
        return orderId;
    }

    /** 建一条单工序的生效路线并建单下达，返回生产工单编号。 */
    private Long releaseSingleOpOrder() {
        return releaseSingleOpOrder(MesConstants.NONE_ID);
    }

    /**
     * 建一条单工序的生效路线并建单下达，返回生产工单编号。
     *
     * <p>单工序是为了让"末道工序"没有歧义：完工入库与工单关闭都锚在末道工序上，
     * 多道工序的末道口径已由 {@code ProdReportIntegrationTest} 覆盖。
     */
    private Long releaseSingleOpOrder(Long demandId) {
        Long routeId = createSingleOpRoute();
        ProdOrderSaveReqVO reqVO = new ProdOrderSaveReqVO();
        reqVO.setPlantId(line.plantId());
        reqVO.setWorkshopId(line.workshopId());
        reqVO.setProjectId(MesConstants.NONE_ID);
        reqVO.setProjectTaskId(MesConstants.NONE_ID);
        reqVO.setMatId(line.fgMatId());
        reqVO.setQty(PLANNED_QTY);
        reqVO.setUomId(line.uomId());
        reqVO.setBomId(line.bomId());
        reqVO.setRouteId(routeId);
        reqVO.setDemandId(demandId);
        reqVO.setDueDate(LocalDateTime.of(2026, 12, 31, 18, 0));
        reqVO.setPriority(0);
        Long orderId = prodOrderService.createProdOrder(reqVO);

        MesReleaseOrderBO release = new MesReleaseOrderBO();
        release.setProdOrderId(orderId);
        release.setSourceModule(SOURCE_MODULE);
        release.setSourceBizNo(prodOrderMapper.selectById(orderId).getOrderNo());
        release.setIdempotencyKey(nextKey("RELEASE_PROD_ORDER"));
        prodOrderDomainService.releaseProdOrder(release);
        return orderId;
    }

    /** 建一条只有 1 道工序的生效路线（工序复用试点首道，用料不要求开工前必发）。 */
    private Long createSingleOpRoute() {
        RouteSaveReqVO route = new RouteSaveReqVO();
        route.setMatId(line.fgMatId());
        route.setVersion("V-T604-IN");
        route.setStatus(CommonStatusEnum.DISABLE.getStatus());
        route.setEffectiveDate(EFFECTIVE_DATE);
        Long routeId = routeService.createRoute(route);
        RouteOpSaveReqVO routeOp = new RouteOpSaveReqVO();
        routeOp.setRouteId(routeId);
        routeOp.setOpId(line.opIds().get(0));
        routeOp.setWcId(line.wcId());
        routeOp.setKeyFlag(true);
        routeOp.setFaiFlag(false);
        routeOp.setSkipPolicy("NOT_ALLOWED");
        routeService.createRouteOp(routeOp);
        route.setId(routeId);
        route.setStatus(CommonStatusEnum.ENABLE.getStatus());
        routeService.updateRoute(route);
        return routeId;
    }

    /**
     * 给倒冲件反冲备料：经 {@code InOrderApi} 真实过账补一批库存。
     *
     * <p>不补的话关闭时反冲出库会因账上无量而失败——倒冲件在齐套与领料里都被剔除，
     * 它的库存只能由采购入库这条真实路径备出来。快照列（{@code uom_code} 等）是调用方责任，显式带上。
     */
    private void stockInBackflushMaterial(BigDecimal qty) {
        CreateInOrderReqDTO req = new CreateInOrderReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-04-BACKFLUSH-STOCK");
        req.setIdempotencyKey("INTEGRATION:T6-04-BACKFLUSH-STOCK:CREATE_IN");
        req.setBizType("PO_IN");
        req.setInvOrgId(line.invOrgId());
        req.setPlantId(line.plantId());
        req.setWhId(stock.whId());
        req.setQualityStatus("QUALIFIED");
        CreateInOrderReqDTO.Line inLine = new CreateInOrderReqDTO.Line();
        inLine.setLineNo(10);
        inLine.setMatId(line.componentMatIds().get(1));
        inLine.setUomId(line.uomId());
        inLine.setUomCode(MdmFixture.UOM_CODE);
        inLine.setLocId(stock.locId());
        inLine.setPlanQty(qty);
        req.setLines(List.of(inLine));
        Long inOrderId = inOrderApi.createInOrder(req);

        ConfirmOrderReqDTO confirm = new ConfirmOrderReqDTO();
        confirm.setSourceModule(SOURCE_MODULE);
        confirm.setSourceBizNo("T6-04-BACKFLUSH-STOCK");
        confirm.setIdempotencyKey("INTEGRATION:T6-04-BACKFLUSH-STOCK:CONFIRM_IN:" + inOrderId);
        confirm.setOrderId(inOrderId);
        inOrderApi.confirmInOrder(confirm);
    }

    /** 给物料配置默认仓库与库位（完工入库与倒冲反冲的回落目标）。 */
    private void setDefaultStockOfFg(Long matId, Long whId, Long locId) {
        MatRespVO mat = matService.getMat(matId);
        MatSaveReqVO update = new MatSaveReqVO();
        update.setId(mat.getId());
        update.setCatId(mat.getCatId());
        update.setBaseUomId(mat.getBaseUomId());
        update.setName(mat.getName());
        update.setMatType(mat.getMatType());
        update.setPlanStrategy(mat.getPlanStrategy());
        update.setStatus(mat.getStatus());
        update.setBatchFlag(mat.getBatchFlag());
        update.setSnFlag(mat.getSnFlag());
        update.setDefaultWhId(whId);
        update.setDefaultLocId(locId);
        matService.updateMat(update);
    }

    /** 经真实开工路径把工序推到 {@code RUNNING}。 */
    private void startTask(OpTaskDO task) {
        // 首道工序下达后停在 PENDING（还没排计划），要先经 APS 回写排上资源才转 READY 可开工
        scheduleTask(task);
        MesOpTaskActionBO bo = new MesOpTaskActionBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-04-IN-START");
        bo.setIdempotencyKey(nextKey("START_OP_TASK"));
        bo.setOpTaskId(task.getId());
        opTaskDomainService.startOpTask(bo);
    }

    /** 经 APS 回写门面给工序任务排资源：写 res_id 并把 {@code PENDING} 推进到 {@code READY}。 */
    private void scheduleTask(OpTaskDO task) {
        ensureResourceCalendar();
        ApplyScheduleReqDTO req = new ApplyScheduleReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-04-IN-SCHEDULE");
        req.setIdempotencyKey(nextKey("APPLY_SCHEDULE"));
        req.setVersionNo("T6-04-IN-V" + KEY_SEQ.incrementAndGet());
        ApplyScheduleReqDTO.Item item = new ApplyScheduleReqDTO.Item();
        item.setOpTaskId(task.getId());
        item.setResId(line.resId());
        item.setPlannedStartTime(LocalDate.now().atTime(8, 0));
        item.setPlannedEndTime(LocalDate.now().atTime(12, 0));
        req.setItems(List.of(item));
        opTaskApi.applySchedule(req);
    }

    /** 资源当天有排班（试点 fixture 只给 2020-01-01 建了排班）。 */
    private void ensureResourceCalendar() {
        ResCalendarBatchSaveReqVO batch = new ResCalendarBatchSaveReqVO();
        batch.setResIds(List.of(line.resId()));
        batch.setDateFrom(LocalDate.now());
        batch.setDateTo(LocalDate.now());
        batch.setShiftId(line.shiftId());
        batch.setAvailableMinutes(720);
        resCalendarService.saveResCalendarBatch(batch);
    }

    private void report(OpTaskDO task, BigDecimal qtyGood, BigDecimal qtyScrap) {
        MesSubmitReportBO bo = new MesSubmitReportBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-04-IN-REPORT");
        bo.setIdempotencyKey(SOURCE_MODULE + ":" + task.getTaskNo() + ":REPORT:1");
        bo.setOpTaskId(task.getId());
        bo.setQtyGood(qtyGood);
        bo.setQtyScrap(qtyScrap);
        bo.setQtyRework(BigDecimal.ZERO);
        bo.setWorkerId(MesConstants.NONE_ID);
        bo.setResId(MesConstants.NONE_ID);
        bo.setShiftId(MesConstants.NONE_ID);
        LocalDateTime now = LocalDateTime.now();
        bo.setStartTime(now.minusHours(1));
        bo.setEndTime(now);
        reportDomainService.submitReport(bo);
    }

    private void finish(OpTaskDO task) {
        MesOpTaskFinishBO bo = new MesOpTaskFinishBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo("T6-04-IN-FINISH");
        bo.setIdempotencyKey(nextKey("FINISH_OP_TASK"));
        bo.setOpTaskId(task.getId());
        opTaskDomainService.finishOpTask(bo);
    }

    private MesFinishInboundBO inboundBO(Long orderId, BigDecimal qty) {
        MesFinishInboundBO bo = new MesFinishInboundBO();
        bo.setSourceModule(SOURCE_MODULE);
        bo.setSourceBizNo(prodOrderMapper.selectById(orderId).getOrderNo());
        bo.setIdempotencyKey(nextKey("FINISH_INBOUND"));
        bo.setProdOrderId(orderId);
        bo.setQty(qty);
        bo.setRemark("T6-04 完工入库");
        return bo;
    }

    /**
     * 提交一张领料申请并停在 {@code SUBMITTED}（预留已建、未确认也未取消）。
     *
     * <p>这正是关闭那一步要处理的单据状态：料没搬出去，可用量却被占着。
     */
    private Long submitMaterialReq(Long orderId) {
        CreateMaterialReqReqDTO req = new CreateMaterialReqReqDTO();
        req.setSourceModule(SOURCE_MODULE);
        req.setSourceBizNo("T6-04-IN-ISSUE");
        req.setIdempotencyKey(nextKey("CREATE_MATERIAL_REQ"));
        req.setProdOrderId(orderId);
        req.setWorkOrderId(workOrdersOf(orderId).get(0).getId());
        req.setOpTaskId(tasksBySeq(orderId).get(0).getId());
        req.setReqType(MesMaterialReqTypeEnum.ISSUE.name());
        req.setInvOrgId(line.invOrgId());
        req.setWhId(stock.whId());
        CreateMaterialReqReqDTO.Line reqLine = new CreateMaterialReqReqDTO.Line();
        reqLine.setMatId(line.anyComponentMatId());
        reqLine.setQty(new BigDecimal("100"));
        reqLine.setUomId(line.uomId());
        req.setLines(List.of(reqLine));
        Long reqId = materialReqApi.createMaterialReq(req);

        MesMaterialReqActionBO action = new MesMaterialReqActionBO();
        action.setSourceModule(SOURCE_MODULE);
        action.setSourceBizNo("T6-04-IN-SUBMIT");
        action.setIdempotencyKey(nextKey("SUBMIT_MATERIAL_REQ"));
        action.setMaterialReqId(reqId);
        materialReqDomainService.submitMaterialReq(action);
        return reqId;
    }

    /* ------------------------------------------------------------------ 取数与断言辅助 ------------------------------------------------------------------ */

    private List<WorkOrderDO> workOrdersOf(Long prodOrderId) {
        return workOrderMapper.selectListByProdOrderIds(List.of(prodOrderId));
    }

    private List<OpTaskDO> tasksBySeq(Long prodOrderId) {
        List<Long> workOrderIds = workOrdersOf(prodOrderId).stream().map(WorkOrderDO::getId).toList();
        return opTaskMapper.selectListByWorkOrderIds(workOrderIds).stream()
                .sorted(Comparator.comparing(OpTaskDO::getSeq).thenComparing(OpTaskDO::getId)).toList();
    }

    /** 一张工单的完工入库流水。 */
    private List<WipTxnDO> inboundTxnsOf(Long prodOrderId) {
        return wipTxnMapper.selectList(WipTxnDO::getProdOrderId, prodOrderId,
                WipTxnDO::getTxnType, MesWipTxnTypeEnum.FINISH_INBOUND.name());
    }

    private WipTxnDO wipTxnOf(Long prodOrderId, MesWipTxnTypeEnum txnType) {
        return wipTxnMapper.selectList(WipTxnDO::getProdOrderId, prodOrderId,
                WipTxnDO::getTxnType, txnType.name()).stream().findFirst().orElse(null);
    }

    /**
     * 自动入库产生的 WMS 入库单编号。
     *
     * <p>MES 侧的完工入库流水把 {@code biz_no} 记成 WMS 入库单编号（两个门面都没有按编号查单号的
     * 只读方法），因此从流水反查。
     */
    private Long autoInboundInOrderIdOf(Long prodOrderId) {
        WipTxnDO txn = wipTxnOf(prodOrderId, MesWipTxnTypeEnum.FINISH_INBOUND);
        if (txn == null || txn.getBizNo() == null) {
            return null;
        }
        return Long.valueOf(txn.getBizNo());
    }

    private BigDecimal onhandQtyOf(Long matId, Long whId) {
        InventoryQueryReqDTO query = new InventoryQueryReqDTO();
        query.setMatId(matId);
        query.setWhId(whId);
        return inventoryApi.getInventoryBalanceList(query).stream()
                .map(InventoryBalanceRespDTO::getQtyOnhand)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal reservedQtyOf(Long matId) {
        InventoryQueryReqDTO query = new InventoryQueryReqDTO();
        query.setMatId(matId);
        return inventoryApi.getInventoryBalanceList(query).stream()
                .map(InventoryBalanceRespDTO::getQtyReserved)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** 某个倒冲件被反冲出库的数量（出库过账后余额减少，用负的库存流水求和）。 */
    private BigDecimal backflushIssuedQtyOf(Long matId) {
        com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnQueryReqDTO query =
                new com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnQueryReqDTO();
        query.setMatId(matId);
        return inventoryApi.getInventoryTxnList(query).stream()
                .map(com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnRespDTO::getQty)
                .filter(java.util.Objects::nonNull)
                .filter(qty -> qty.signum() < 0)
                .map(BigDecimal::negate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String demandStatusOf(Long demandId) {
        return demandMapper.selectById(demandId).getStatus();
    }

    private String nextKey(String action) {
        return SOURCE_MODULE + ":T6-04:IN:" + KEY_SEQ.incrementAndGet() + ":" + action;
    }

}
