package com.iboot.pf.module.wms.service.strategy;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.wms.controller.admin.strategy.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.strategy.WmsStrategyDO;
import com.iboot.pf.module.wms.dal.mysql.strategy.WmsStrategyMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;

import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link WmsStrategyServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(WmsStrategyServiceImpl.class)
public class WmsStrategyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsStrategyServiceImpl strategyService;

    @Resource
    private WmsStrategyMapper strategyMapper;

    @Test
    public void testCreateStrategy_success() {
        // 准备参数
        StrategySaveReqVO createReqVO = randomPojo(StrategySaveReqVO.class).setId(null);

        // 调用
        Long strategyId = strategyService.createStrategy(createReqVO);
        // 断言
        assertNotNull(strategyId);
        // 校验记录的属性是否正确
        WmsStrategyDO strategy = strategyMapper.selectById(strategyId);
        assertPojoEquals(createReqVO, strategy, "id");
    }

    @Test
    public void testUpdateStrategy_success() {
        // mock 数据
        WmsStrategyDO dbStrategy = randomPojo(WmsStrategyDO.class);
        strategyMapper.insert(dbStrategy);// @Sql: 先插入出一条存在的数据
        // 准备参数
        StrategySaveReqVO updateReqVO = randomPojo(StrategySaveReqVO.class, o -> {
            o.setId(dbStrategy.getId()); // 设置更新的 ID
        });

        // 调用
        strategyService.updateStrategy(updateReqVO);
        // 校验是否更新正确
        WmsStrategyDO strategy = strategyMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, strategy);
    }

    @Test
    public void testUpdateStrategy_notExists() {
        // 准备参数
        StrategySaveReqVO updateReqVO = randomPojo(StrategySaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> strategyService.updateStrategy(updateReqVO), STRATEGY_NOT_EXISTS);
    }

    @Test
    public void testDeleteStrategy_success() {
        // mock 数据
        WmsStrategyDO dbStrategy = randomPojo(WmsStrategyDO.class);
        strategyMapper.insert(dbStrategy);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbStrategy.getId();

        // 调用
        strategyService.deleteStrategy(id);
       // 校验数据不存在了
       assertNull(strategyMapper.selectById(id));
    }

    @Test
    public void testDeleteStrategy_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> strategyService.deleteStrategy(id), STRATEGY_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetStrategyPage() {
       // mock 数据
       WmsStrategyDO dbStrategy = randomPojo(WmsStrategyDO.class, o -> { // 等会查询到
           o.setCode(null);
           o.setName(null);
           o.setStrategyType(null);
           o.setWhId(null);
           o.setVersionNo(null);
           o.setConfigJson(null);
           o.setPriority(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       strategyMapper.insert(dbStrategy);
       // 测试 code 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setCode(null)));
       // 测试 name 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setName(null)));
       // 测试 strategyType 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setStrategyType(null)));
       // 测试 whId 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setWhId(null)));
       // 测试 versionNo 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setVersionNo(null)));
       // 测试 configJson 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setConfigJson(null)));
       // 测试 priority 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setPriority(null)));
       // 测试 status 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setCreateTime(null)));
       // 准备参数
       StrategyPageReqVO reqVO = new StrategyPageReqVO();
       reqVO.setCode(null);
       reqVO.setName(null);
       reqVO.setStrategyType(null);
       reqVO.setWhId(null);
       reqVO.setVersionNo(null);
       reqVO.setConfigJson(null);
       reqVO.setPriority(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<StrategyRespVO> pageResult = strategyService.getStrategyPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbStrategy, pageResult.getList().get(0));
    }

}