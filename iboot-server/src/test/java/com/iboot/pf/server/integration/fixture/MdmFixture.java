package com.iboot.pf.server.integration.fixture;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.module.infra.api.coderule.CodeRuleApi;
import com.iboot.pf.module.mdm.controller.admin.bom.vo.BomSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.calendar.vo.CalendarRuleSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.calendar.vo.CalendarSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.invorg.vo.InvOrgSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.mat.vo.MatSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.matcat.vo.MatCatSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.op.vo.OpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.plant.vo.PlantSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.res.vo.ResSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.ResCalendarSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.rescapability.vo.ResCapabilitySaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteOpSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.routeopmat.vo.RouteOpMatSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.shift.vo.ShiftSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.uom.vo.UomSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.wc.vo.WcSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.workshop.vo.WorkshopSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;
import com.iboot.pf.module.mdm.service.bom.BomService;
import com.iboot.pf.module.mdm.service.calendar.CalendarService;
import com.iboot.pf.module.mdm.service.invorg.InvOrgService;
import com.iboot.pf.module.mdm.service.mat.MatService;
import com.iboot.pf.module.mdm.service.matcat.MatCatService;
import com.iboot.pf.module.mdm.service.op.OpService;
import com.iboot.pf.module.mdm.service.plant.PlantService;
import com.iboot.pf.module.mdm.service.res.ResService;
import com.iboot.pf.module.mdm.service.rescalendar.ResCalendarService;
import com.iboot.pf.module.mdm.service.rescapability.ResCapabilityService;
import com.iboot.pf.module.mdm.service.route.RouteService;
import com.iboot.pf.module.mdm.service.routeopmat.RouteOpMatService;
import com.iboot.pf.module.mdm.service.shift.ShiftService;
import com.iboot.pf.module.mdm.service.uom.UomService;
import com.iboot.pf.module.mdm.service.wc.WcService;
import com.iboot.pf.module.mdm.service.workshop.WorkshopService;
import com.iboot.pf.server.integration.PilotLine;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MDM 主数据工厂（T6-01）。
 *
 * 全部走各主数据的 create Service（与生产写入路径一致），不直接 Mapper insert。
 * 编码一律留空，由 base-data.sql 预置的编码规则经 {@code MdmCodeService} 自动生成。
 *
 * 覆盖一条试点产线的最小主数据集：
 * 库存组织 → 工厂 → 车间 → 工作中心；单位；物料分类；成品 + 3 子件
 * （三种 issue_method：PICK / BACKFLUSH / PHANTOM）；班次、日历、资源、
 * 资源能力、资源日历；3 道工序（其中 2 道并行）的工艺路线；路线工序用料；
 * BOM（含损耗率、虚拟件、倒冲件）。
 *
 * @author iboot
 */
@Component
public class MdmFixture {

    /** 带生效日期的版本号：写成过去日期，保证 {@code getEffectiveBom/Route} 命中。 */
    private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2020, 1, 1);

    /** 工序编码自增序号：mdm_op.code 必填且唯一，逐次建工序时用它保证不撞码。 */
    private static final AtomicInteger OP_SEQ = new AtomicInteger();

    /**
     * 试点产线统一使用的计量单位编码。
     *
     * <p>WMS 单据行（{@code wms_in_order_line} / {@code wms_inv_balance} 等）的
     * {@code uom_code} 是 NOT NULL 的<b>调用方快照字段</b>，Service 不会自动回填，
     * 故建 WMS 单据时要把这个编码显式带上（见 {@code WmsFixture}）。
     */
    public static final String UOM_CODE = "IT-PCS";

    @Resource
    private InvOrgService invOrgService;
    @Resource
    private PlantService plantService;
    @Resource
    private WorkshopService workshopService;
    @Resource
    private WcService wcService;
    @Resource
    private UomService uomService;
    @Resource
    private MatCatService matCatService;
    @Resource
    private MatService matService;
    @Resource
    private BomService bomService;
    @Resource
    private RouteService routeService;
    @Resource
    private RouteOpMatService routeOpMatService;
    @Resource
    private OpService opService;
    @Resource
    private ResService resService;
    @Resource
    private ResCapabilityService resCapabilityService;
    @Resource
    private ResCalendarService resCalendarService;
    @Resource
    private ShiftService shiftService;
    @Resource
    private CalendarService calendarService;
    @Resource
    private CodeRuleApi codeRuleApi;

    /** 编码规则流水号在事务回滚后不会回退，但 H2 库是每个测试类独立的，无此问题。 */
    public String nextCode(String ruleCode) {
        return codeRuleApi.generate(ruleCode);
    }

    /**
     * 建一条试点产线的全套最小主数据。
     *
     * @return 试点产线句柄，含全部主数据 ID
     */
    public PilotLine createPilotLine() {
        // 1. 组织与资源层级：库存组织 → 工厂 → 车间 → 工作中心
        InvOrgSaveReqVO invOrg = new InvOrgSaveReqVO();
        invOrg.setCode("IT-INVORG"); // mdm_inv_org.code 为 NOT NULL，必填
        invOrg.setName("集成测试库存组织");
        invOrg.setStatus(CommonStatusEnum.ENABLE.getStatus());
        Long invOrgId = invOrgService.createInvOrg(invOrg);

        PlantSaveReqVO plant = new PlantSaveReqVO();
        plant.setInvOrgId(invOrgId);
        plant.setName("集成测试工厂");
        plant.setStatus(CommonStatusEnum.ENABLE.getStatus());
        Long plantId = plantService.createPlant(plant);

        WorkshopSaveReqVO workshop = new WorkshopSaveReqVO();
        workshop.setPlantId(plantId);
        workshop.setName("集成测试车间");
        workshop.setStatus(CommonStatusEnum.ENABLE.getStatus());
        Long workshopId = workshopService.createWorkshop(workshop);

        WcSaveReqVO wc = new WcSaveReqVO();
        wc.setWorkshopId(workshopId);
        wc.setName("集成测试工作中心");
        wc.setStatus(CommonStatusEnum.ENABLE.getStatus());
        Long wcId = wcService.createWc(wc);

        // 2. 计量单位
        UomSaveReqVO uom = new UomSaveReqVO();
        uom.setCode(UOM_CODE); // mdm_uom.code 为 NOT NULL，必填
        uom.setName("件");
        uom.setStatus(CommonStatusEnum.ENABLE.getStatus());
        Long uomId = uomService.create(uom);

        // 3. 物料分类
        MatCatSaveReqVO matCat = new MatCatSaveReqVO();
        matCat.setParentId(0L); // 0 表示根节点
        matCat.setName("集成测试物料分类");
        matCat.setStatus(CommonStatusEnum.ENABLE.getStatus());
        Long matCatId = matCatService.createMatCat(matCat);

        // 4. 物料：1 个成品 + 3 个子件（A 领料 / B 倒冲 / C 虚拟件）
        Long fgMatId = createMat(matCatId, uomId, "试点成品", "FG");
        Long pickMatId = createMat(matCatId, uomId, "试点子件-领料", "RM");
        Long backflushMatId = createMat(matCatId, uomId, "试点子件-倒冲", "RM");
        Long phantomMatId = createMat(matCatId, uomId, "试点子件-虚拟件", "RM");

        // 5. 班次与日历（日历主档 + 一条周一规则）
        ShiftSaveReqVO shift = new ShiftSaveReqVO();
        shift.setCode("IT-SHIFT-DAY"); // mdm_shift.code 为 NOT NULL，必填
        shift.setName("集成测试白班");
        shift.setStartTime(LocalTime.of(8, 0));
        shift.setEndTime(LocalTime.of(20, 0));
        shift.setStatus(CommonStatusEnum.ENABLE.getStatus());
        Long shiftId = shiftService.createShift(shift);

        CalendarSaveReqVO calendar = new CalendarSaveReqVO();
        calendar.setCode("IT-CAL"); // mdm_calendar.code 为 NOT NULL，必填
        calendar.setName("集成测试日历");
        calendar.setStatus(CommonStatusEnum.ENABLE.getStatus());
        Long calendarId = calendarService.createCalendar(calendar);

        CalendarRuleSaveReqVO calendarRule = new CalendarRuleSaveReqVO();
        calendarRule.setCalendarId(calendarId);
        calendarRule.setDayOfWeek(1); // 周一
        calendarRule.setShiftId(shiftId);
        calendarRule.setAvailableMinutes(720);
        calendarService.createCalendarRule(calendarRule);

        // 6. 生产资源 + 资源能力 + 资源日历
        ResSaveReqVO res = new ResSaveReqVO();
        res.setWcId(wcId);
        res.setResType("MACHINE");
        res.setName("集成测试生产资源");
        res.setPlantId(plantId);
        res.setWorkshopId(workshopId);
        res.setCalendarId(calendarId);
        res.setEfficiency(BigDecimal.ONE);
        res.setParallelCapacity(1);
        res.setStatus(CommonStatusEnum.ENABLE.getStatus());
        Long resId = resService.createRes(res);

        // 7. 3 道标准工序（工时模型：前 2 道按件累加，第 3 道固定工时）
        List<Long> opIds = new ArrayList<>();
        opIds.add(createOp("集成测试工序一", "PER_UNIT"));
        opIds.add(createOp("集成测试工序二", "PER_UNIT"));
        opIds.add(createOp("集成测试工序三", "FIXED"));

        ResCapabilitySaveReqVO capability = new ResCapabilitySaveReqVO();
        capability.setResId(resId);
        capability.setOpId(opIds.get(0)); // 资源至少要能承接首道工序
        capability.setPreferredFlag(true);
        capability.setEfficiency(BigDecimal.ONE);
        capability.setStatus(CommonStatusEnum.ENABLE.getStatus());
        Long resCapabilityId = resCapabilityService.createResCapability(capability);

        ResCalendarSaveReqVO resCalendar = new ResCalendarSaveReqVO();
        resCalendar.setResId(resId);
        resCalendar.setWorkDate(EFFECTIVE_DATE);
        resCalendar.setShiftId(shiftId);
        resCalendar.setAvailableMinutes(720);
        Long resCalendarId = resCalendarService.createResCalendar(resCalendar);

        // 8. 工艺路线（先建停用态再挂工序）——3 道工序 seq=10/20/30，20 与 30 同并行组
        RouteSaveReqVO route = new RouteSaveReqVO();
        route.setMatId(fgMatId);
        route.setVersion("V1.0");
        route.setStatus(CommonStatusEnum.DISABLE.getStatus());
        route.setEffectiveDate(EFFECTIVE_DATE);
        Long routeId = routeService.createRoute(route);

        List<Long> routeOpIds = new ArrayList<>();
        routeOpIds.add(createRouteOp(routeId, opIds.get(0), wcId, null));
        routeOpIds.add(createRouteOp(routeId, opIds.get(1), wcId, "PG1"));
        routeOpIds.add(createRouteOp(routeId, opIds.get(2), wcId, "PG1"));

        // 路线工序用料：首道工序领 2 件领料件
        List<Long> routeOpMatIds = new ArrayList<>();
        RouteOpMatSaveReqVO routeOpMat = new RouteOpMatSaveReqVO();
        routeOpMat.setRouteOpId(routeOpIds.get(0));
        routeOpMat.setMatId(pickMatId);
        routeOpMat.setQty(new BigDecimal("2"));
        routeOpMat.setIssueMethod("PICK");
        routeOpMat.setMustIssueBeforeStart(true);
        routeOpMat.setUomId(uomId);
        routeOpMatIds.add(routeOpMatService.createRouteOpMat(routeOpMat));

        // 路线工序建完后再启用路线（启用态禁止改工序，故顺序不能反）
        route.setId(routeId);
        route.setStatus(CommonStatusEnum.ENABLE.getStatus());
        routeService.updateRoute(route);

        // 9. BOM（先建停用态再挂明细，最后启用）——含损耗率、虚拟件、倒冲件
        BomSaveReqVO bom = new BomSaveReqVO();
        bom.setMatId(fgMatId);
        bom.setVersion("V1.0");
        bom.setStatus(CommonStatusEnum.DISABLE.getStatus());
        bom.setEffectiveDate(EFFECTIVE_DATE);
        Long bomId = bomService.createBom(bom);

        // 子件 A：PICK，带损耗率 0.02（齐套需求量 = 父项数量 × 单位用量 ÷ (1 - 损耗率)）
        bomService.createBomLine(buildBomLine(bomId, 10, pickMatId, new BigDecimal("2"),
                new BigDecimal("0.02"), "PICK", uomId));
        // 子件 B：BACKFLUSH，无损耗（倒冲件不计入齐套快照）
        bomService.createBomLine(buildBomLine(bomId, 20, backflushMatId, new BigDecimal("1"),
                BigDecimal.ZERO, "BACKFLUSH", uomId));
        // 子件 C：PHANTOM，虚拟件本体不产生需求（其子件才产生需求）
        bomService.createBomLine(buildBomLine(bomId, 30, phantomMatId, new BigDecimal("1"),
                BigDecimal.ZERO, "PHANTOM", uomId));

        bom.setId(bomId);
        bom.setStatus(CommonStatusEnum.ENABLE.getStatus());
        bomService.updateBom(bom);

        return new PilotLine(
                /* ---- MDM 组织与资源 ---- */
                invOrgId, plantId, workshopId, wcId, uomId,
                fgMatId, List.of(pickMatId, backflushMatId, phantomMatId), matCatId,
                shiftId, calendarId, resId, resCapabilityId, resCalendarId,
                opIds, routeId, routeOpIds, routeOpMatIds, bomId,
                /* ---- WMS 仓储：由 WmsFixture 回填占位 0 ---- */
                0L, 0L, 0L, List.of(),
                /* ---- MES / 质量基础数据：由 MesFixture / QcFixture 回填占位 0 ---- */
                0L, List.of(), List.of());
    }

    /** 建一个物料主档。matType 用 MDM 既有取值，batchFlag/snFlag 由物料类型决定。 */
    private Long createMat(Long catId, Long uomId, String name, String matType) {
        MatSaveReqVO mat = new MatSaveReqVO();
        mat.setCatId(catId);
        mat.setName(name);
        mat.setMatType(matType);
        mat.setBaseUomId(uomId);
        // 成品批次管控（追溯链路要按批次走），子件不管控
        boolean fg = "FG".equals(matType);
        mat.setBatchFlag(fg);
        mat.setSnFlag(false);
        mat.setPlanStrategy("MTO");
        mat.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return matService.createMat(mat);
    }

    /** 建一道标准工序。FIXED 走 stdDuration，PER_UNIT 走 stdMinutesPerUnit。 */
    private Long createOp(String name, String durationType) {
        OpSaveReqVO op = new OpSaveReqVO();
        op.setCode("IT-OP-" + OP_SEQ.incrementAndGet()); // mdm_op.code 为 NOT NULL，必填且需唯一
        op.setName(name);
        op.setDurationType(durationType);
        if ("PER_UNIT".equals(durationType)) {
            op.setStdDuration(0);
            op.setStdMinutesPerUnit(new BigDecimal("1"));
        } else {
            op.setStdDuration(30);
        }
        op.setSetupMinutes(0);
        op.setTeardownMinutes(0);
        op.setQueueMinutes(0);
        op.setMoveMinutes(0);
        op.setManQty(1);
        op.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return opService.createOp(op);
    }

    /** 建一个路线工序。seq 由 Service 按 10 递增分配，与用例断言的 10/20/30 一致。 */
    private Long createRouteOp(Long routeId, Long opId, Long wcId, String parallelGroup) {
        RouteOpSaveReqVO routeOp = new RouteOpSaveReqVO();
        routeOp.setRouteId(routeId);
        routeOp.setOpId(opId);
        routeOp.setWcId(wcId);
        routeOp.setParallelGroup(parallelGroup);
        routeOp.setKeyFlag(true);
        routeOp.setFaiFlag(false);
        routeOp.setSkipPolicy("NOT_ALLOWED");
        return routeService.createRouteOp(routeOp);
    }

    private BomLineDO buildBomLine(Long bomId, Integer lineNo, Long matId, BigDecimal qty,
                                   BigDecimal scrapRate, String issueMethod, Long uomId) {
        return BomLineDO.builder()
                .bomId(bomId)
                .lineNo(lineNo)
                .matId(matId)
                .qty(qty)
                .scrapRate(scrapRate)
                .issueMethod(issueMethod)
                .uomId(uomId)
                .effectiveDate(EFFECTIVE_DATE)
                .build();
    }

}
