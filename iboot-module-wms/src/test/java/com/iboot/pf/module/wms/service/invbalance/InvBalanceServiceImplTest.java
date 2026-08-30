package com.iboot.pf.module.wms.service.invbalance;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.invbalance.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.invbalance.InvBalanceDO;
import com.iboot.pf.module.wms.dal.mysql.invbalance.InvBalanceMapper;
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
 * {@link InvBalanceServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(InvBalanceServiceImpl.class)
public class InvBalanceServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InvBalanceServiceImpl invBalanceService;

    @Resource
    private InvBalanceMapper invBalanceMapper;

    @Test
    public void testCreateInvBalance_success() {
        // 准备参数
        InvBalanceSaveReqVO createReqVO = randomPojo(InvBalanceSaveReqVO.class).setId(null);

        // 调用
        Long invBalanceId = invBalanceService.createInvBalance(createReqVO);
        // 断言
        assertNotNull(invBalanceId);
        // 校验记录的属性是否正确
        InvBalanceDO invBalance = invBalanceMapper.selectById(invBalanceId);
        assertPojoEquals(createReqVO, invBalance, "id");
    }

    @Test
    public void testUpdateInvBalance_success() {
        // mock 数据
        InvBalanceDO dbInvBalance = randomPojo(InvBalanceDO.class);
        invBalanceMapper.insert(dbInvBalance);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InvBalanceSaveReqVO updateReqVO = randomPojo(InvBalanceSaveReqVO.class, o -> {
            o.setId(dbInvBalance.getId()); // 设置更新的 ID
        });

        // 调用
        invBalanceService.updateInvBalance(updateReqVO);
        // 校验是否更新正确
        InvBalanceDO invBalance = invBalanceMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, invBalance);
    }

    @Test
    public void testUpdateInvBalance_notExists() {
        // 准备参数
        InvBalanceSaveReqVO updateReqVO = randomPojo(InvBalanceSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> invBalanceService.updateInvBalance(updateReqVO), INV_BALANCE_NOT_EXISTS);
    }

    @Test
    public void testDeleteInvBalance_success() {
        // mock 数据
        InvBalanceDO dbInvBalance = randomPojo(InvBalanceDO.class);
        invBalanceMapper.insert(dbInvBalance);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbInvBalance.getId();

        // 调用
        invBalanceService.deleteInvBalance(id);
       // 校验数据不存在了
       assertNull(invBalanceMapper.selectById(id));
    }

    @Test
    public void testDeleteInvBalance_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> invBalanceService.deleteInvBalance(id), INV_BALANCE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInvBalancePage() {
       // mock 数据
       InvBalanceDO dbInvBalance = randomPojo(InvBalanceDO.class, o -> { // 等会查询到
           o.setInvOrgId(null);
           o.setPlantId(null);
           o.setWhId(null);
           o.setLocId(null);
           o.setMatId(null);
           o.setBatchId(null);
           o.setContainerId(null);
           o.setProjectId(null);
           o.setProjectTaskId(null);
           o.setStockScope(null);
           o.setQualityStatus(null);
           o.setQtyOnhand(null);
           o.setQtyReserved(null);
           o.setQtyFrozen(null);
           o.setMatCode(null);
           o.setMatName(null);
           o.setMatSpec(null);
           o.setUomCode(null);
           o.setLotNo(null);
           o.setWhCode(null);
           o.setLocCode(null);
           o.setVersion(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       invBalanceMapper.insert(dbInvBalance);
       // 测试 invOrgId 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setInvOrgId(null)));
       // 测试 plantId 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setPlantId(null)));
       // 测试 whId 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setWhId(null)));
       // 测试 locId 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setLocId(null)));
       // 测试 matId 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setMatId(null)));
       // 测试 batchId 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setBatchId(null)));
       // 测试 containerId 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setContainerId(null)));
       // 测试 projectId 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setProjectId(null)));
       // 测试 projectTaskId 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setProjectTaskId(null)));
       // 测试 stockScope 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setStockScope(null)));
       // 测试 qualityStatus 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setQualityStatus(null)));
       // 测试 qtyOnhand 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setQtyOnhand(null)));
       // 测试 qtyReserved 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setQtyReserved(null)));
       // 测试 qtyFrozen 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setQtyFrozen(null)));
       // 测试 matCode 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setMatCode(null)));
       // 测试 matName 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setMatName(null)));
       // 测试 matSpec 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setMatSpec(null)));
       // 测试 uomCode 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setUomCode(null)));
       // 测试 lotNo 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setLotNo(null)));
       // 测试 whCode 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setWhCode(null)));
       // 测试 locCode 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setLocCode(null)));
       // 测试 version 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setVersion(null)));
       // 测试 remark 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       invBalanceMapper.insert(cloneIgnoreId(dbInvBalance, o -> o.setCreateTime(null)));
       // 准备参数
       InvBalancePageReqVO reqVO = new InvBalancePageReqVO();
       reqVO.setInvOrgId(null);
       reqVO.setPlantId(null);
       reqVO.setWhId(null);
       reqVO.setLocId(null);
       reqVO.setMatId(null);
       reqVO.setBatchId(null);
       reqVO.setContainerId(null);
       reqVO.setProjectId(null);
       reqVO.setProjectTaskId(null);
       reqVO.setStockScope(null);
       reqVO.setQualityStatus(null);
       reqVO.setQtyOnhand(null);
       reqVO.setQtyReserved(null);
       reqVO.setQtyFrozen(null);
       reqVO.setMatCode(null);
       reqVO.setMatName(null);
       reqVO.setMatSpec(null);
       reqVO.setUomCode(null);
       reqVO.setLotNo(null);
       reqVO.setWhCode(null);
       reqVO.setLocCode(null);
       reqVO.setVersion(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<InvBalanceRespVO> pageResult = invBalanceService.getInvBalancePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbInvBalance, pageResult.getList().get(0));
    }

}