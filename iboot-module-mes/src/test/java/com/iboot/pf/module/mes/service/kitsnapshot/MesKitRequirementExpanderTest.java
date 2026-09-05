package com.iboot.pf.module.mes.service.kitsnapshot;

import com.iboot.pf.module.mdm.api.bom.BomApi;
import com.iboot.pf.module.mdm.api.bom.dto.BomLineRespDTO;
import com.iboot.pf.module.mdm.api.bom.dto.BomRespDTO;
import com.iboot.pf.module.mdm.api.route.RouteApi;
import com.iboot.pf.module.mdm.api.route.dto.RouteOpMatRespDTO;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesIssueMethodEnum;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitRequirementBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.KIT_CHECK_BOM_DEPTH_EXCEEDED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 需求展开测试。
 *
 * <p>不连数据库：展开只依赖 mdm 门面，纯 mock 跑得比起 Spring 容器快一个数量级。
 * 齐套的落库与判定由 {@link MesKitCheckServiceImplTest} 覆盖，这里只盯三件事：
 * 损耗率公式、三种供料方式的分流、按物料合并。
 */
class MesKitRequirementExpanderTest {

    private static final Long BOM_ID = 11L;
    private static final Long PHANTOM_BOM_ID = 12L;
    private static final Long MAT_A = 1001L;
    private static final Long MAT_CHILD = 1003L;
    private static final Long MAT_PHANTOM = 1004L;
    private static final Long MAT_BACKFLUSH = 1005L;
    private static final Long ROUTE_OP_CUT = 101L;
    private static final Long ROUTE_OP_MACHINE = 102L;
    private static final BigDecimal PARENT_QTY = new BigDecimal("100");

    private MesKitRequirementExpander expander;
    private BomApi bomApi;
    private RouteApi routeApi;

    @BeforeEach
    void setUp() {
        expander = new MesKitRequirementExpander();
        bomApi = mock(BomApi.class);
        routeApi = mock(RouteApi.class);
        ReflectionTestUtils.setField(expander, "bomApi", bomApi);
        ReflectionTestUtils.setField(expander, "routeApi", routeApi);
    }

    @Test
    void testExpandByBom_scrapRateFormula() {
        // 准备：单位用量 2、损耗率 0.05，父项 100
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(MAT_A, "2", "0.05", MesIssueMethodEnum.PICK)));

        // 调用
        List<MesKitRequirementBO> requirements = expander.expandByBom(BOM_ID, PARENT_QTY);

        // 断言：100 × 2 ÷ (1 − 0.05) = 210.526316，精度取库表的 decimal(20,6)
        assertEquals(1, requirements.size());
        assertEquals(new BigDecimal("210.526316"), requirements.get(0).getReqQty());
        assertEquals(MAT_A, requirements.get(0).getMatId());
        // 整单 BOM 展开不带工序用料编号
        assertEquals(MesConstants.NONE_ID, requirements.get(0).getRouteOpMatId());
    }

    @Test
    void testExpandByBom_nullScrapRateTreatedAsZero() {
        // 准备：损耗率没维护
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(MAT_A, "3", null, MesIssueMethodEnum.PICK)));

        // 调用并断言：按 0 损耗算，300 而不是抛参数异常
        assertEquals(0, new BigDecimal("300").compareTo(
                expander.expandByBom(BOM_ID, PARENT_QTY).get(0).getReqQty()));
    }

    @Test
    void testExpandByBom_backflushSkippedPhantomExpanded() {
        // 准备：三行分别是常规料、倒冲件、虚拟件；虚拟件下挂一个真实子件，用量 3
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(MAT_A, "1", "0", MesIssueMethodEnum.PICK),
                bomLine(MAT_BACKFLUSH, "5", "0", MesIssueMethodEnum.BACKFLUSH),
                bomLine(MAT_PHANTOM, "2", "0", MesIssueMethodEnum.PHANTOM)));
        givenPhantomBom(bomLine(MAT_CHILD, "3", "0", MesIssueMethodEnum.PICK));

        // 调用
        List<MesKitRequirementBO> requirements = expander.expandByBom(BOM_ID, PARENT_QTY);

        // 断言：倒冲件与虚拟件本身都不产生需求，虚拟件的子件按 200 × 3 = 600 产生需求
        assertEquals(2, requirements.size());
        assertEquals(0, new BigDecimal("100").compareTo(requirementOf(requirements, MAT_A).getReqQty()));
        assertEquals(0, new BigDecimal("600").compareTo(requirementOf(requirements, MAT_CHILD).getReqQty()));
        assertTrue(requirements.stream().noneMatch(item -> MAT_BACKFLUSH.equals(item.getMatId())));
        assertTrue(requirements.stream().noneMatch(item -> MAT_PHANTOM.equals(item.getMatId())));
    }

    @Test
    void testExpandByBom_phantomCycleRejected() {
        // 准备：虚拟件的 BOM 里又是它自己
        when(bomApi.getBomLineList(BOM_ID)).thenReturn(List.of(
                bomLine(MAT_PHANTOM, "1", "0", MesIssueMethodEnum.PHANTOM)));
        givenPhantomBom(bomLine(MAT_PHANTOM, "1", "0", MesIssueMethodEnum.PHANTOM));

        // 调用并断言：十层封顶，成环时报错而不是栈溢出
        assertServiceException(() -> expander.expandByBom(BOM_ID, PARENT_QTY),
                KIT_CHECK_BOM_DEPTH_EXCEEDED, 10);
    }

    @Test
    void testExpandByRouteOps_mergeSameMaterialAndKeepStrictestMustIssue() {
        // 准备：两道工序都用同一个物料，一处可以后补、一处必须开工前发
        when(routeApi.getRouteOpMatList(any())).thenReturn(List.of(
                routeOpMat(401L, ROUTE_OP_CUT, MAT_A, "1", false),
                routeOpMat(402L, ROUTE_OP_MACHINE, MAT_A, "2", true)));

        // 调用
        List<MesKitRequirementBO> requirements = expander.expandByRouteOps(
                List.of(ROUTE_OP_CUT, ROUTE_OP_MACHINE), PARENT_QTY);

        // 断言：合并成一行、需求相加、工序用料编号取首次出现的那条
        assertEquals(1, requirements.size());
        MesKitRequirementBO requirement = requirements.get(0);
        assertEquals(0, new BigDecimal("300").compareTo(requirement.getReqQty()));
        assertEquals(401L, requirement.getRouteOpMatId());
        // 服从更严的那条：否则必须先发料的那道工序会被放行
        assertTrue(requirement.getMustIssueBeforeStart());
    }

    @Test
    void testExpandByRouteOps_emptyRouteOpsSkipsFacade() {
        // 调用并断言：空集合直接返回空，不去打扰门面
        assertTrue(expander.expandByRouteOps(List.of(), PARENT_QTY).isEmpty());
        verifyNoInteractions(routeApi);
    }

    private void givenPhantomBom(BomLineRespDTO... lines) {
        BomRespDTO phantomBom = new BomRespDTO();
        phantomBom.setId(PHANTOM_BOM_ID);
        when(bomApi.getEffectiveBom(eq(MAT_PHANTOM), any())).thenReturn(phantomBom);
        when(bomApi.getBomLineList(PHANTOM_BOM_ID)).thenReturn(List.of(lines));
    }

    private MesKitRequirementBO requirementOf(List<MesKitRequirementBO> requirements, Long matId) {
        return requirements.stream().filter(item -> matId.equals(item.getMatId())).findFirst().orElseThrow();
    }

    private BomLineRespDTO bomLine(Long matId, String qty, String scrapRate, MesIssueMethodEnum issueMethod) {
        BomLineRespDTO line = new BomLineRespDTO();
        line.setBomId(BOM_ID);
        line.setMatId(matId);
        line.setQty(new BigDecimal(qty));
        line.setScrapRate(scrapRate == null ? null : new BigDecimal(scrapRate));
        line.setIssueMethod(issueMethod.name());
        line.setUomId(1L);
        return line;
    }

    private RouteOpMatRespDTO routeOpMat(Long id, Long routeOpId, Long matId, String qty, boolean mustIssue) {
        RouteOpMatRespDTO opMat = new RouteOpMatRespDTO();
        opMat.setId(id);
        opMat.setRouteOpId(routeOpId);
        opMat.setMatId(matId);
        opMat.setQty(new BigDecimal(qty));
        opMat.setScrapRate(BigDecimal.ZERO);
        opMat.setIssueMethod(MesIssueMethodEnum.PICK.name());
        opMat.setMustIssueBeforeStart(mustIssue);
        opMat.setUomId(1L);
        return opMat;
    }

}
