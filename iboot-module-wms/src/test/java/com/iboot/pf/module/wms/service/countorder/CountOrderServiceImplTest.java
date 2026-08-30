package com.iboot.pf.module.wms.service.countorder;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.countorder.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.countorder.CountOrderDO;
import com.iboot.pf.module.wms.dal.mysql.countorder.CountOrderMapper;
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
 * {@link CountOrderServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(CountOrderServiceImpl.class)
public class CountOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CountOrderServiceImpl countOrderService;

    @Resource
    private CountOrderMapper countOrderMapper;

    @Test
    public void testCreateCountOrder_success() {
        // 准备参数
        CountOrderSaveReqVO createReqVO = randomPojo(CountOrderSaveReqVO.class).setId(null);

        // 调用
        Long countOrderId = countOrderService.createCountOrder(createReqVO);
        // 断言
        assertNotNull(countOrderId);
        // 校验记录的属性是否正确
        CountOrderDO countOrder = countOrderMapper.selectById(countOrderId);
        assertPojoEquals(createReqVO, countOrder, "id");
    }

    @Test
    public void testUpdateCountOrder_success() {
        // mock 数据
        CountOrderDO dbCountOrder = randomPojo(CountOrderDO.class);
        countOrderMapper.insert(dbCountOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        CountOrderSaveReqVO updateReqVO = randomPojo(CountOrderSaveReqVO.class, o -> {
            o.setId(dbCountOrder.getId()); // 设置更新的 ID
        });

        // 调用
        countOrderService.updateCountOrder(updateReqVO);
        // 校验是否更新正确
        CountOrderDO countOrder = countOrderMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, countOrder);
    }

    @Test
    public void testUpdateCountOrder_notExists() {
        // 准备参数
        CountOrderSaveReqVO updateReqVO = randomPojo(CountOrderSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> countOrderService.updateCountOrder(updateReqVO), COUNT_ORDER_NOT_EXISTS);
    }

    @Test
    public void testDeleteCountOrder_success() {
        // mock 数据
        CountOrderDO dbCountOrder = randomPojo(CountOrderDO.class);
        countOrderMapper.insert(dbCountOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbCountOrder.getId();

        // 调用
        countOrderService.deleteCountOrder(id);
       // 校验数据不存在了
       assertNull(countOrderMapper.selectById(id));
    }

    @Test
    public void testDeleteCountOrder_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> countOrderService.deleteCountOrder(id), COUNT_ORDER_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetCountOrderPage() {
       // mock 数据
       CountOrderDO dbCountOrder = randomPojo(CountOrderDO.class, o -> { // 等会查询到
           o.setCountNo(null);
           o.setCountType(null);
           o.setInvOrgId(null);
           o.setWhId(null);
           o.setZoneId(null);
           o.setProjectId(null);
           o.setSnapshotTime(null);
           o.setBlindCountFlag(null);
           o.setStatus(null);
           o.setIdempotencyKey(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       countOrderMapper.insert(dbCountOrder);
       // 测试 countNo 不匹配
       countOrderMapper.insert(cloneIgnoreId(dbCountOrder, o -> o.setCountNo(null)));
       // 测试 countType 不匹配
       countOrderMapper.insert(cloneIgnoreId(dbCountOrder, o -> o.setCountType(null)));
       // 测试 invOrgId 不匹配
       countOrderMapper.insert(cloneIgnoreId(dbCountOrder, o -> o.setInvOrgId(null)));
       // 测试 whId 不匹配
       countOrderMapper.insert(cloneIgnoreId(dbCountOrder, o -> o.setWhId(null)));
       // 测试 zoneId 不匹配
       countOrderMapper.insert(cloneIgnoreId(dbCountOrder, o -> o.setZoneId(null)));
       // 测试 projectId 不匹配
       countOrderMapper.insert(cloneIgnoreId(dbCountOrder, o -> o.setProjectId(null)));
       // 测试 snapshotTime 不匹配
       countOrderMapper.insert(cloneIgnoreId(dbCountOrder, o -> o.setSnapshotTime(null)));
       // 测试 blindCountFlag 不匹配
       countOrderMapper.insert(cloneIgnoreId(dbCountOrder, o -> o.setBlindCountFlag(null)));
       // 测试 status 不匹配
       countOrderMapper.insert(cloneIgnoreId(dbCountOrder, o -> o.setStatus(null)));
       // 测试 idempotencyKey 不匹配
       countOrderMapper.insert(cloneIgnoreId(dbCountOrder, o -> o.setIdempotencyKey(null)));
       // 测试 remark 不匹配
       countOrderMapper.insert(cloneIgnoreId(dbCountOrder, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       countOrderMapper.insert(cloneIgnoreId(dbCountOrder, o -> o.setCreateTime(null)));
       // 准备参数
       CountOrderPageReqVO reqVO = new CountOrderPageReqVO();
       reqVO.setCountNo(null);
       reqVO.setCountType(null);
       reqVO.setInvOrgId(null);
       reqVO.setWhId(null);
       reqVO.setZoneId(null);
       reqVO.setProjectId(null);
       reqVO.setSnapshotTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setBlindCountFlag(null);
       reqVO.setStatus(null);
       reqVO.setIdempotencyKey(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<CountOrderRespVO> pageResult = countOrderService.getCountOrderPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbCountOrder, pageResult.getList().get(0));
    }

}