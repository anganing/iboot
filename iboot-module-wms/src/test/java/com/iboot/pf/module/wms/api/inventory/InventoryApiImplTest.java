package com.iboot.pf.module.wms.api.inventory;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.infra.api.coderule.CodeRuleApi;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnQueryReqDTO;
import com.iboot.pf.module.wms.api.inventory.dto.InventoryTxnRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.inventory.WmsInvTxnDO;
import com.iboot.pf.module.wms.dal.mysql.inventory.WmsInvTxnMapper;
import com.iboot.pf.module.wms.enums.WmsIoTypeEnum;
import com.iboot.pf.module.wms.enums.WmsTxnTypeEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.service.inventory.WmsInventoryServiceImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 库存流水门面的集合维度过滤测试。
 *
 * <p>覆盖 T5-01 收尾暴露的门面缺口：流水查询原来只有单值条件，追溯要按批次取数时只能
 * "按物料分片查回 + 内存过滤"，单物料流水量大时拉取量随时间只增不减。补了 {@code batchIds}
 * 与 {@code lotNos} 之后过滤下推到 SQL。
 *
 * <p>本测试的关键是<b>证明过滤真的发生在 SQL 里</b>：造同一物料下多个批次的流水，
 * 只给 {@code batchIds} 时命中的批次之外的行不许出现在结果里。
 * 领域 Service 用真实实现打 H2，只有单号生成用的 {@code CodeRuleApi} 替成 mock（本测试不过账）。
 */
@Import({InventoryApiImpl.class, WmsInventoryServiceImpl.class, WmsCodeService.class})
class InventoryApiImplTest extends BaseDbUnitTest {

    private static final Long INV_ORG = 1L;
    private static final Long WH = 1L;
    private static final Long MAT = 100L;
    private static final String OUT_BIZ_TYPE = "PROD_ISSUE_OUT";

    @Resource
    private InventoryApi inventoryApi;
    @Resource
    private WmsInvTxnMapper txnMapper;
    @MockitoBean
    private CodeRuleApi codeRuleApi;

    /** 播种一笔领料出库流水。 */
    private void seedTxn(String txnNo, Long batchId, String lotNo, BigDecimal qty) {
        WmsInvTxnDO txn = new WmsInvTxnDO();
        txn.setTxnNo(txnNo);
        txn.setTxnType(WmsTxnTypeEnum.ISSUE.name());
        txn.setBizType(OUT_BIZ_TYPE);
        txn.setBizNo("OUT-0001");
        txn.setIoType(WmsIoTypeEnum.OUT.name());
        txn.setInvOrgId(INV_ORG);
        txn.setWhId(WH);
        txn.setFromLocId(1L);
        txn.setToLocId(0L);
        txn.setMatId(MAT);
        txn.setBatchId(batchId);
        txn.setLotNo(lotNo);
        txn.setContainerId(0L);
        txn.setUomId(1L);
        txn.setQty(qty);
        txn.setBeforeQty(new BigDecimal("100"));
        txn.setAfterQty(new BigDecimal("100").subtract(qty));
        txn.setIdempotencyKey("WMS:TEST:" + txnNo);
        txnMapper.insert(txn);
    }

    /** 同一物料下三个批次各一笔流水。 */
    private void seedSameMaterialThreeBatches() {
        seedTxn("TXN-0001", 501L, "LOT-001", new BigDecimal("10"));
        seedTxn("TXN-0002", 502L, "LOT-002", new BigDecimal("20"));
        seedTxn("TXN-0003", 503L, "LOT-003", new BigDecimal("30"));
    }

    @Test
    void testGetInventoryTxnList_byBatchIds_pushesFilterToSql() {
        seedSameMaterialThreeBatches();

        InventoryTxnQueryReqDTO query = new InventoryTxnQueryReqDTO();
        query.setMatId(MAT);
        query.setBizType(OUT_BIZ_TYPE);
        query.setBatchIds(List.of(501L, 503L));

        List<InventoryTxnRespDTO> list = inventoryApi.getInventoryTxnList(query);

        // 命中之外的批次不许回来：这就是"过滤下推 SQL"与"拉全量再内存筛"的区别
        List<Long> batchIds = list.stream().map(InventoryTxnRespDTO::getBatchId).sorted().toList();
        assertEquals(List.of(501L, 503L), batchIds);
        // DECIMAL(20,6) 读回来标度是 6，只能按 compareTo 比数值，直接 equals 会因标度不等而失败
        assertEquals(0, new BigDecimal("10").add(new BigDecimal("30"))
                .compareTo(list.stream().map(InventoryTxnRespDTO::getQty)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)),
                "两笔流水的数量都取回");
    }

    @Test
    void testGetInventoryTxnList_byLotNos() {
        seedSameMaterialThreeBatches();

        InventoryTxnQueryReqDTO query = new InventoryTxnQueryReqDTO();
        query.setBizType(OUT_BIZ_TYPE);
        query.setLotNos(List.of("LOT-002"));

        List<InventoryTxnRespDTO> list = inventoryApi.getInventoryTxnList(query);

        assertEquals(1, list.size());
        assertEquals(502L, list.get(0).getBatchId());
        assertEquals("LOT-002", list.get(0).getLotNo());
    }

    @Test
    void testGetInventoryTxnList_batchIdsAndLotNos_areIntersected() {
        seedSameMaterialThreeBatches();

        InventoryTxnQueryReqDTO query = new InventoryTxnQueryReqDTO();
        query.setBatchIds(List.of(501L, 502L));
        query.setLotNos(List.of("LOT-002", "LOT-003"));

        List<InventoryTxnRespDTO> list = inventoryApi.getInventoryTxnList(query);

        assertEquals(1, list.size(), "两个集合条件相与，取交集");
        assertEquals(502L, list.get(0).getBatchId());
    }

    @Test
    void testGetInventoryTxnList_emptyCollections_backwardCompatible() {
        seedSameMaterialThreeBatches();

        // 新参数留空 = 行为与改造前一致，既有调用方不受影响
        InventoryTxnQueryReqDTO query = new InventoryTxnQueryReqDTO();
        query.setMatId(MAT);
        query.setBizType(OUT_BIZ_TYPE);
        query.setBatchIds(List.of());
        query.setLotNos(List.of());
        assertEquals(3, inventoryApi.getInventoryTxnList(query).size(), "空集合不过滤");

        query.setBatchIds(List.of(999L));
        assertTrue(inventoryApi.getInventoryTxnList(query).isEmpty(), "集合里没有的批次返回空");
    }

    @Test
    void testDataPermissionDisabledOnClassOnly() {
        DataPermission classLevel = InventoryApiImpl.class.getAnnotation(DataPermission.class);
        assertNotNull(classLevel, "跨模块门面必须在类上关掉数据权限");
        assertFalse(classLevel.enable());
        for (Method method : InventoryApiImpl.class.getDeclaredMethods()) {
            assertNull(method.getAnnotation(DataPermission.class), method.getName());
        }
    }

}
