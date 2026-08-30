package com.iboot.pf.module.mes.service.proddemand;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandRespVO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.*;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandRespVO;
import com.iboot.pf.module.mes.dal.dataobject.proddemand.ProdDemandDO;
import com.iboot.pf.module.mes.controller.admin.proddemand.vo.ProdDemandRespVO;
import com.iboot.pf.module.mes.dal.mysql.proddemand.ProdDemandMapper;
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
 * {@link ProdDemandServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ProdDemandServiceImpl.class)
public class ProdDemandServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProdDemandServiceImpl prodDemandService;

    @Resource
    private ProdDemandMapper prodDemandMapper;

    @Test
    public void testCreateProdDemand_success() {
        // 准备参数
        ProdDemandSaveReqVO createReqVO = randomPojo(ProdDemandSaveReqVO.class).setId(null);

        // 调用
        Long prodDemandId = prodDemandService.createProdDemand(createReqVO);
        // 断言
        assertNotNull(prodDemandId);
        // 校验记录的属性是否正确
        ProdDemandDO prodDemand = prodDemandMapper.selectById(prodDemandId);
        assertPojoEquals(createReqVO, prodDemand, "id");
    }

    @Test
    public void testUpdateProdDemand_success() {
        // mock 数据
        ProdDemandDO dbProdDemand = randomPojo(ProdDemandDO.class);
        prodDemandMapper.insert(dbProdDemand);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ProdDemandSaveReqVO updateReqVO = randomPojo(ProdDemandSaveReqVO.class, o -> {
            o.setId(dbProdDemand.getId()); // 设置更新的 ID
        });

        // 调用
        prodDemandService.updateProdDemand(updateReqVO);
        // 校验是否更新正确
        ProdDemandDO prodDemand = prodDemandMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, prodDemand);
    }

    @Test
    public void testUpdateProdDemand_notExists() {
        // 准备参数
        ProdDemandSaveReqVO updateReqVO = randomPojo(ProdDemandSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> prodDemandService.updateProdDemand(updateReqVO), PROD_DEMAND_NOT_EXISTS);
    }

    @Test
    public void testDeleteProdDemand_success() {
        // mock 数据
        ProdDemandDO dbProdDemand = randomPojo(ProdDemandDO.class);
        prodDemandMapper.insert(dbProdDemand);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbProdDemand.getId();

        // 调用
        prodDemandService.deleteProdDemand(id);
       // 校验数据不存在了
       assertNull(prodDemandMapper.selectById(id));
    }

    @Test
    public void testDeleteProdDemand_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> prodDemandService.deleteProdDemand(id), PROD_DEMAND_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetProdDemandPage() {
       // mock 数据
       ProdDemandDO dbProdDemand = randomPojo(ProdDemandDO.class, o -> { // 等会查询到
           o.setDemandNo(null);
           o.setSourceType(null);
           o.setSourceNo(null);
           o.setPlantId(null);
           o.setProjectId(null);
           o.setProjectTaskId(null);
           o.setMatId(null);
           o.setMatCode(null);
           o.setMatName(null);
           o.setMatSpec(null);
           o.setQty(null);
           o.setUomId(null);
           o.setUomCode(null);
           o.setDueDate(null);
           o.setPriority(null);
           o.setStatus(null);
           o.setIdempotencyKey(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       prodDemandMapper.insert(dbProdDemand);
       // 测试 demandNo 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setDemandNo(null)));
       // 测试 sourceType 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setSourceType(null)));
       // 测试 sourceNo 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setSourceNo(null)));
       // 测试 plantId 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setPlantId(null)));
       // 测试 projectId 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setProjectId(null)));
       // 测试 projectTaskId 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setProjectTaskId(null)));
       // 测试 matId 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setMatId(null)));
       // 测试 matCode 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setMatCode(null)));
       // 测试 matName 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setMatName(null)));
       // 测试 matSpec 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setMatSpec(null)));
       // 测试 qty 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setQty(null)));
       // 测试 uomId 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setUomId(null)));
       // 测试 uomCode 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setUomCode(null)));
       // 测试 dueDate 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setDueDate(null)));
       // 测试 priority 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setPriority(null)));
       // 测试 status 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setStatus(null)));
       // 测试 idempotencyKey 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setIdempotencyKey(null)));
       // 测试 remark 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       prodDemandMapper.insert(cloneIgnoreId(dbProdDemand, o -> o.setCreateTime(null)));
       // 准备参数
       ProdDemandPageReqVO reqVO = new ProdDemandPageReqVO();
       reqVO.setDemandNo(null);
       reqVO.setSourceType(null);
       reqVO.setSourceNo(null);
       reqVO.setPlantId(null);
       reqVO.setProjectId(null);
       reqVO.setProjectTaskId(null);
       reqVO.setMatId(null);
       reqVO.setMatCode(null);
       reqVO.setMatName(null);
       reqVO.setMatSpec(null);
       reqVO.setQty(null);
       reqVO.setUomId(null);
       reqVO.setUomCode(null);
       reqVO.setDueDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setPriority(null);
       reqVO.setStatus(null);
       reqVO.setIdempotencyKey(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ProdDemandRespVO> pageResult = prodDemandService.getProdDemandPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbProdDemand, pageResult.getList().get(0));
    }

}