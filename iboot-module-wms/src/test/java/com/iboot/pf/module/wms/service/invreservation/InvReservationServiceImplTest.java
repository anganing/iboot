package com.iboot.pf.module.wms.service.invreservation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.invreservation.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.invreservation.InvReservationDO;
import com.iboot.pf.module.wms.dal.mysql.invreservation.InvReservationMapper;
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
 * {@link InvReservationServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(InvReservationServiceImpl.class)
public class InvReservationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InvReservationServiceImpl invReservationService;

    @Resource
    private InvReservationMapper invReservationMapper;

    @Test
    public void testCreateInvReservation_success() {
        // 准备参数
        InvReservationSaveReqVO createReqVO = randomPojo(InvReservationSaveReqVO.class).setId(null);

        // 调用
        Long invReservationId = invReservationService.createInvReservation(createReqVO);
        // 断言
        assertNotNull(invReservationId);
        // 校验记录的属性是否正确
        InvReservationDO invReservation = invReservationMapper.selectById(invReservationId);
        assertPojoEquals(createReqVO, invReservation, "id");
    }

    @Test
    public void testUpdateInvReservation_success() {
        // mock 数据
        InvReservationDO dbInvReservation = randomPojo(InvReservationDO.class);
        invReservationMapper.insert(dbInvReservation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InvReservationSaveReqVO updateReqVO = randomPojo(InvReservationSaveReqVO.class, o -> {
            o.setId(dbInvReservation.getId()); // 设置更新的 ID
        });

        // 调用
        invReservationService.updateInvReservation(updateReqVO);
        // 校验是否更新正确
        InvReservationDO invReservation = invReservationMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, invReservation);
    }

    @Test
    public void testUpdateInvReservation_notExists() {
        // 准备参数
        InvReservationSaveReqVO updateReqVO = randomPojo(InvReservationSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> invReservationService.updateInvReservation(updateReqVO), INV_RESERVATION_NOT_EXISTS);
    }

    @Test
    public void testDeleteInvReservation_success() {
        // mock 数据
        InvReservationDO dbInvReservation = randomPojo(InvReservationDO.class);
        invReservationMapper.insert(dbInvReservation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbInvReservation.getId();

        // 调用
        invReservationService.deleteInvReservation(id);
       // 校验数据不存在了
       assertNull(invReservationMapper.selectById(id));
    }

    @Test
    public void testDeleteInvReservation_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> invReservationService.deleteInvReservation(id), INV_RESERVATION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInvReservationPage() {
       // mock 数据
       InvReservationDO dbInvReservation = randomPojo(InvReservationDO.class, o -> { // 等会查询到
           o.setReservationNo(null);
           o.setBizType(null);
           o.setBizNo(null);
           o.setBizLineNo(null);
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
           o.setStatus(null);
           o.setExpireTime(null);
           o.setIdempotencyKey(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       invReservationMapper.insert(dbInvReservation);
       // 测试 reservationNo 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setReservationNo(null)));
       // 测试 bizType 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setBizType(null)));
       // 测试 bizNo 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setBizNo(null)));
       // 测试 bizLineNo 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setBizLineNo(null)));
       // 测试 invOrgId 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setInvOrgId(null)));
       // 测试 whId 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setWhId(null)));
       // 测试 locId 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setLocId(null)));
       // 测试 matId 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setMatId(null)));
       // 测试 batchId 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setBatchId(null)));
       // 测试 containerId 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setContainerId(null)));
       // 测试 projectId 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setProjectId(null)));
       // 测试 projectTaskId 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setProjectTaskId(null)));
       // 测试 qualityStatus 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setQualityStatus(null)));
       // 测试 qty 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setQty(null)));
       // 测试 releasedQty 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setReleasedQty(null)));
       // 测试 status 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setStatus(null)));
       // 测试 expireTime 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setExpireTime(null)));
       // 测试 idempotencyKey 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setIdempotencyKey(null)));
       // 测试 remark 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       invReservationMapper.insert(cloneIgnoreId(dbInvReservation, o -> o.setCreateTime(null)));
       // 准备参数
       InvReservationPageReqVO reqVO = new InvReservationPageReqVO();
       reqVO.setReservationNo(null);
       reqVO.setBizType(null);
       reqVO.setBizNo(null);
       reqVO.setBizLineNo(null);
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
       reqVO.setStatus(null);
       reqVO.setExpireTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setIdempotencyKey(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<InvReservationRespVO> pageResult = invReservationService.getInvReservationPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbInvReservation, pageResult.getList().get(0));
    }

}