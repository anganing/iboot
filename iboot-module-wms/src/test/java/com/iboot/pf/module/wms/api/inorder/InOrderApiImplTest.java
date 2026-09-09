package com.iboot.pf.module.wms.api.inorder;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.api.inorder.dto.InOrderRespDTO;
import com.iboot.pf.module.wms.dal.dataobject.inorder.WmsInOrderDO;
import com.iboot.pf.module.wms.dal.mysql.inorder.WmsInOrderMapper;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.service.inorder.WmsInOrderService;
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
 * 入库单门面只读查询测试。
 *
 * <p>插入两条入库单，验证批量反查只取回这两条并按编号映射；空集合直接返回空 Map。
 * 顺带用反射守住类级数据权限注解：任务书裁定门面类级关闭、不许逐方法加。
 *
 * <p>命令方法的行为由 {@code WmsInOrderServiceImplTest} 保证，这里把领域 Service 替成 mock，
 * 只让 {@code getInOrderMap} 走真实 Mapper 打 H2，证明门面这条只读链是通的。
 */
@Import(InOrderApiImpl.class)
class InOrderApiImplTest extends BaseDbUnitTest {

    @Resource
    private InOrderApi inOrderApi;
    @Resource
    private WmsInOrderMapper inOrderMapper;
    @MockitoBean
    private WmsInOrderService inOrderService;

    private void seedOrder(Long id, String inNo, WmsOrderStatusEnum status) {
        WmsInOrderDO order = new WmsInOrderDO();
        order.setId(id);
        order.setInNo(inNo);
        order.setBizType("PO_IN");
        order.setSourceType("MES");
        order.setSourceBizNo("AS-" + id);
        order.setInvOrgId(1L);
        order.setWhId(1L);
        order.setStatus(status.name());
        inOrderMapper.insert(order);
    }

    @Test
    void testGetInOrderMap_returnsOnlyRequested() {
        seedOrder(201L, "IN-0201", WmsOrderStatusEnum.DRAFT);
        seedOrder(202L, "IN-0202", WmsOrderStatusEnum.DONE);
        seedOrder(203L, "IN-0203", WmsOrderStatusEnum.CANCELLED);

        Map<Long, InOrderRespDTO> map = inOrderApi.getInOrderMap(List.of(201L, 202L));

        assertEquals(2, map.size(), "只取回请求的两条");
        assertEquals("IN-0201", map.get(201L).getInNo());
        assertEquals("PO_IN", map.get(201L).getBizType());
        assertEquals("MES", map.get(201L).getSourceType());
        assertEquals(1L, map.get(201L).getWhId());
        assertEquals(WmsOrderStatusEnum.DRAFT.name(), map.get(201L).getStatus());
        assertEquals("IN-0202", map.get(202L).getInNo());
        assertEquals(WmsOrderStatusEnum.DONE.name(), map.get(202L).getStatus());
        assertNull(map.get(203L), "未请求的单据不应出现在结果里");
    }

    @Test
    void testGetInOrderMap_emptyIds_returnsEmptyMap() {
        assertTrue(inOrderApi.getInOrderMap(List.of()).isEmpty(), "空集合返回空 Map，不抛异常");
    }

    @Test
    void testDataPermissionDisabledOnClassOnly() {
        DataPermission classLevel = InOrderApiImpl.class.getAnnotation(DataPermission.class);
        assertNotNull(classLevel, "跨模块门面必须在类上关掉数据权限");
        assertFalse(classLevel.enable());
        for (Method method : InOrderApiImpl.class.getDeclaredMethods()) {
            assertNull(method.getAnnotation(DataPermission.class), method.getName());
        }
    }

}
