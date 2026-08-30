package com.iboot.pf.module.mes.service.prodorder;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderRespVO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.*;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderRespVO;
import com.iboot.pf.module.mes.dal.dataobject.prodorder.ProdOrderDO;
import com.iboot.pf.module.mes.controller.admin.prodorder.vo.ProdOrderRespVO;
import com.iboot.pf.module.mes.dal.mysql.prodorder.ProdOrderMapper;
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
 * {@link ProdOrderServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ProdOrderServiceImpl.class)
public class ProdOrderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProdOrderServiceImpl prodOrderService;

    @Resource
    private ProdOrderMapper prodOrderMapper;

    @Test
    public void testCreateProdOrder_success() {
        // 准备参数
        ProdOrderSaveReqVO createReqVO = randomPojo(ProdOrderSaveReqVO.class).setId(null);

        // 调用
        Long prodOrderId = prodOrderService.createProdOrder(createReqVO);
        // 断言
        assertNotNull(prodOrderId);
        // 校验记录的属性是否正确
        ProdOrderDO prodOrder = prodOrderMapper.selectById(prodOrderId);
        assertPojoEquals(createReqVO, prodOrder, "id");
    }

    @Test
    public void testUpdateProdOrder_success() {
        // mock 数据
        ProdOrderDO dbProdOrder = randomPojo(ProdOrderDO.class);
        prodOrderMapper.insert(dbProdOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ProdOrderSaveReqVO updateReqVO = randomPojo(ProdOrderSaveReqVO.class, o -> {
            o.setId(dbProdOrder.getId()); // 设置更新的 ID
        });

        // 调用
        prodOrderService.updateProdOrder(updateReqVO);
        // 校验是否更新正确
        ProdOrderDO prodOrder = prodOrderMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, prodOrder);
    }

    @Test
    public void testUpdateProdOrder_notExists() {
        // 准备参数
        ProdOrderSaveReqVO updateReqVO = randomPojo(ProdOrderSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> prodOrderService.updateProdOrder(updateReqVO), PROD_ORDER_NOT_EXISTS);
    }

    @Test
    public void testDeleteProdOrder_success() {
        // mock 数据
        ProdOrderDO dbProdOrder = randomPojo(ProdOrderDO.class);
        prodOrderMapper.insert(dbProdOrder);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbProdOrder.getId();

        // 调用
        prodOrderService.deleteProdOrder(id);
       // 校验数据不存在了
       assertNull(prodOrderMapper.selectById(id));
    }

    @Test
    public void testDeleteProdOrder_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> prodOrderService.deleteProdOrder(id), PROD_ORDER_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetProdOrderPage() {
       // mock 数据
       ProdOrderDO dbProdOrder = randomPojo(ProdOrderDO.class, o -> { // 等会查询到
           o.setOrderNo(null);
           o.setDemandId(null);
           o.setPlantId(null);
           o.setWorkshopId(null);
           o.setProjectId(null);
           o.setProjectTaskId(null);
           o.setMatId(null);
           o.setMatCode(null);
           o.setMatName(null);
           o.setMatSpec(null);
           o.setQty(null);
           o.setCompletedQty(null);
           o.setScrapQty(null);
           o.setUomId(null);
           o.setUomCode(null);
           o.setBomId(null);
           o.setBomVersion(null);
           o.setRouteId(null);
           o.setRouteVersion(null);
           o.setDueDate(null);
           o.setPriority(null);
           o.setKitStatus(null);
           o.setStatus(null);
           o.setReleaseTime(null);
           o.setActualStartTime(null);
           o.setActualEndTime(null);
           o.setVersion(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       prodOrderMapper.insert(dbProdOrder);
       // 测试 orderNo 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setOrderNo(null)));
       // 测试 demandId 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setDemandId(null)));
       // 测试 plantId 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setPlantId(null)));
       // 测试 workshopId 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setWorkshopId(null)));
       // 测试 projectId 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setProjectId(null)));
       // 测试 projectTaskId 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setProjectTaskId(null)));
       // 测试 matId 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setMatId(null)));
       // 测试 matCode 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setMatCode(null)));
       // 测试 matName 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setMatName(null)));
       // 测试 matSpec 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setMatSpec(null)));
       // 测试 qty 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setQty(null)));
       // 测试 completedQty 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setCompletedQty(null)));
       // 测试 scrapQty 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setScrapQty(null)));
       // 测试 uomId 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setUomId(null)));
       // 测试 uomCode 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setUomCode(null)));
       // 测试 bomId 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setBomId(null)));
       // 测试 bomVersion 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setBomVersion(null)));
       // 测试 routeId 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setRouteId(null)));
       // 测试 routeVersion 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setRouteVersion(null)));
       // 测试 dueDate 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setDueDate(null)));
       // 测试 priority 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setPriority(null)));
       // 测试 kitStatus 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setKitStatus(null)));
       // 测试 status 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setStatus(null)));
       // 测试 releaseTime 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setReleaseTime(null)));
       // 测试 actualStartTime 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setActualStartTime(null)));
       // 测试 actualEndTime 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setActualEndTime(null)));
       // 测试 version 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setVersion(null)));
       // 测试 remark 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       prodOrderMapper.insert(cloneIgnoreId(dbProdOrder, o -> o.setCreateTime(null)));
       // 准备参数
       ProdOrderPageReqVO reqVO = new ProdOrderPageReqVO();
       reqVO.setOrderNo(null);
       reqVO.setDemandId(null);
       reqVO.setPlantId(null);
       reqVO.setWorkshopId(null);
       reqVO.setProjectId(null);
       reqVO.setProjectTaskId(null);
       reqVO.setMatId(null);
       reqVO.setMatCode(null);
       reqVO.setMatName(null);
       reqVO.setMatSpec(null);
       reqVO.setQty(null);
       reqVO.setCompletedQty(null);
       reqVO.setScrapQty(null);
       reqVO.setUomId(null);
       reqVO.setUomCode(null);
       reqVO.setBomId(null);
       reqVO.setBomVersion(null);
       reqVO.setRouteId(null);
       reqVO.setRouteVersion(null);
       reqVO.setDueDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setPriority(null);
       reqVO.setKitStatus(null);
       reqVO.setStatus(null);
       reqVO.setReleaseTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setActualStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setActualEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setVersion(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ProdOrderRespVO> pageResult = prodOrderService.getProdOrderPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbProdOrder, pageResult.getList().get(0));
    }

}