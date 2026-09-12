package com.iboot.pf.module.wms.api.batch;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.api.material.MaterialApi;
import com.iboot.pf.module.wms.api.batch.dto.BatchRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.batch.WmsBatchDO;
import com.iboot.pf.module.wms.dal.mysql.batch.WmsBatchMapper;
import com.iboot.pf.module.wms.service.batch.WmsBatchServiceImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 批次门面按批次号反查测试。
 *
 * <p>覆盖 T5-01 收尾暴露的门面缺口：调用方只持有批次号（追溯链路的领料行快照、外部单据批号）时，
 * 原来只能借 {@code mes_material_req_line.lot_no} 反查，未被领用过的批次因此报"起点不存在"。
 *
 * <p>重点守住一件事：<b>值必须是列表</b>。{@code wms_batch} 的唯一索引是
 * {@code uk_wms_batch_mat_lot_deleted(mat_id, lot_no, deleted)}，批次号只在物料内唯一，
 * 同一批次号可以合法地属于多个物料（无批次物料全部挂在默认批次号 DEFAULT 上就是这个情形）。
 * 谁哪天把返回类型"优化"成 {@code Map<String, BatchRespDTO>}，同一批次号下的批次就会互相覆盖，
 * 追溯静默丢节点——所以这里显式断言同一批次号取回两条。
 *
 * <p>只读链走真实 {@link WmsBatchServiceImpl} 打 H2，证明门面到 Mapper 这一段是通的；
 * 批次注册要校验物料，把 {@code MaterialApi} 替成 mock（本测试不调注册方法）。
 */
@Import({BatchApiImpl.class, WmsBatchServiceImpl.class})
class BatchApiImplTest extends BaseDbUnitTest {

    @Resource
    private BatchApi batchApi;
    @Resource
    private WmsBatchMapper batchMapper;
    @MockitoBean
    private MaterialApi materialApi;

    /** 播种一条批次主档，返回主键。 */
    private Long seedBatch(Long matId, String lotNo, Long supplierId, String supplierLotNo, String furnaceLotNo) {
        WmsBatchDO batch = new WmsBatchDO();
        batch.setMatId(matId);
        batch.setMatCode("M-" + matId);
        batch.setMatName("物料" + matId);
        batch.setLotNo(lotNo);
        batch.setSupplierId(supplierId);
        batch.setSupplierLotNo(supplierLotNo);
        batch.setFurnaceLotNo(furnaceLotNo);
        batch.setProduceDate(LocalDate.of(2026, 8, 1));
        batch.setStatus(0);
        batchMapper.insert(batch);
        return batch.getId();
    }

    @Test
    void testGetBatchMapByLotNos_carriesSupplierAttributes() {
        Long id = seedBatch(1001L, "LOT-A-001", 2001L, "SUP-LOT-9", "FURN-9");

        Map<String, List<BatchRespDTO>> map = batchApi.getBatchMapByLotNos(List.of("LOT-A-001"));

        assertEquals(1, map.size());
        BatchRespDTO dto = map.get("LOT-A-001").get(0);
        assertEquals(id, dto.getId());
        // T5-02 反向追溯要拿这几个属性挂供应商节点，字段缺一个链路就断
        assertEquals(1001L, dto.getMatId());
        assertEquals(2001L, dto.getSupplierId());
        assertEquals("SUP-LOT-9", dto.getSupplierLotNo());
        assertEquals("FURN-9", dto.getFurnaceLotNo());
        assertEquals(LocalDate.of(2026, 8, 1), dto.getProduceDate());
    }

    @Test
    void testGetBatchMapByLotNos_sameLotNoAcrossMaterials_returnsAll() {
        seedBatch(1001L, "DEFAULT", 2001L, null, null);
        seedBatch(1002L, "DEFAULT", 2002L, null, null);
        seedBatch(1001L, "LOT-B-002", null, null, null);

        Map<String, List<BatchRespDTO>> map = batchApi.getBatchMapByLotNos(List.of("DEFAULT"));

        assertEquals(1, map.size(), "只返回请求的批次号");
        assertEquals(2, map.get("DEFAULT").size(), "同一批次号下的不同物料都要返回，不许互相覆盖");
        assertTrue(map.get("DEFAULT").stream().allMatch(b -> "DEFAULT".equals(b.getLotNo())));
        assertFalse(map.containsKey("LOT-B-002"), "未请求的批次号不产生键");
    }

    @Test
    void testGetBatchMapByLotNos_missingAndEmpty() {
        seedBatch(1003L, "LOT-C-003", null, null, null);

        assertTrue(batchApi.getBatchMapByLotNos(List.of("LOT-NOT-EXIST")).isEmpty(), "查不到返回空 Map");
        assertTrue(batchApi.getBatchMapByLotNos(List.of()).isEmpty(), "空集合返回空 Map，不抛异常");
        assertNull(batchApi.getBatchMapByLotNos(List.of("LOT-C-003")).get("LOT-NOT-EXIST"));
    }

    @Test
    void testDataPermissionDisabledOnClassOnly() {
        DataPermission classLevel = BatchApiImpl.class.getAnnotation(DataPermission.class);
        assertNotNull(classLevel, "跨模块门面必须在类上关掉数据权限");
        assertFalse(classLevel.enable());
        for (Method method : BatchApiImpl.class.getDeclaredMethods()) {
            assertNull(method.getAnnotation(DataPermission.class), method.getName());
        }
    }

}
