package com.iboot.pf.module.mes.service.proddemand;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.dal.dataobject.command.MesCommandRecordDO;
import com.iboot.pf.module.mes.dal.dataobject.proddemand.ProdDemandDO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.dal.mysql.command.MesCommandRecordMapper;
import com.iboot.pf.module.mes.dal.mysql.proddemand.ProdDemandMapper;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
import com.iboot.pf.module.mes.enums.MesCommandTypeEnum;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesDemandSourceTypeEnum;
import com.iboot.pf.module.mes.enums.MesDemandStatusEnum;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.enums.MesProdOrderStatusEnum;
import com.iboot.pf.module.mes.service.coderule.MesCodeService;
import com.iboot.pf.module.mes.service.command.MesCommandServiceImpl;
import com.iboot.pf.module.mes.service.proddemand.bo.MesConfirmDemandBO;
import com.iboot.pf.module.mes.service.proddemand.bo.MesDemandToOrderBO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_DEMAND_STATUS_INVALID;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.PROD_DEMAND_TO_ORDER_QTY_EXCEEDED;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.QTY_MUST_BE_POSITIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 生产需求领域动作测试：确认、取消、转工单三个动作各自的正常流转、非法状态拦截与幂等，
 * 以及分批转单下"未转完保持原状态、转满才置已下达"的口径。
 *
 * <p>命令幂等服务用真实实现而不是 mock：幂等是这三个动作的核心行为，mock 掉等于没测。
 */
@Import({MesProdDemandDomainServiceImpl.class, MesCommandServiceImpl.class})
class MesProdDemandDomainServiceImplTest extends BaseDbUnitTest {

    /** 幂等键第一段：界面发起。 */
    private static final String SOURCE_MODULE = "UI";
    private static final Long PLANT_ID = 11L;
    private static final Long WORKSHOP_ID = 21L;
    private static final Long PROJECT_ID = 31L;
    private static final Long PROJECT_TASK_ID = 41L;
    private static final Long MAT_ID = 51L;
    private static final Long UOM_ID = 61L;
    private static final LocalDateTime DUE_DATE = LocalDateTime.of(2026, 10, 1, 8, 0, 0);
    private static final Integer PRIORITY = 7;

    @Resource
    private MesProdDemandDomainService domainService;
    @Resource
    private ProdDemandMapper prodDemandMapper;
    @Resource
    private ProdOrderMapper prodOrderMapper;
    @Resource
    private MesCommandRecordMapper commandRecordMapper;

    @MockitoBean
    private MesCodeService codeService;

    @BeforeEach
    void mockCodeRule() {
        // 编码规则在 infra，单测里按调用次数发号，用来验证工单号确实由编码规则产出而非调用方拼串
        AtomicInteger seq = new AtomicInteger();
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) != null
                        ? invocation.getArgument(1)
                        : String.format("PO%04d", seq.incrementAndGet()));
    }

    @Test
    void testConfirmDemand_draftToConfirmed() {
        // 准备：新建状态的需求，幂等键留空交给服务端按四段格式派生
        ProdDemandDO demand = createDemand("D-0001", MesDemandStatusEnum.DRAFT, "100");

        // 调用
        domainService.confirmDemand(confirmBO(demand.getId(), null, "计划确认投产"));

        // 断言：状态推进到已确认，说明落到备注列
        ProdDemandDO updated = prodDemandMapper.selectById(demand.getId());
        assertEquals(MesDemandStatusEnum.CONFIRMED.name(), updated.getStatus());
        assertEquals("计划确认投产", updated.getRemark());
        // 断言：幂等键按 {来源模块}:{需求编号}:{命令类型}:{行号} 四段派生，命令类型段取枚举名
        MesCommandRecordDO record = commandRecordMapper
                .selectByIdempotencyKey("UI:D-0001:" + MesCommandTypeEnum.CONFIRM_DEMAND.name() + ":0");
        assertNotNull(record);
        assertEquals(MesCommandTypeEnum.CONFIRM_DEMAND.name(), record.getCommandType());
    }

    @Test
    void testConfirmDemand_statusInvalid() {
        // 准备：已确认的需求不能再确认，否则已排产、已下达的需求都能被退回重走一遍
        ProdDemandDO demand = createDemand("D-0002", MesDemandStatusEnum.CONFIRMED, "100");

        // 调用与断言
        assertServiceException(() -> domainService.confirmDemand(confirmBO(demand.getId(), null, null)),
                PROD_DEMAND_STATUS_INVALID);
    }

    @Test
    void testConfirmDemand_idempotent() {
        // 准备：新建状态的需求，同一幂等键连点两次
        ProdDemandDO demand = createDemand("D-0003", MesDemandStatusEnum.DRAFT, "100");
        String key = "UI:D-0003:" + MesCommandTypeEnum.CONFIRM_DEMAND.name() + ":0";

        // 调用：第二次若没被幂等拦住，会因为状态已不是新建而抛非法状态
        domainService.confirmDemand(confirmBO(demand.getId(), key, "首次确认"));
        domainService.confirmDemand(confirmBO(demand.getId(), key, "重复确认"));

        // 断言：状态与备注都停在首次结果，命令记录只一条
        ProdDemandDO updated = prodDemandMapper.selectById(demand.getId());
        assertEquals(MesDemandStatusEnum.CONFIRMED.name(), updated.getStatus());
        assertEquals("首次确认", updated.getRemark());
        assertEquals(1L, commandRecordMapper.selectCount(MesCommandRecordDO::getIdempotencyKey, key));
    }

    @Test
    void testCancelDemand_nonFinalToCancelled() {
        // 准备：已确认的需求可以取消
        ProdDemandDO demand = createDemand("D-0004", MesDemandStatusEnum.CONFIRMED, "100");

        // 调用
        domainService.cancelDemand(cancelBO(demand.getId(), null, "客户撤单"));

        // 断言：状态到已取消，取消原因落备注；幂等键的命令段取取消枚举名
        ProdDemandDO updated = prodDemandMapper.selectById(demand.getId());
        assertEquals(MesDemandStatusEnum.CANCELLED.name(), updated.getStatus());
        assertEquals("客户撤单", updated.getRemark());
        assertNotNull(commandRecordMapper
                .selectByIdempotencyKey("UI:D-0004:" + MesCommandTypeEnum.CANCEL_DEMAND.name() + ":0"));
    }

    @Test
    void testCancelDemand_finalStatusInvalid() {
        // 准备：已完成是终态，账已随工单关闭收尾，不能再取消
        ProdDemandDO demand = createDemand("D-0005", MesDemandStatusEnum.DONE, "100");

        // 调用与断言
        assertServiceException(() -> domainService.cancelDemand(cancelBO(demand.getId(), null, "误操作")),
                PROD_DEMAND_STATUS_INVALID);
    }

    @Test
    void testCancelDemand_idempotent() {
        // 准备：同一幂等键取消两次
        ProdDemandDO demand = createDemand("D-0006", MesDemandStatusEnum.PLANNED, "100");
        String key = "UI:D-0006:" + MesCommandTypeEnum.CANCEL_DEMAND.name() + ":0";

        // 调用：第二次若没被幂等拦住，会因为已经是终态而抛非法状态
        domainService.cancelDemand(cancelBO(demand.getId(), key, "首次取消"));
        domainService.cancelDemand(cancelBO(demand.getId(), key, "重复取消"));

        // 断言
        ProdDemandDO updated = prodDemandMapper.selectById(demand.getId());
        assertEquals(MesDemandStatusEnum.CANCELLED.name(), updated.getStatus());
        assertEquals("首次取消", updated.getRemark());
        assertEquals(1L, commandRecordMapper.selectCount(MesCommandRecordDO::getIdempotencyKey, key));
    }

    @Test
    void testDemandToProdOrder_fullConvertSnapshot() {
        // 准备：已确认的需求，一次性全量转单
        ProdDemandDO demand = createDemand("D-0007", MesDemandStatusEnum.CONFIRMED, "100");

        // 调用
        Long prodOrderId = domainService.demandToProdOrder(toOrderBO(demand.getId(), "100", null));

        // 断言：需求转满置已下达
        assertNotNull(prodOrderId);
        assertEquals(MesDemandStatusEnum.RELEASED.name(),
                prodDemandMapper.selectById(demand.getId()).getStatus());

        // 断言：工单号来自编码规则，需求编号指回需求
        ProdOrderDO order = prodOrderMapper.selectById(prodOrderId);
        assertEquals("PO0001", order.getOrderNo());
        assertEquals(demand.getId(), order.getDemandId());

        // 断言：物料、数量、交期、优先级、工厂、项目字段逐个快照需求
        assertEquals(PLANT_ID, order.getPlantId());
        assertEquals(PROJECT_ID, order.getProjectId());
        assertEquals(PROJECT_TASK_ID, order.getProjectTaskId());
        assertEquals(MAT_ID, order.getMatId());
        assertEquals("M-001", order.getMatCode());
        assertEquals("电机总成", order.getMatName());
        assertEquals("380V 15kW", order.getMatSpec());
        assertEquals(UOM_ID, order.getUomId());
        assertEquals("PCS", order.getUomCode());
        assertEquals(DUE_DATE, order.getDueDate());
        assertEquals(PRIORITY, order.getPriority());
        assertEquals(0, new BigDecimal("100").compareTo(order.getQty()));
        assertEquals(WORKSHOP_ID, order.getWorkshopId());

        // 断言：新建工单落 DRAFT，进度归零，齐套未检查，四个版本快照留给下达动作锁
        assertEquals(MesProdOrderStatusEnum.DRAFT.name(), order.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(order.getCompletedQty()));
        assertEquals(0, BigDecimal.ZERO.compareTo(order.getScrapQty()));
        assertEquals(MesKitStatusEnum.UNCHECKED.name(), order.getKitStatus());
        assertEquals(MesConstants.NONE_ID, order.getBomId());
        assertEquals(MesConstants.NONE_ID, order.getRouteId());
        assertEquals("", order.getBomVersion());
        assertEquals("", order.getRouteVersion());
        assertEquals(MesConstants.INIT_VERSION, order.getVersion());
    }

    @Test
    void testDemandToProdOrder_statusInvalid() {
        // 准备：未确认的需求不能转工单
        ProdDemandDO demand = createDemand("D-0008", MesDemandStatusEnum.DRAFT, "100");

        // 调用与断言
        assertServiceException(() -> domainService.demandToProdOrder(toOrderBO(demand.getId(), "100", null)),
                PROD_DEMAND_STATUS_INVALID);
    }

    @Test
    void testDemandToProdOrder_idempotent() {
        // 准备：同一幂等键转两次。转单是有返回值的命令，幂等命中要返回首次创建的那张工单
        ProdDemandDO demand = createDemand("D-0009", MesDemandStatusEnum.CONFIRMED, "100");
        String key = "UI:D-0009:" + MesCommandTypeEnum.DEMAND_TO_ORDER.name() + ":0";

        // 调用
        Long first = domainService.demandToProdOrder(toOrderBO(demand.getId(), "100", key));
        Long repeated = domainService.demandToProdOrder(toOrderBO(demand.getId(), "100", key));

        // 断言：返回同一张工单，工单表只一条，累计转单量没有被翻倍
        assertEquals(first, repeated);
        assertEquals(1L, prodOrderMapper.selectCountByDemandId(demand.getId()));
        assertEquals(1L, commandRecordMapper.selectCount(MesCommandRecordDO::getIdempotencyKey, key));
        assertEquals(MesDemandStatusEnum.RELEASED.name(),
                prodDemandMapper.selectById(demand.getId()).getStatus());
    }

    @Test
    void testDemandToProdOrder_partialThenFull() {
        // 准备：需求 100，分两批投产。这是部分转单的验收场景
        ProdDemandDO demand = createDemand("D-0010", MesDemandStatusEnum.CONFIRMED, "100");

        // 调用：第一批 40
        Long firstOrderId = domainService.demandToProdOrder(toOrderBO(demand.getId(), "40",
                "UI:D-0010:" + MesCommandTypeEnum.DEMAND_TO_ORDER.name() + ":0"));

        // 断言：没转完，需求保持已确认，累计转单量 40
        assertEquals(MesDemandStatusEnum.CONFIRMED.name(),
                prodDemandMapper.selectById(demand.getId()).getStatus());
        assertEquals(1L, prodOrderMapper.selectCountByDemandId(demand.getId()));
        assertEquals(0, new BigDecimal("40").compareTo(sumOrderQty(demand.getId())));
        assertEquals(0, new BigDecimal("40").compareTo(prodOrderMapper.selectById(firstOrderId).getQty()));

        // 调用：第二批 60，转满
        Long secondOrderId = domainService.demandToProdOrder(toOrderBO(demand.getId(), "60",
                "UI:D-0010:" + MesCommandTypeEnum.DEMAND_TO_ORDER.name() + ":1"));

        // 断言：转满置已下达，两张工单都指回同一需求，累计等于需求量，工单号各自来自编码规则
        assertNotEquals(firstOrderId, secondOrderId);
        assertEquals(MesDemandStatusEnum.RELEASED.name(),
                prodDemandMapper.selectById(demand.getId()).getStatus());
        assertEquals(2L, prodOrderMapper.selectCountByDemandId(demand.getId()));
        assertEquals(0, new BigDecimal("100").compareTo(sumOrderQty(demand.getId())));
        ProdOrderDO second = prodOrderMapper.selectById(secondOrderId);
        assertEquals(0, new BigDecimal("60").compareTo(second.getQty()));
        assertEquals(demand.getId(), second.getDemandId());
        assertEquals("PO0002", second.getOrderNo());
        assertEquals(MesProdOrderStatusEnum.DRAFT.name(), second.getStatus());
    }

    @Test
    void testDemandToProdOrder_partialKeepsPlanned() {
        // 准备：已排产的需求部分转单。未转完时要保持已排产，不能退回已确认，
        // 否则 APS 会以为这批需求还没被纳入排程，下一轮又排一次
        ProdDemandDO demand = createDemand("D-0011", MesDemandStatusEnum.PLANNED, "100");

        // 调用
        domainService.demandToProdOrder(toOrderBO(demand.getId(), "30", null));

        // 断言
        assertEquals(MesDemandStatusEnum.PLANNED.name(),
                prodDemandMapper.selectById(demand.getId()).getStatus());
        assertEquals(1L, prodOrderMapper.selectCountByDemandId(demand.getId()));
    }

    @Test
    void testDemandToProdOrder_qtyExceeded() {
        // 准备：需求 100，已转 40，再转 70 就超了
        ProdDemandDO demand = createDemand("D-0012", MesDemandStatusEnum.CONFIRMED, "100");
        domainService.demandToProdOrder(toOrderBO(demand.getId(), "40", null));

        // 调用与断言：超量被拦，不产生第二张工单
        assertServiceException(() -> domainService.demandToProdOrder(toOrderBO(demand.getId(), "70",
                "UI:D-0012:" + MesCommandTypeEnum.DEMAND_TO_ORDER.name() + ":1")),
                PROD_DEMAND_TO_ORDER_QTY_EXCEEDED);
        assertEquals(1L, prodOrderMapper.selectCountByDemandId(demand.getId()));
    }

    @Test
    void testDemandToProdOrder_qtyMustBePositive() {
        // 准备：转单量为零
        ProdDemandDO demand = createDemand("D-0013", MesDemandStatusEnum.CONFIRMED, "100");

        // 调用与断言
        assertServiceException(() -> domainService.demandToProdOrder(toOrderBO(demand.getId(), "0", null)),
                QTY_MUST_BE_POSITIVE);
    }

    @Test
    void testDemandToProdOrder_cancelledOrderNotCounted() {
        // 准备：需求 100 先转 60，随后这张工单被取消——那批量没有真的投产，不能占着累计转单量
        ProdDemandDO demand = createDemand("D-0014", MesDemandStatusEnum.CONFIRMED, "100");
        Long cancelledOrderId = domainService.demandToProdOrder(toOrderBO(demand.getId(), "60", null));
        prodOrderMapper.updateById(new ProdOrderDO().setId(cancelledOrderId)
                .setStatus(MesProdOrderStatusEnum.CANCELLED.name()));

        // 调用：已取消的 60 不计入，因此还能整量转 100
        domainService.demandToProdOrder(toOrderBO(demand.getId(), "100",
                "UI:D-0014:" + MesCommandTypeEnum.DEMAND_TO_ORDER.name() + ":1"));

        // 断言：需求转满置已下达
        assertEquals(MesDemandStatusEnum.RELEASED.name(),
                prodDemandMapper.selectById(demand.getId()).getStatus());
        assertEquals(2L, prodOrderMapper.selectCountByDemandId(demand.getId()));
    }

    /** 建一条需求。字段给全，转单用例要逐个核对快照是否原样带到工单上。 */
    private ProdDemandDO createDemand(String demandNo, MesDemandStatusEnum status, String qty) {
        ProdDemandDO demand = new ProdDemandDO()
                .setDemandNo(demandNo)
                .setSourceType(MesDemandSourceTypeEnum.MANUAL.name())
                .setPlantId(PLANT_ID)
                .setProjectId(PROJECT_ID)
                .setProjectTaskId(PROJECT_TASK_ID)
                .setMatId(MAT_ID)
                .setMatCode("M-001")
                .setMatName("电机总成")
                .setMatSpec("380V 15kW")
                .setQty(new BigDecimal(qty))
                .setUomId(UOM_ID)
                .setUomCode("PCS")
                .setDueDate(DUE_DATE)
                .setPriority(PRIORITY)
                .setStatus(status.name());
        prodDemandMapper.insert(demand);
        return demand;
    }

    private MesConfirmDemandBO confirmBO(Long demandId, String idempotencyKey, String remark) {
        MesConfirmDemandBO bo = new MesConfirmDemandBO();
        bo.setDemandId(demandId);
        bo.setSourceModule(SOURCE_MODULE);
        bo.setIdempotencyKey(idempotencyKey);
        bo.setRemark(remark);
        return bo;
    }

    /** 取消与确认共用参数对象，这里只是让用例读起来更贴近动作语义。 */
    private MesConfirmDemandBO cancelBO(Long demandId, String idempotencyKey, String reason) {
        return confirmBO(demandId, idempotencyKey, reason);
    }

    private MesDemandToOrderBO toOrderBO(Long demandId, String qty, String idempotencyKey) {
        MesDemandToOrderBO bo = new MesDemandToOrderBO();
        bo.setDemandId(demandId);
        bo.setQty(new BigDecimal(qty));
        bo.setWorkshopId(WORKSHOP_ID);
        bo.setSourceModule(SOURCE_MODULE);
        bo.setIdempotencyKey(idempotencyKey);
        return bo;
    }

    /** 累计转单量：本需求下工单的计划数量之和，用来核对部分转单的进度。 */
    private BigDecimal sumOrderQty(Long demandId) {
        List<ProdOrderDO> orders = prodOrderMapper.selectList(ProdOrderDO::getDemandId, demandId);
        return orders.stream().map(ProdOrderDO::getQty).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
