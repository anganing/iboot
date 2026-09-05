package com.iboot.pf.module.mes.api.kit;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mes.api.kit.dto.KitCheckReqDTO;
import com.iboot.pf.module.mes.api.kit.dto.KitCheckRespDTO;
import com.iboot.pf.module.mes.api.kit.dto.KitSnapshotRespDTO;
import com.iboot.pf.module.mes.dal.dataobject.kitsnapshot.KitSnapshotDO;
import com.iboot.pf.module.mes.dal.mysql.kitsnapshot.KitSnapshotMapper;
import com.iboot.pf.module.mes.enums.MesConstants;
import com.iboot.pf.module.mes.enums.MesKitCheckScopeEnum;
import com.iboot.pf.module.mes.enums.MesKitStatusEnum;
import com.iboot.pf.module.mes.service.kitsnapshot.MesKitCheckService;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitCheckBO;
import com.iboot.pf.module.mes.service.kitsnapshot.bo.MesKitCheckResultBO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 齐套快照门面委托测试。
 *
 * <p>检查动作 mock 掉（需求展开与库存比对由它自己的测试类保证），这里证的是"结论与明细一起带出去"：
 * 只给一个 {@code PARTIAL_KIT} 而不说缺什么、缺多少、有没有替代料，调用方拿到了也没法备料。
 *
 * <p>快照查询走真实 H2：它按 {@code prod_order_id} 过滤，用替身断言不了"查的是这张工单的快照"。
 */
@Import(KitSnapshotApiImpl.class)
class KitSnapshotApiImplTest extends BaseDbUnitTest {

    private static final Long PROD_ORDER_ID = 99L;
    private static final String CHECK_NO = "KC-0001";
    private static final LocalDateTime CHECK_TIME = LocalDateTime.of(2026, 9, 5, 10, 0);

    @Resource
    private KitSnapshotApi kitSnapshotApi;
    @Resource
    private KitSnapshotMapper kitSnapshotMapper;

    @MockitoBean
    private MesKitCheckService kitCheckService;

    @Test
    void testCheckKitSnapshot_delegatesAndCarriesLines() {
        MesKitCheckResultBO result = new MesKitCheckResultBO();
        result.setCheckNo(CHECK_NO);
        result.setCheckScope(MesKitCheckScopeEnum.PROD_ORDER.name());
        result.setProdOrderId(PROD_ORDER_ID);
        result.setWorkOrderId(MesConstants.NONE_ID);
        result.setOpTaskId(MesConstants.NONE_ID);
        result.setResult(MesKitStatusEnum.PARTIAL_KIT.name());
        result.setCheckTime(CHECK_TIME);
        result.setLines(List.of(snapshot(1, 1001L, "MAT-1001", new BigDecimal("210.526316"),
                        new BigDecimal("300"), BigDecimal.ZERO, MesKitStatusEnum.FULL_KIT, 0L),
                snapshot(2, 1002L, "MAT-1002", new BigDecimal("100"),
                        new BigDecimal("70"), new BigDecimal("30"), MesKitStatusEnum.SHORTAGE, 1099L)));
        when(kitCheckService.checkKit(any())).thenReturn(result);

        KitCheckReqDTO reqDTO = new KitCheckReqDTO();
        reqDTO.setIdempotencyKey("UI:PO-0001:KIT_CHECK:KC-0001");
        reqDTO.setSourceModule("UI");
        reqDTO.setSourceBizNo("PO-0001");
        reqDTO.setCheckScope(MesKitCheckScopeEnum.PROD_ORDER.name());
        reqDTO.setProdOrderId(PROD_ORDER_ID);
        reqDTO.setWorkOrderId(MesConstants.NONE_ID);
        reqDTO.setOpTaskId(MesConstants.NONE_ID);
        reqDTO.setWhId(2L);

        KitCheckRespDTO respDTO = kitSnapshotApi.checkKitSnapshot(reqDTO);

        ArgumentCaptor<MesKitCheckBO> captor = ArgumentCaptor.forClass(MesKitCheckBO.class);
        verify(kitCheckService).checkKit(captor.capture());
        MesKitCheckBO bo = captor.getValue();
        assertEquals(MesKitCheckScopeEnum.PROD_ORDER.name(), bo.getCheckScope());
        assertEquals(PROD_ORDER_ID, bo.getProdOrderId());
        assertEquals(2L, bo.getWhId());
        assertEquals("UI:PO-0001:KIT_CHECK:KC-0001", bo.getIdempotencyKey());
        assertEquals("UI", bo.getSourceModule());

        assertNotNull(respDTO);
        assertEquals(CHECK_NO, respDTO.getCheckNo());
        assertEquals(MesKitStatusEnum.PARTIAL_KIT.name(), respDTO.getResult());
        assertEquals(CHECK_TIME, respDTO.getCheckTime());
        assertEquals(2, respDTO.getLines().size());
        // 缺料行的三个数字与替代料是车间备料的全部依据，一个都不能丢
        KitSnapshotRespDTO shortage = respDTO.getLines().get(1);
        assertEquals("MAT-1002", shortage.getMatCode());
        assertEquals(0, new BigDecimal("100").compareTo(shortage.getReqQty()));
        assertEquals(0, new BigDecimal("70").compareTo(shortage.getAvailQty()));
        assertEquals(0, new BigDecimal("30").compareTo(shortage.getShortQty()));
        assertEquals(1099L, shortage.getAltMatId());
        assertEquals(MesKitStatusEnum.SHORTAGE.name(), shortage.getResult());
        // 损耗率场景的六位小数需求量原样带出，四舍五入会让备料数量对不上
        assertEquals(0, new BigDecimal("210.526316").compareTo(respDTO.getLines().get(0).getReqQty()));
    }

    @Test
    void testGetKitSnapshotList_filtersByProdOrderAndKeepsOrder() {
        KitSnapshotDO first = snapshot(1, 1001L, "MAT-1001", new BigDecimal("100"),
                new BigDecimal("100"), BigDecimal.ZERO, MesKitStatusEnum.FULL_KIT, 0L);
        KitSnapshotDO second = snapshot(2, 1002L, "MAT-1002", new BigDecimal("50"),
                new BigDecimal("20"), new BigDecimal("30"), MesKitStatusEnum.SHORTAGE, 0L);
        kitSnapshotMapper.insert(first);
        kitSnapshotMapper.insert(second);
        // 别的工单的快照，用来证明按工单过滤生效而不是全表返回
        KitSnapshotDO otherOrder = snapshot(1, 1003L, "MAT-1003", new BigDecimal("10"),
                new BigDecimal("10"), BigDecimal.ZERO, MesKitStatusEnum.FULL_KIT, 0L);
        otherOrder.setProdOrderId(100L);
        kitSnapshotMapper.insert(otherOrder);

        List<KitSnapshotRespDTO> list = kitSnapshotApi.getKitSnapshotList(PROD_ORDER_ID);

        assertEquals(2, list.size());
        assertEquals("MAT-1001", list.get(0).getMatCode());
        assertEquals("MAT-1002", list.get(1).getMatCode());
        assertEquals(CHECK_NO, list.get(0).getCheckNo());
        assertEquals(0, new BigDecimal("30").compareTo(list.get(1).getShortQty()));
    }

    @Test
    void testGetKitSnapshotList_nullProdOrderId() {
        assertTrue(kitSnapshotApi.getKitSnapshotList(null).isEmpty());
    }

    @Test
    void testDataPermissionDisabledOnClassOnly() {
        DataPermission classLevel = KitSnapshotApiImpl.class.getAnnotation(DataPermission.class);
        assertNotNull(classLevel, "跨模块门面必须在类上关掉数据权限");
        assertFalse(classLevel.enable());
        for (Method method : KitSnapshotApiImpl.class.getDeclaredMethods()) {
            assertNull(method.getAnnotation(DataPermission.class), method.getName());
        }
    }

    private KitSnapshotDO snapshot(Integer lineNo, Long matId, String matCode, BigDecimal reqQty,
                                   BigDecimal availQty, BigDecimal shortQty, MesKitStatusEnum result,
                                   Long altMatId) {
        return KitSnapshotDO.builder()
                .checkNo(CHECK_NO).checkScope(MesKitCheckScopeEnum.PROD_ORDER.name())
                .prodOrderId(PROD_ORDER_ID).workOrderId(MesConstants.NONE_ID).opTaskId(MesConstants.NONE_ID)
                .lineNo(lineNo).matId(matId).matCode(matCode).matName("物料" + matId)
                .reqQty(reqQty).availQty(availQty)
                .reservedQty(BigDecimal.ZERO).issuedQty(BigDecimal.ZERO).shortQty(shortQty)
                .altMatId(altMatId).result(result.name()).checkTime(CHECK_TIME)
                .build();
    }

}
