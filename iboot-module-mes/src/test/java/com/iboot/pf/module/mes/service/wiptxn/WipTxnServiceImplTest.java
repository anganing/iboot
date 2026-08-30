package com.iboot.pf.module.mes.service.wiptxn;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.WipTxnRespVO;
import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.*;
import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.WipTxnRespVO;
import com.iboot.pf.module.mes.dal.dataobject.wiptxn.WipTxnDO;
import com.iboot.pf.module.mes.controller.admin.wiptxn.vo.WipTxnRespVO;
import com.iboot.pf.module.mes.dal.mysql.wiptxn.WipTxnMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static com.iboot.pf.module.mes.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static com.iboot.pf.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link WipTxnServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(WipTxnServiceImpl.class)
public class WipTxnServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WipTxnServiceImpl wipTxnService;

    @Resource
    private WipTxnMapper wipTxnMapper;

    @Test
    public void testCreateWipTxn_success() {
        // 准备参数
        WipTxnSaveReqVO createReqVO = randomPojo(WipTxnSaveReqVO.class).setId(null);

        // 调用
        Long wipTxnId = wipTxnService.createWipTxn(createReqVO);
        // 断言
        assertNotNull(wipTxnId);
        // 校验记录的属性是否正确
        WipTxnDO wipTxn = wipTxnMapper.selectById(wipTxnId);
        assertPojoEquals(createReqVO, wipTxn, "id");
    }

    @Test
    public void testUpdateWipTxn_success() {
        // mock 数据
        WipTxnDO dbWipTxn = randomPojo(WipTxnDO.class);
        wipTxnMapper.insert(dbWipTxn);// @Sql: 先插入出一条存在的数据
        // 准备参数
        WipTxnSaveReqVO updateReqVO = randomPojo(WipTxnSaveReqVO.class, o -> {
            o.setId(dbWipTxn.getId()); // 设置更新的 ID
        });

        // 调用
        wipTxnService.updateWipTxn(updateReqVO);
        // 校验是否更新正确
        WipTxnDO wipTxn = wipTxnMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, wipTxn);
    }

    @Test
    public void testUpdateWipTxn_notExists() {
        // 准备参数
        WipTxnSaveReqVO updateReqVO = randomPojo(WipTxnSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> wipTxnService.updateWipTxn(updateReqVO), WIP_TXN_NOT_EXISTS);
    }

    @Test
    public void testDeleteWipTxn_success() {
        // mock 数据
        WipTxnDO dbWipTxn = randomPojo(WipTxnDO.class);
        wipTxnMapper.insert(dbWipTxn);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbWipTxn.getId();

        // 调用
        wipTxnService.deleteWipTxn(id);
       // 校验数据不存在了
       assertNull(wipTxnMapper.selectById(id));
    }

    @Test
    public void testDeleteWipTxn_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> wipTxnService.deleteWipTxn(id), WIP_TXN_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetWipTxnPage() {
       // mock 数据
       WipTxnDO dbWipTxn = randomPojo(WipTxnDO.class, o -> { // 等会查询到
           o.setTxnNo(null);
           o.setProdOrderId(null);
           o.setWorkOrderId(null);
           o.setOpTaskId(null);
           o.setTxnType(null);
           o.setQty(null);
           o.setFromStatus(null);
           o.setToStatus(null);
           o.setBizType(null);
           o.setBizNo(null);
           o.setFromOpTaskId(null);
           o.setToOpTaskId(null);
           o.setTxnTime(null);
           o.setIdempotencyKey(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       wipTxnMapper.insert(dbWipTxn);
       // 测试 txnNo 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setTxnNo(null)));
       // 测试 prodOrderId 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setProdOrderId(null)));
       // 测试 workOrderId 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setWorkOrderId(null)));
       // 测试 opTaskId 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setOpTaskId(null)));
       // 测试 txnType 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setTxnType(null)));
       // 测试 qty 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setQty(null)));
       // 测试 fromStatus 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setFromStatus(null)));
       // 测试 toStatus 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setToStatus(null)));
       // 测试 bizType 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setBizType(null)));
       // 测试 bizNo 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setBizNo(null)));
       // 测试 fromOpTaskId 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setFromOpTaskId(null)));
       // 测试 toOpTaskId 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setToOpTaskId(null)));
       // 测试 txnTime 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setTxnTime(null)));
       // 测试 idempotencyKey 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setIdempotencyKey(null)));
       // 测试 remark 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       wipTxnMapper.insert(cloneIgnoreId(dbWipTxn, o -> o.setCreateTime(null)));
       // 准备参数
       WipTxnPageReqVO reqVO = new WipTxnPageReqVO();
       reqVO.setTxnNo(null);
       reqVO.setProdOrderId(null);
       reqVO.setWorkOrderId(null);
       reqVO.setOpTaskId(null);
       reqVO.setTxnType(null);
       reqVO.setQty(null);
       reqVO.setFromStatus(null);
       reqVO.setToStatus(null);
       reqVO.setBizType(null);
       reqVO.setBizNo(null);
       reqVO.setFromOpTaskId(null);
       reqVO.setToOpTaskId(null);
       reqVO.setTxnTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setIdempotencyKey(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<WipTxnRespVO> pageResult = wipTxnService.getWipTxnPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbWipTxn, pageResult.getList().get(0));
    }

}