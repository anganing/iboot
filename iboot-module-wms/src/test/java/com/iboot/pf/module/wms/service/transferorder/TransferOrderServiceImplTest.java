package com.iboot.pf.module.wms.service.transferorder;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.transferorder.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.transferorder.TransferOrderDO;
import com.iboot.pf.module.wms.dal.mysql.transferorder.TransferOrderMapper;
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
 * {@link TransferOrderServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(TransferOrderServiceImpl.class)
public class TransferOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private TransferOrderServiceImpl transferOrderService;

    @Resource
    private TransferOrderMapper transferOrderMapper;

    @Test
    public void testCreateTransferOrder_success() {
        // 准备参数
        TransferOrderSaveReqVO createReqVO = randomPojo(TransferOrderSaveReqVO.class).setId(null);

        // 调用
        Long transferOrderId = transferOrderService.createTransferOrder(createReqVO);
        // 断言
        assertNotNull(transferOrderId);
        // 校验记录的属性是否正确
        TransferOrderDO transferOrder = transferOrderMapper.selectById(transferOrderId);
        assertPojoEquals(createReqVO, transferOrder, "id");
    }

    @Test
    public void testUpdateTransferOrder_success() {
        // mock 数据
        TransferOrderDO dbTransferOrder = randomPojo(TransferOrderDO.class);
        transferOrderMapper.insert(dbTransferOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        TransferOrderSaveReqVO updateReqVO = randomPojo(TransferOrderSaveReqVO.class, o -> {
            o.setId(dbTransferOrder.getId()); // 设置更新的 ID
        });

        // 调用
        transferOrderService.updateTransferOrder(updateReqVO);
        // 校验是否更新正确
        TransferOrderDO transferOrder = transferOrderMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, transferOrder);
    }

    @Test
    public void testUpdateTransferOrder_notExists() {
        // 准备参数
        TransferOrderSaveReqVO updateReqVO = randomPojo(TransferOrderSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> transferOrderService.updateTransferOrder(updateReqVO), TRANSFER_ORDER_NOT_EXISTS);
    }

    @Test
    public void testDeleteTransferOrder_success() {
        // mock 数据
        TransferOrderDO dbTransferOrder = randomPojo(TransferOrderDO.class);
        transferOrderMapper.insert(dbTransferOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbTransferOrder.getId();

        // 调用
        transferOrderService.deleteTransferOrder(id);
       // 校验数据不存在了
       assertNull(transferOrderMapper.selectById(id));
    }

    @Test
    public void testDeleteTransferOrder_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> transferOrderService.deleteTransferOrder(id), TRANSFER_ORDER_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetTransferOrderPage() {
       // mock 数据
       TransferOrderDO dbTransferOrder = randomPojo(TransferOrderDO.class, o -> { // 等会查询到
           o.setTransferNo(null);
           o.setTransferType(null);
           o.setSourceType(null);
           o.setSourceBizNo(null);
           o.setFromInvOrgId(null);
           o.setToInvOrgId(null);
           o.setFromWhId(null);
           o.setToWhId(null);
           o.setFromProjectId(null);
           o.setToProjectId(null);
           o.setHookMode(null);
           o.setStatus(null);
           o.setIdempotencyKey(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       transferOrderMapper.insert(dbTransferOrder);
       // 测试 transferNo 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setTransferNo(null)));
       // 测试 transferType 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setTransferType(null)));
       // 测试 sourceType 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setSourceType(null)));
       // 测试 sourceBizNo 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setSourceBizNo(null)));
       // 测试 fromInvOrgId 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setFromInvOrgId(null)));
       // 测试 toInvOrgId 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setToInvOrgId(null)));
       // 测试 fromWhId 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setFromWhId(null)));
       // 测试 toWhId 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setToWhId(null)));
       // 测试 fromProjectId 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setFromProjectId(null)));
       // 测试 toProjectId 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setToProjectId(null)));
       // 测试 hookMode 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setHookMode(null)));
       // 测试 status 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setStatus(null)));
       // 测试 idempotencyKey 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setIdempotencyKey(null)));
       // 测试 remark 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       transferOrderMapper.insert(cloneIgnoreId(dbTransferOrder, o -> o.setCreateTime(null)));
       // 准备参数
       TransferOrderPageReqVO reqVO = new TransferOrderPageReqVO();
       reqVO.setTransferNo(null);
       reqVO.setTransferType(null);
       reqVO.setSourceType(null);
       reqVO.setSourceBizNo(null);
       reqVO.setFromInvOrgId(null);
       reqVO.setToInvOrgId(null);
       reqVO.setFromWhId(null);
       reqVO.setToWhId(null);
       reqVO.setFromProjectId(null);
       reqVO.setToProjectId(null);
       reqVO.setHookMode(null);
       reqVO.setStatus(null);
       reqVO.setIdempotencyKey(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<TransferOrderRespVO> pageResult = transferOrderService.getTransferOrderPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbTransferOrder, pageResult.getList().get(0));
    }

}