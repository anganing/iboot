package com.iboot.pf.module.wms.service.arrival;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.arrival.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.arrival.ArrivalDO;
import com.iboot.pf.module.wms.dal.mysql.arrival.ArrivalMapper;
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
 * {@link ArrivalServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ArrivalServiceImpl.class)
public class ArrivalServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ArrivalServiceImpl arrivalService;

    @Resource
    private ArrivalMapper arrivalMapper;

    @Test
    public void testCreateArrival_success() {
        // 准备参数
        ArrivalSaveReqVO createReqVO = randomPojo(ArrivalSaveReqVO.class).setId(null);

        // 调用
        Long arrivalId = arrivalService.createArrival(createReqVO);
        // 断言
        assertNotNull(arrivalId);
        // 校验记录的属性是否正确
        ArrivalDO arrival = arrivalMapper.selectById(arrivalId);
        assertPojoEquals(createReqVO, arrival, "id");
    }

    @Test
    public void testUpdateArrival_success() {
        // mock 数据
        ArrivalDO dbArrival = randomPojo(ArrivalDO.class);
        arrivalMapper.insert(dbArrival);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ArrivalSaveReqVO updateReqVO = randomPojo(ArrivalSaveReqVO.class, o -> {
            o.setId(dbArrival.getId()); // 设置更新的 ID
        });

        // 调用
        arrivalService.updateArrival(updateReqVO);
        // 校验是否更新正确
        ArrivalDO arrival = arrivalMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, arrival);
    }

    @Test
    public void testUpdateArrival_notExists() {
        // 准备参数
        ArrivalSaveReqVO updateReqVO = randomPojo(ArrivalSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> arrivalService.updateArrival(updateReqVO), ARRIVAL_NOT_EXISTS);
    }

    @Test
    public void testDeleteArrival_success() {
        // mock 数据
        ArrivalDO dbArrival = randomPojo(ArrivalDO.class);
        arrivalMapper.insert(dbArrival);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbArrival.getId();

        // 调用
        arrivalService.deleteArrival(id);
       // 校验数据不存在了
       assertNull(arrivalMapper.selectById(id));
    }

    @Test
    public void testDeleteArrival_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> arrivalService.deleteArrival(id), ARRIVAL_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetArrivalPage() {
       // mock 数据
       ArrivalDO dbArrival = randomPojo(ArrivalDO.class, o -> { // 等会查询到
           o.setArrivalNo(null);
           o.setSourceType(null);
           o.setSourceNo(null);
           o.setSupplierId(null);
           o.setSupplierCode(null);
           o.setSupplierName(null);
           o.setInvOrgId(null);
           o.setPlantId(null);
           o.setWhId(null);
           o.setExpectDate(null);
           o.setArrivalTime(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       arrivalMapper.insert(dbArrival);
       // 测试 arrivalNo 不匹配
       arrivalMapper.insert(cloneIgnoreId(dbArrival, o -> o.setArrivalNo(null)));
       // 测试 sourceType 不匹配
       arrivalMapper.insert(cloneIgnoreId(dbArrival, o -> o.setSourceType(null)));
       // 测试 sourceNo 不匹配
       arrivalMapper.insert(cloneIgnoreId(dbArrival, o -> o.setSourceNo(null)));
       // 测试 supplierId 不匹配
       arrivalMapper.insert(cloneIgnoreId(dbArrival, o -> o.setSupplierId(null)));
       // 测试 supplierCode 不匹配
       arrivalMapper.insert(cloneIgnoreId(dbArrival, o -> o.setSupplierCode(null)));
       // 测试 supplierName 不匹配
       arrivalMapper.insert(cloneIgnoreId(dbArrival, o -> o.setSupplierName(null)));
       // 测试 invOrgId 不匹配
       arrivalMapper.insert(cloneIgnoreId(dbArrival, o -> o.setInvOrgId(null)));
       // 测试 plantId 不匹配
       arrivalMapper.insert(cloneIgnoreId(dbArrival, o -> o.setPlantId(null)));
       // 测试 whId 不匹配
       arrivalMapper.insert(cloneIgnoreId(dbArrival, o -> o.setWhId(null)));
       // 测试 expectDate 不匹配
       arrivalMapper.insert(cloneIgnoreId(dbArrival, o -> o.setExpectDate(null)));
       // 测试 arrivalTime 不匹配
       arrivalMapper.insert(cloneIgnoreId(dbArrival, o -> o.setArrivalTime(null)));
       // 测试 status 不匹配
       arrivalMapper.insert(cloneIgnoreId(dbArrival, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       arrivalMapper.insert(cloneIgnoreId(dbArrival, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       arrivalMapper.insert(cloneIgnoreId(dbArrival, o -> o.setCreateTime(null)));
       // 准备参数
       ArrivalPageReqVO reqVO = new ArrivalPageReqVO();
       reqVO.setArrivalNo(null);
       reqVO.setSourceType(null);
       reqVO.setSourceNo(null);
       reqVO.setSupplierId(null);
       reqVO.setSupplierCode(null);
       reqVO.setSupplierName(null);
       reqVO.setInvOrgId(null);
       reqVO.setPlantId(null);
       reqVO.setWhId(null);
       reqVO.setExpectDate(new java.time.LocalDate[]{java.time.LocalDate.of(2023, 2, 1), java.time.LocalDate.of(2023, 2, 28)});
       reqVO.setArrivalTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ArrivalRespVO> pageResult = arrivalService.getArrivalPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbArrival, pageResult.getList().get(0));
    }

}
