package com.iboot.pf.module.wms.service.invtxn;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.invtxn.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.invtxn.InvTxnDO;
import com.iboot.pf.module.wms.dal.mysql.invtxn.InvTxnMapper;
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
 * {@link InvTxnServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(InvTxnServiceImpl.class)
public class InvTxnServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InvTxnServiceImpl invTxnService;

    @Resource
    private InvTxnMapper invTxnMapper;

    @Test
    public void testCreateInvTxn_success() {
        // 准备参数
        InvTxnSaveReqVO createReqVO = randomPojo(InvTxnSaveReqVO.class).setId(null);

        // 调用
        Long invTxnId = invTxnService.createInvTxn(createReqVO);
        // 断言
        assertNotNull(invTxnId);
        // 校验记录的属性是否正确
        InvTxnDO invTxn = invTxnMapper.selectById(invTxnId);
        assertPojoEquals(createReqVO, invTxn, "id");
    }

    @Test
    public void testUpdateInvTxn_success() {
        // mock 数据
        InvTxnDO dbInvTxn = randomPojo(InvTxnDO.class);
        invTxnMapper.insert(dbInvTxn);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InvTxnSaveReqVO updateReqVO = randomPojo(InvTxnSaveReqVO.class, o -> {
            o.setId(dbInvTxn.getId()); // 设置更新的 ID
        });

        // 调用
        invTxnService.updateInvTxn(updateReqVO);
        // 校验是否更新正确
        InvTxnDO invTxn = invTxnMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, invTxn);
    }

    @Test
    public void testUpdateInvTxn_notExists() {
        // 准备参数
        InvTxnSaveReqVO updateReqVO = randomPojo(InvTxnSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> invTxnService.updateInvTxn(updateReqVO), INV_TXN_NOT_EXISTS);
    }

    @Test
    public void testDeleteInvTxn_success() {
        // mock 数据
        InvTxnDO dbInvTxn = randomPojo(InvTxnDO.class);
        invTxnMapper.insert(dbInvTxn);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbInvTxn.getId();

        // 调用
        invTxnService.deleteInvTxn(id);
       // 校验数据不存在了
       assertNull(invTxnMapper.selectById(id));
    }

    @Test
    public void testDeleteInvTxn_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> invTxnService.deleteInvTxn(id), INV_TXN_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInvTxnPage() {
       // mock 数据
       InvTxnDO dbInvTxn = randomPojo(InvTxnDO.class, o -> { // 等会查询到
           o.setTxnNo(null);
           o.setTxnType(null);
           o.setBizType(null);
           o.setBizNo(null);
           o.setBizLineNo(null);
           o.setIoType(null);
           o.setInvOrgId(null);
           o.setPlantId(null);
           o.setWhId(null);
           o.setFromLocId(null);
           o.setToLocId(null);
           o.setMatId(null);
           o.setBatchId(null);
           o.setContainerId(null);
           o.setUomId(null);
           o.setQty(null);
           o.setBeforeQty(null);
           o.setAfterQty(null);
           o.setQualityStatusFrom(null);
           o.setQualityStatusTo(null);
           o.setProjectIdFrom(null);
           o.setProjectTaskIdFrom(null);
           o.setProjectIdTo(null);
           o.setProjectTaskIdTo(null);
           o.setMatCode(null);
           o.setMatName(null);
           o.setUomCode(null);
           o.setLotNo(null);
           o.setFromLocCode(null);
           o.setToLocCode(null);
           o.setIdempotencyKey(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       invTxnMapper.insert(dbInvTxn);
       // 测试 txnNo 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setTxnNo(null)));
       // 测试 txnType 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setTxnType(null)));
       // 测试 bizType 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setBizType(null)));
       // 测试 bizNo 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setBizNo(null)));
       // 测试 bizLineNo 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setBizLineNo(null)));
       // 测试 ioType 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setIoType(null)));
       // 测试 invOrgId 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setInvOrgId(null)));
       // 测试 plantId 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setPlantId(null)));
       // 测试 whId 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setWhId(null)));
       // 测试 fromLocId 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setFromLocId(null)));
       // 测试 toLocId 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setToLocId(null)));
       // 测试 matId 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setMatId(null)));
       // 测试 batchId 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setBatchId(null)));
       // 测试 containerId 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setContainerId(null)));
       // 测试 uomId 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setUomId(null)));
       // 测试 qty 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setQty(null)));
       // 测试 beforeQty 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setBeforeQty(null)));
       // 测试 afterQty 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setAfterQty(null)));
       // 测试 qualityStatusFrom 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setQualityStatusFrom(null)));
       // 测试 qualityStatusTo 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setQualityStatusTo(null)));
       // 测试 projectIdFrom 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setProjectIdFrom(null)));
       // 测试 projectTaskIdFrom 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setProjectTaskIdFrom(null)));
       // 测试 projectIdTo 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setProjectIdTo(null)));
       // 测试 projectTaskIdTo 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setProjectTaskIdTo(null)));
       // 测试 matCode 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setMatCode(null)));
       // 测试 matName 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setMatName(null)));
       // 测试 uomCode 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setUomCode(null)));
       // 测试 lotNo 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setLotNo(null)));
       // 测试 fromLocCode 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setFromLocCode(null)));
       // 测试 toLocCode 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setToLocCode(null)));
       // 测试 idempotencyKey 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setIdempotencyKey(null)));
       // 测试 remark 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       invTxnMapper.insert(cloneIgnoreId(dbInvTxn, o -> o.setCreateTime(null)));
       // 准备参数
       InvTxnPageReqVO reqVO = new InvTxnPageReqVO();
       reqVO.setTxnNo(null);
       reqVO.setTxnType(null);
       reqVO.setBizType(null);
       reqVO.setBizNo(null);
       reqVO.setBizLineNo(null);
       reqVO.setIoType(null);
       reqVO.setInvOrgId(null);
       reqVO.setPlantId(null);
       reqVO.setWhId(null);
       reqVO.setFromLocId(null);
       reqVO.setToLocId(null);
       reqVO.setMatId(null);
       reqVO.setBatchId(null);
       reqVO.setContainerId(null);
       reqVO.setUomId(null);
       reqVO.setQty(null);
       reqVO.setBeforeQty(null);
       reqVO.setAfterQty(null);
       reqVO.setQualityStatusFrom(null);
       reqVO.setQualityStatusTo(null);
       reqVO.setProjectIdFrom(null);
       reqVO.setProjectTaskIdFrom(null);
       reqVO.setProjectIdTo(null);
       reqVO.setProjectTaskIdTo(null);
       reqVO.setMatCode(null);
       reqVO.setMatName(null);
       reqVO.setUomCode(null);
       reqVO.setLotNo(null);
       reqVO.setFromLocCode(null);
       reqVO.setToLocCode(null);
       reqVO.setIdempotencyKey(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<InvTxnRespVO> pageResult = invTxnService.getInvTxnPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbInvTxn, pageResult.getList().get(0));
    }

}