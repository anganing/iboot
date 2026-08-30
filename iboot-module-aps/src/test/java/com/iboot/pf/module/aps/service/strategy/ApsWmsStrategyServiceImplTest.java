package com.iboot.pf.module.aps.service.strategy;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.aps.controller.admin.strategy.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.strategy.ApsStrategyDO;
import com.iboot.pf.module.aps.dal.mysql.strategy.ApsStrategyMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;

import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ApsStrategyServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ApsStrategyServiceImpl.class)
public class ApsWmsStrategyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ApsStrategyServiceImpl strategyService;

    @Resource
    private ApsStrategyMapper strategyMapper;

    @Test
    public void testCreateStrategy_success() {
        // 准备参数
        StrategySaveReqVO createReqVO = randomPojo(StrategySaveReqVO.class).setId(null);

        // 调用
        Long strategyId = strategyService.createStrategy(createReqVO);
        // 断言
        assertNotNull(strategyId);
        // 校验记录的属性是否正确
        ApsStrategyDO strategy = strategyMapper.selectById(strategyId);
        assertPojoEquals(createReqVO, strategy, "id");
    }

    @Test
    public void testUpdateStrategy_success() {
        // mock 数据
        ApsStrategyDO dbStrategy = randomPojo(ApsStrategyDO.class);
        strategyMapper.insert(dbStrategy);// @Sql: 先插入出一条存在的数据
        // 准备参数
        StrategySaveReqVO updateReqVO = randomPojo(StrategySaveReqVO.class, o -> {
            o.setId(dbStrategy.getId()); // 设置更新的 ID
        });

        // 调用
        strategyService.updateStrategy(updateReqVO);
        // 校验是否更新正确
        ApsStrategyDO strategy = strategyMapper.selectById(updateReqVO.getId()); // 获取最新的
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
        ApsStrategyDO dbStrategy = randomPojo(ApsStrategyDO.class);
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
       ApsStrategyDO dbStrategy = randomPojo(ApsStrategyDO.class, o -> { // 等会查询到
           o.setCode(null);
           o.setName(null);
           o.setVersionNo(null);
           o.setObjective(null);
           o.setConfigJson(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       strategyMapper.insert(dbStrategy);
       // 测试 code 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setCode(null)));
       // 测试 name 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setName(null)));
       // 测试 versionNo 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setVersionNo(null)));
       // 测试 objective 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setObjective(null)));
       // 测试 configJson 不匹配
       strategyMapper.insert(cloneIgnoreId(dbStrategy, o -> o.setConfigJson(null)));
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
       reqVO.setVersionNo(null);
       reqVO.setObjective(null);
       reqVO.setConfigJson(null);
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