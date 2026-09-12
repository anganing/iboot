package com.iboot.pf.module.wms.api.outorder;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.api.outorder.dto.OutOrderRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.outorder.WmsOutOrderDO;
import com.iboot.pf.module.wms.dal.mysql.outorder.WmsOutOrderMapper;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.service.outorder.WmsOutOrderService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 出库单门面只读查询测试。
 *
 * <p>插入两条出库单，验证批量反查只取回这两条并按编号映射；空集合直接返回空 Map。
 * 顺带用反射守住类级数据权限注解：任务书裁定门面类级关闭、不许逐方法加。
 *
 * <p>命令方法的行为由 {@code WmsOutOrderServiceImplTest} 保证，这里把领域 Service 替成 mock，
 * 只让 {@code getOutOrderMap} 走真实 Mapper 打 H2，证明门面这条只读链是通的。
 */
@Import(OutOrderApiImpl.class)
class OutOrderApiImplTest extends BaseDbUnitTest {

    @Resource
    private OutOrderApi outOrderApi;
    @Resource
    private WmsOutOrderMapper outOrderMapper;
    @MockitoBean
    private WmsOutOrderService outOrderService;

    private void seedOrder(Long id, String outNo, WmsOrderStatusEnum status) {
        WmsOutOrderDO order = new WmsOutOrderDO();
        order.setId(id);
        order.setOutNo(outNo);
        order.setBizType("PROD_ISSUE_OUT");
        order.setSourceType("MES");
        order.setSourceBizNo("MO-" + id);
        order.setInvOrgId(1L);
        order.setWhId(1L);
        order.setStatus(status.name());
        outOrderMapper.insert(order);
    }

    /** 播种销售发货出库单：带上追溯客户节点要用的客户编号与工单号快照。 */
    private void seedSalesOrder(Long id, String outNo, Long customerId, String prodOrderNo) {
        WmsOutOrderDO order = new WmsOutOrderDO();
        order.setId(id);
        order.setOutNo(outNo);
        order.setBizType("SALES_DELIVERY_OUT");
        order.setSourceType("UI");
        order.setSourceBizNo("SO-" + id);
        order.setCustomerId(customerId);
        order.setProdOrderNo(prodOrderNo);
        order.setInvOrgId(1L);
        order.setWhId(1L);
        order.setStatus(WmsOrderStatusEnum.DONE.name());
        outOrderMapper.insert(order);
    }

    @Test
    void testGetOutOrderMap_returnsOnlyRequested() {
        seedOrder(101L, "OUT-0101", WmsOrderStatusEnum.DRAFT);
        seedOrder(102L, "OUT-0102", WmsOrderStatusEnum.DONE);
        seedOrder(103L, "OUT-0103", WmsOrderStatusEnum.CANCELLED);

        Map<Long, OutOrderRespDTO> map = outOrderApi.getOutOrderMap(List.of(101L, 103L));

        assertEquals(2, map.size(), "只取回请求的两条");
        assertEquals("OUT-0101", map.get(101L).getOutNo());
        assertEquals("PROD_ISSUE_OUT", map.get(101L).getBizType());
        assertEquals("MES", map.get(101L).getSourceType());
        assertEquals(1L, map.get(101L).getWhId());
        assertEquals(WmsOrderStatusEnum.DRAFT.name(), map.get(101L).getStatus());
        assertEquals("OUT-0103", map.get(103L).getOutNo());
        assertEquals(WmsOrderStatusEnum.CANCELLED.name(), map.get(103L).getStatus());
        assertNull(map.get(102L), "未请求的单据不应出现在结果里");
    }

    @Test
    void testGetOutOrderMap_emptyIds_returnsEmptyMap() {
        assertTrue(outOrderApi.getOutOrderMap(List.of()).isEmpty(), "空集合返回空 Map，不抛异常");
    }

    @Test
    void testGetOutOrderMap_carriesCustomerIdAndProdOrderNo() {
        seedSalesOrder(111L, "OUT-0111", 3001L, "MO-20260912-001");

        OutOrderRespDTO dto = outOrderApi.getOutOrderMap(List.of(111L)).get(111L);

        // 追溯客户节点靠这两个字段：按 id 反查同样要能拿到（BeanUtils 逐字段拷贝，不能只在新方法里补）
        assertEquals(3001L, dto.getCustomerId());
        assertEquals("MO-20260912-001", dto.getProdOrderNo());
    }

    @Test
    void testGetOutOrderMapByNos_returnsOnlyRequested() {
        seedOrder(121L, "OUT-0121", WmsOrderStatusEnum.DONE);
        seedSalesOrder(122L, "OUT-0122", 3002L, "MO-20260912-002");
        seedSalesOrder(123L, "OUT-0123", null, null);

        Map<String, OutOrderRespDTO> map = outOrderApi.getOutOrderMapByNos(List.of("OUT-0121", "OUT-0122"));

        assertEquals(2, map.size(), "只取回请求的两条");
        assertEquals(121L, map.get("OUT-0121").getId());
        assertEquals("PROD_ISSUE_OUT", map.get("OUT-0121").getBizType());
        assertEquals(3002L, map.get("OUT-0122").getCustomerId(), "销售出库单的客户编号");
        assertEquals("MO-20260912-002", map.get("OUT-0122").getProdOrderNo(), "生产工单号快照");
        assertNull(map.get("OUT-0123"), "未请求的单号不应出现在结果里");
    }

    @Test
    void testGetOutOrderMapByNos_missingAndEmpty() {
        seedOrder(131L, "OUT-0131", WmsOrderStatusEnum.DRAFT);

        // 查不到的单号不产生键，也不抛异常
        assertTrue(outOrderApi.getOutOrderMapByNos(List.of("OUT-NOT-EXIST")).isEmpty());
        assertTrue(outOrderApi.getOutOrderMapByNos(List.of()).isEmpty(), "空集合返回空 Map");
    }

    @Test
    void testDataPermissionDisabledOnClassOnly() {
        DataPermission classLevel = OutOrderApiImpl.class.getAnnotation(DataPermission.class);
        assertNotNull(classLevel, "跨模块门面必须在类上关掉数据权限");
        assertFalse(classLevel.enable());
        for (Method method : OutOrderApiImpl.class.getDeclaredMethods()) {
            assertNull(method.getAnnotation(DataPermission.class), method.getName());
        }
    }

}
