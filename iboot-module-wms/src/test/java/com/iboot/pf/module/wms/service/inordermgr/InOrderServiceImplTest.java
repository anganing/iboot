package com.iboot.pf.module.wms.service.inordermgr;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.inordermgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.inordermgr.InOrderDO;
import com.iboot.pf.module.wms.dal.mysql.inordermgr.InOrderMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static com.iboot.pf.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link InOrderServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(InOrderServiceImpl.class)
public class InOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InOrderServiceImpl inOrderService;

    @Resource
    private InOrderMapper inOrderMapper;

    @Test
    public void testCreateInOrder_success() {
        // 准备参数
        InOrderSaveReqVO createReqVO = randomPojo(InOrderSaveReqVO.class).setId(null);

        // 调用
        Long inOrderId = inOrderService.createInOrder(createReqVO);
        // 断言
        assertNotNull(inOrderId);
        // 校验记录的属性是否正确
        InOrderDO inOrder = inOrderMapper.selectById(inOrderId);
        assertPojoEquals(createReqVO, inOrder, "id");
    }

    @Test
    public void testUpdateInOrder_success() {
        // mock 数据
        InOrderDO dbInOrder = randomPojo(InOrderDO.class);
        inOrderMapper.insert(dbInOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InOrderSaveReqVO updateReqVO = randomPojo(InOrderSaveReqVO.class, o -> {
            o.setId(dbInOrder.getId()); // 设置更新的 ID
        });

        // 调用
        inOrderService.updateInOrder(updateReqVO);
        // 校验是否更新正确
        InOrderDO inOrder = inOrderMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, inOrder);
    }

    @Test
    public void testUpdateInOrder_notExists() {
        // 准备参数
        InOrderSaveReqVO updateReqVO = randomPojo(InOrderSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> inOrderService.updateInOrder(updateReqVO), IN_ORDER_NOT_EXISTS);
    }

    @Test
    public void testDeleteInOrder_success() {
        // mock 数据
        InOrderDO dbInOrder = randomPojo(InOrderDO.class);
        inOrderMapper.insert(dbInOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbInOrder.getId();

        // 调用
        inOrderService.deleteInOrder(id);
       // 校验数据不存在了
       assertNull(inOrderMapper.selectById(id));
    }

    @Test
    public void testDeleteInOrder_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> inOrderService.deleteInOrder(id), IN_ORDER_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInOrderPage() {
       // mock 数据
       InOrderDO dbInOrder = randomPojo(InOrderDO.class, o -> { // 等会查询到
           o.setInNo(null);
           o.setBizType(null);
           o.setSourceType(null);
           o.setSourceBizNo(null);
           o.setSourceLineId(null);
           o.setOriginType(null);
           o.setOriginNo(null);
           o.setArrivalId(null);
           o.setSupplierId(null);
           o.setCustomerId(null);
           o.setProdOrderNo(null);
           o.setWorkOrderNo(null);
           o.setTransferNo(null);
           o.setCountOrderNo(null);
           o.setInvOrgId(null);
           o.setPlantId(null);
           o.setProjectId(null);
           o.setProjectTaskId(null);
           o.setWhId(null);
           o.setWhCode(null);
           o.setFromInvOrgId(null);
           o.setFromWhId(null);
           o.setNeedQc(null);
           o.setIqcMode(null);
           o.setQualityStatus(null);
           o.setHookMode(null);
           o.setStatus(null);
           o.setIdempotencyKey(null);
           o.setBizAttrsJson(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       inOrderMapper.insert(dbInOrder);
       // 测试 inNo 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setInNo(null)));
       // 测试 bizType 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setBizType(null)));
       // 测试 sourceType 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setSourceType(null)));
       // 测试 sourceBizNo 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setSourceBizNo(null)));
       // 测试 sourceLineId 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setSourceLineId(null)));
       // 测试 originType 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setOriginType(null)));
       // 测试 originNo 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setOriginNo(null)));
       // 测试 arrivalId 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setArrivalId(null)));
       // 测试 supplierId 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setSupplierId(null)));
       // 测试 customerId 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setCustomerId(null)));
       // 测试 prodOrderNo 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setProdOrderNo(null)));
       // 测试 workOrderNo 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setWorkOrderNo(null)));
       // 测试 transferNo 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setTransferNo(null)));
       // 测试 countOrderNo 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setCountOrderNo(null)));
       // 测试 invOrgId 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setInvOrgId(null)));
       // 测试 plantId 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setPlantId(null)));
       // 测试 projectId 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setProjectId(null)));
       // 测试 projectTaskId 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setProjectTaskId(null)));
       // 测试 whId 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setWhId(null)));
       // 测试 whCode 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setWhCode(null)));
       // 测试 fromInvOrgId 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setFromInvOrgId(null)));
       // 测试 fromWhId 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setFromWhId(null)));
       // 测试 needQc 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setNeedQc(null)));
       // 测试 iqcMode 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setIqcMode(null)));
       // 测试 qualityStatus 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setQualityStatus(null)));
       // 测试 hookMode 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setHookMode(null)));
       // 测试 status 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setStatus(null)));
       // 测试 idempotencyKey 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setIdempotencyKey(null)));
       // 测试 bizAttrsJson 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setBizAttrsJson(null)));
       // 测试 remark 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       inOrderMapper.insert(cloneIgnoreId(dbInOrder, o -> o.setCreateTime(null)));
       // 准备参数
       InOrderPageReqVO reqVO = new InOrderPageReqVO();
       reqVO.setInNo(null);
       reqVO.setBizType(null);
       reqVO.setSourceType(null);
       reqVO.setSourceBizNo(null);
       reqVO.setSourceLineId(null);
       reqVO.setOriginType(null);
       reqVO.setOriginNo(null);
       reqVO.setArrivalId(null);
       reqVO.setSupplierId(null);
       reqVO.setCustomerId(null);
       reqVO.setProdOrderNo(null);
       reqVO.setWorkOrderNo(null);
       reqVO.setTransferNo(null);
       reqVO.setCountOrderNo(null);
       reqVO.setInvOrgId(null);
       reqVO.setPlantId(null);
       reqVO.setProjectId(null);
       reqVO.setProjectTaskId(null);
       reqVO.setWhId(null);
       reqVO.setWhCode(null);
       reqVO.setFromInvOrgId(null);
       reqVO.setFromWhId(null);
       reqVO.setNeedQc(null);
       reqVO.setIqcMode(null);
       reqVO.setQualityStatus(null);
       reqVO.setHookMode(null);
       reqVO.setStatus(null);
       reqVO.setIdempotencyKey(null);
       reqVO.setBizAttrsJson(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<InOrderRespVO> pageResult = inOrderService.getInOrderPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbInOrder, pageResult.getList().get(0));
    }

}