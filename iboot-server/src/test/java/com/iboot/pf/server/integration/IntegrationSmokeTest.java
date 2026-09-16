package com.iboot.pf.server.integration;

import com.iboot.pf.module.mdm.controller.admin.mat.vo.MatRespVO;
import com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants;
import com.iboot.pf.module.mdm.service.bom.BomService;
import com.iboot.pf.module.mdm.service.mat.MatService;
import com.iboot.pf.module.mdm.service.route.RouteService;
import com.iboot.pf.module.wms.api.inventory.InventoryApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryBalanceRespDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryQueryReqDTO;
import com.iboot.pf.server.integration.fixture.MdmFixture;
import com.iboot.pf.server.integration.fixture.MesFixture;
import com.iboot.pf.server.integration.fixture.QcFixture;
import com.iboot.pf.server.integration.fixture.WmsFixture;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * T6-01 冒烟用例：验证集成测试基础设施本身可用。
 *
 * 覆盖三件事：
 * 1. 完整 Spring 上下文可启动（全部业务模块真实装配，不含 mock 门面）；
 * 2. H2 内存库 schema 与基础数据就绪（字典 / 编码规则 / 测试用户角色）；
 * 3. 四个 fixture 建一条试点产线全套主数据，断言主数据可查（走真实 Service/门面读取路径）。
 *
 * 该用例是 T6-02 ~ T6-08 全部端到端用例的地基，只验证基建，不写业务断言。
 */
class IntegrationSmokeTest extends BaseIntegrationTest {

    @Resource
    private MatService matService;

    @Resource
    private BomService bomService;

    @Resource
    private RouteService routeService;

    @Resource
    private InventoryApi inventoryApi;

    @Resource
    private MdmFixture mdmFixture;

    @Resource
    private WmsFixture wmsFixture;

    @Resource
    private MesFixture mesFixture;

    @Resource
    private QcFixture qcFixture;

    @Test
    void contextLoads() {
        // 上下文能走到这里即代表：155 张业务表 DDL 全部建表成功、
        // 81 个 @KeySequence 序列存在、基础数据加载成功、
        // Flowable 引擎自建 ACT_* 表成功、jedis-mock Redis 正常。
    }

    @Test
    void baseDataLoaded() {
        // 编码规则（infra_code_rule，54 条）由 base-data.sql 预置——
        // 编码生成是全部业务单据编号的入口，这里验证 CodeRuleApi 真实可用
        String matCode = mdmFixture.nextCode(MdmCodeRuleConstants.MAT);
        assertNotNull(matCode);
        assertFalse(matCode.isBlank());
    }

    @Test
    void buildPilotLineMasterData() {
        // 建全套主数据（MdmFixture + WmsFixture + MesFixture + QcFixture）
        PilotLine line = mdmFixture.createPilotLine();
        WmsFixture.WmsStock stock = wmsFixture.createWarehouseWithStock(line);
        Long demandId = mesFixture.createProdDemand(line);
        QcFixture.QcBasics qc = qcFixture.createQcBasics(line);

        // 主数据可查（真实 Service 查询路径）
        MatRespVO fgMat = matService.getMat(line.fgMatId());
        assertNotNull(fgMat);
        assertEquals(3, line.componentMatIds().size(), "成品 + 3 子件（覆盖三种 issue_method）");
        assertEquals(3, line.opIds().size(), "3 道工序");
        assertEquals(3, line.routeOpIds().size(), "3 个路线工序");
        assertNotNull(bomService.getBomDO(line.bomId()));
        assertNotNull(routeService.getRouteDO(line.routeId()));

        // WMS 侧：仓库/库区/库位已建，初始库存经 InOrderApi 真实过账建立后可查
        assertNotNull(stock.whId());
        assertNotNull(stock.zoneId());
        assertNotNull(stock.locId());
        assertEquals(1, stock.inOrderIds().size(), "初始库存一笔入库单过账");
        List<InventoryBalanceRespDTO> balances = inventoryApi.getInventoryBalanceList(
                new InventoryQueryReqDTO());
        assertFalse(balances.isEmpty(), "初始库存应经 InOrderApi 真实过账生成余额");

        // MES 侧：生产需求已建
        assertNotNull(demandId);

        // 质量侧：方案已发布，五检验点规则齐备
        assertNotNull(qc.qcPlanId());
        assertEquals(2, qc.qcItemIds().size(), "检验项 2 条（数值型 + 文本型）");
        assertEquals(5, qc.qcRuleIds().size(), "五个检验点各一条规则");
    }
}
