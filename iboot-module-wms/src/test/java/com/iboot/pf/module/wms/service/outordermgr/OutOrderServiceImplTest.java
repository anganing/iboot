package com.iboot.pf.module.wms.service.outordermgr;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.outordermgr.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.outordermgr.OutOrderDO;
import com.iboot.pf.module.wms.dal.mysql.outordermgr.OutOrderMapper;
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
 * {@link OutOrderServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(OutOrderServiceImpl.class)
public class OutOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OutOrderServiceImpl outOrderService;

    @Resource
    private OutOrderMapper outOrderMapper;

    @Test
    public void testCreateOutOrder_success() {
        // 准备参数
        OutOrderSaveReqVO createReqVO = randomPojo(OutOrderSaveReqVO.class).setId(null);

        // 调用
        Long outOrderId = outOrderService.createOutOrder(createReqVO);
        // 断言
        assertNotNull(outOrderId);
        // 校验记录的属性是否正确
        OutOrderDO outOrder = outOrderMapper.selectById(outOrderId);
        assertPojoEquals(createReqVO, outOrder, "id");
    }

    @Test
    public void testUpdateOutOrder_success() {
        // mock 数据
        OutOrderDO dbOutOrder = randomPojo(OutOrderDO.class);
        outOrderMapper.insert(dbOutOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        OutOrderSaveReqVO updateReqVO = randomPojo(OutOrderSaveReqVO.class, o -> {
            o.setId(dbOutOrder.getId()); // 设置更新的 ID
        });

        // 调用
        outOrderService.updateOutOrder(updateReqVO);
        // 校验是否更新正确
        OutOrderDO outOrder = outOrderMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, outOrder);
    }

    @Test
    public void testUpdateOutOrder_notExists() {
        // 准备参数
        OutOrderSaveReqVO updateReqVO = randomPojo(OutOrderSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> outOrderService.updateOutOrder(updateReqVO), OUT_ORDER_NOT_EXISTS);
    }

    @Test
    public void testDeleteOutOrder_success() {
        // mock 数据
        OutOrderDO dbOutOrder = randomPojo(OutOrderDO.class);
        outOrderMapper.insert(dbOutOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbOutOrder.getId();

        // 调用
        outOrderService.deleteOutOrder(id);
       // 校验数据不存在了
       assertNull(outOrderMapper.selectById(id));
    }

    @Test
    public void testDeleteOutOrder_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> outOrderService.deleteOutOrder(id), OUT_ORDER_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOutOrderPage() {
       // mock 数据
       OutOrderDO dbOutOrder = randomPojo(OutOrderDO.class, o -> { // 等会查询到
           o.setOutNo(null);
           o.setBizType(null);
           o.setSourceType(null);
           o.setSourceBizNo(null);
           o.setSourceLineId(null);
           o.setOriginType(null);
           o.setOriginNo(null);
           o.setCustomerId(null);
           o.setSupplierId(null);
           o.setProdOrderNo(null);
           o.setWorkOrderNo(null);
           o.setOpTaskId(null);
           o.setMaterialReqNo(null);
           o.setTransferNo(null);
           o.setCountOrderNo(null);
           o.setInvOrgId(null);
           o.setPlantId(null);
           o.setProjectId(null);
           o.setProjectTaskId(null);
           o.setWhId(null);
           o.setWhCode(null);
           o.setToInvOrgId(null);
           o.setToWhId(null);
           o.setPickStrategyId(null);
           o.setHookMode(null);
           o.setStatus(null);
           o.setIdempotencyKey(null);
           o.setBizAttrsJson(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       outOrderMapper.insert(dbOutOrder);
       // 测试 outNo 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setOutNo(null)));
       // 测试 bizType 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setBizType(null)));
       // 测试 sourceType 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setSourceType(null)));
       // 测试 sourceBizNo 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setSourceBizNo(null)));
       // 测试 sourceLineId 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setSourceLineId(null)));
       // 测试 originType 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setOriginType(null)));
       // 测试 originNo 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setOriginNo(null)));
       // 测试 customerId 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setCustomerId(null)));
       // 测试 supplierId 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setSupplierId(null)));
       // 测试 prodOrderNo 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setProdOrderNo(null)));
       // 测试 workOrderNo 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setWorkOrderNo(null)));
       // 测试 opTaskId 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setOpTaskId(null)));
       // 测试 materialReqNo 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setMaterialReqNo(null)));
       // 测试 transferNo 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setTransferNo(null)));
       // 测试 countOrderNo 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setCountOrderNo(null)));
       // 测试 invOrgId 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setInvOrgId(null)));
       // 测试 plantId 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setPlantId(null)));
       // 测试 projectId 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setProjectId(null)));
       // 测试 projectTaskId 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setProjectTaskId(null)));
       // 测试 whId 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setWhId(null)));
       // 测试 whCode 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setWhCode(null)));
       // 测试 toInvOrgId 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setToInvOrgId(null)));
       // 测试 toWhId 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setToWhId(null)));
       // 测试 pickStrategyId 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setPickStrategyId(null)));
       // 测试 hookMode 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setHookMode(null)));
       // 测试 status 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setStatus(null)));
       // 测试 idempotencyKey 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setIdempotencyKey(null)));
       // 测试 bizAttrsJson 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setBizAttrsJson(null)));
       // 测试 remark 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       outOrderMapper.insert(cloneIgnoreId(dbOutOrder, o -> o.setCreateTime(null)));
       // 准备参数
       OutOrderPageReqVO reqVO = new OutOrderPageReqVO();
       reqVO.setOutNo(null);
       reqVO.setBizType(null);
       reqVO.setSourceType(null);
       reqVO.setSourceBizNo(null);
       reqVO.setSourceLineId(null);
       reqVO.setOriginType(null);
       reqVO.setOriginNo(null);
       reqVO.setCustomerId(null);
       reqVO.setSupplierId(null);
       reqVO.setProdOrderNo(null);
       reqVO.setWorkOrderNo(null);
       reqVO.setOpTaskId(null);
       reqVO.setMaterialReqNo(null);
       reqVO.setTransferNo(null);
       reqVO.setCountOrderNo(null);
       reqVO.setInvOrgId(null);
       reqVO.setPlantId(null);
       reqVO.setProjectId(null);
       reqVO.setProjectTaskId(null);
       reqVO.setWhId(null);
       reqVO.setWhCode(null);
       reqVO.setToInvOrgId(null);
       reqVO.setToWhId(null);
       reqVO.setPickStrategyId(null);
       reqVO.setHookMode(null);
       reqVO.setStatus(null);
       reqVO.setIdempotencyKey(null);
       reqVO.setBizAttrsJson(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<OutOrderRespVO> pageResult = outOrderService.getOutOrderPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbOutOrder, pageResult.getList().get(0));
    }

}