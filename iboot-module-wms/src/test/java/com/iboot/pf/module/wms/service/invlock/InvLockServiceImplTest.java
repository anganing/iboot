package com.iboot.pf.module.wms.service.invlock;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.invlock.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.invlock.InvLockDO;
import com.iboot.pf.module.wms.dal.mysql.invlock.InvLockMapper;
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
 * {@link InvLockServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(InvLockServiceImpl.class)
public class InvLockServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InvLockServiceImpl invLockService;

    @Resource
    private InvLockMapper invLockMapper;

    @Test
    public void testCreateInvLock_success() {
        // 准备参数
        InvLockSaveReqVO createReqVO = randomPojo(InvLockSaveReqVO.class).setId(null);

        // 调用
        Long invLockId = invLockService.createInvLock(createReqVO);
        // 断言
        assertNotNull(invLockId);
        // 校验记录的属性是否正确
        InvLockDO invLock = invLockMapper.selectById(invLockId);
        assertPojoEquals(createReqVO, invLock, "id");
    }

    @Test
    public void testUpdateInvLock_success() {
        // mock 数据
        InvLockDO dbInvLock = randomPojo(InvLockDO.class);
        invLockMapper.insert(dbInvLock);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InvLockSaveReqVO updateReqVO = randomPojo(InvLockSaveReqVO.class, o -> {
            o.setId(dbInvLock.getId()); // 设置更新的 ID
        });

        // 调用
        invLockService.updateInvLock(updateReqVO);
        // 校验是否更新正确
        InvLockDO invLock = invLockMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, invLock);
    }

    @Test
    public void testUpdateInvLock_notExists() {
        // 准备参数
        InvLockSaveReqVO updateReqVO = randomPojo(InvLockSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> invLockService.updateInvLock(updateReqVO), INV_LOCK_NOT_EXISTS);
    }

    @Test
    public void testDeleteInvLock_success() {
        // mock 数据
        InvLockDO dbInvLock = randomPojo(InvLockDO.class);
        invLockMapper.insert(dbInvLock);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbInvLock.getId();

        // 调用
        invLockService.deleteInvLock(id);
       // 校验数据不存在了
       assertNull(invLockMapper.selectById(id));
    }

    @Test
    public void testDeleteInvLock_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> invLockService.deleteInvLock(id), INV_LOCK_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInvLockPage() {
       // mock 数据
       InvLockDO dbInvLock = randomPojo(InvLockDO.class, o -> { // 等会查询到
           o.setLockNo(null);
           o.setBizType(null);
           o.setBizNo(null);
           o.setInvOrgId(null);
           o.setWhId(null);
           o.setLocId(null);
           o.setMatId(null);
           o.setBatchId(null);
           o.setContainerId(null);
           o.setProjectId(null);
           o.setProjectTaskId(null);
           o.setQualityStatus(null);
           o.setQty(null);
           o.setReleasedQty(null);
           o.setReason(null);
           o.setStatus(null);
           o.setIdempotencyKey(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       invLockMapper.insert(dbInvLock);
       // 测试 lockNo 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setLockNo(null)));
       // 测试 bizType 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setBizType(null)));
       // 测试 bizNo 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setBizNo(null)));
       // 测试 invOrgId 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setInvOrgId(null)));
       // 测试 whId 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setWhId(null)));
       // 测试 locId 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setLocId(null)));
       // 测试 matId 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setMatId(null)));
       // 测试 batchId 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setBatchId(null)));
       // 测试 containerId 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setContainerId(null)));
       // 测试 projectId 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setProjectId(null)));
       // 测试 projectTaskId 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setProjectTaskId(null)));
       // 测试 qualityStatus 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setQualityStatus(null)));
       // 测试 qty 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setQty(null)));
       // 测试 releasedQty 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setReleasedQty(null)));
       // 测试 reason 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setReason(null)));
       // 测试 status 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setStatus(null)));
       // 测试 idempotencyKey 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setIdempotencyKey(null)));
       // 测试 remark 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       invLockMapper.insert(cloneIgnoreId(dbInvLock, o -> o.setCreateTime(null)));
       // 准备参数
       InvLockPageReqVO reqVO = new InvLockPageReqVO();
       reqVO.setLockNo(null);
       reqVO.setBizType(null);
       reqVO.setBizNo(null);
       reqVO.setInvOrgId(null);
       reqVO.setWhId(null);
       reqVO.setLocId(null);
       reqVO.setMatId(null);
       reqVO.setBatchId(null);
       reqVO.setContainerId(null);
       reqVO.setProjectId(null);
       reqVO.setProjectTaskId(null);
       reqVO.setQualityStatus(null);
       reqVO.setQty(null);
       reqVO.setReleasedQty(null);
       reqVO.setReason(null);
       reqVO.setStatus(null);
       reqVO.setIdempotencyKey(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<InvLockRespVO> pageResult = invLockService.getInvLockPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbInvLock, pageResult.getList().get(0));
    }

}