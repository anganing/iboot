package com.iboot.pf.module.aps.service.scenario;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;

import com.iboot.pf.module.aps.controller.admin.scenario.vo.*;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ScenarioDO;
import com.iboot.pf.module.aps.dal.mysql.scenario.ScenarioMapper;
import com.iboot.pf.framework.common.pojo.PageResult;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.*;
import static com.iboot.pf.framework.test.core.util.AssertUtils.*;
import static com.iboot.pf.framework.test.core.util.RandomUtils.*;
import static com.iboot.pf.framework.common.util.date.LocalDateTimeUtils.*;
import static com.iboot.pf.framework.common.util.object.ObjectUtils.*;
import static com.iboot.pf.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link ScenarioServiceImpl} 的单元测试类
 *
 * @author iboot
 */
@Import(ScenarioServiceImpl.class)
public class ScenarioServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ScenarioServiceImpl scenarioService;

    @Resource
    private ScenarioMapper scenarioMapper;

    @Test
    public void testCreateScenario_success() {
        // 准备参数
        ScenarioSaveReqVO createReqVO = randomPojo(ScenarioSaveReqVO.class).setId(null);

        // 调用
        Long scenarioId = scenarioService.createScenario(createReqVO);
        // 断言
        assertNotNull(scenarioId);
        // 校验记录的属性是否正确
        ScenarioDO scenario = scenarioMapper.selectById(scenarioId);
        assertPojoEquals(createReqVO, scenario, "id");
    }

    @Test
    public void testUpdateScenario_success() {
        // mock 数据
        ScenarioDO dbScenario = randomPojo(ScenarioDO.class);
        scenarioMapper.insert(dbScenario);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ScenarioSaveReqVO updateReqVO = randomPojo(ScenarioSaveReqVO.class, o -> {
            o.setId(dbScenario.getId()); // 设置更新的 ID
        });

        // 调用
        scenarioService.updateScenario(updateReqVO);
        // 校验是否更新正确
        ScenarioDO scenario = scenarioMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, scenario);
    }

    @Test
    public void testUpdateScenario_notExists() {
        // 准备参数
        ScenarioSaveReqVO updateReqVO = randomPojo(ScenarioSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> scenarioService.updateScenario(updateReqVO), SCENARIO_NOT_EXISTS);
    }

    @Test
    public void testDeleteScenario_success() {
        // mock 数据
        ScenarioDO dbScenario = randomPojo(ScenarioDO.class);
        scenarioMapper.insert(dbScenario);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbScenario.getId();

        // 调用
        scenarioService.deleteScenario(id);
       // 校验数据不存在了
       assertNull(scenarioMapper.selectById(id));
    }

    @Test
    public void testDeleteScenario_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> scenarioService.deleteScenario(id), SCENARIO_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetScenarioPage() {
       // mock 数据
       ScenarioDO dbScenario = randomPojo(ScenarioDO.class, o -> { // 等会查询到
           o.setCode(null);
           o.setName(null);
           o.setPlantId(null);
           o.setWorkshopId(null);
           o.setStrategyId(null);
           o.setPlanStartTime(null);
           o.setPlanEndTime(null);
           o.setSnapshotTime(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       scenarioMapper.insert(dbScenario);
       // 测试 code 不匹配
       scenarioMapper.insert(cloneIgnoreId(dbScenario, o -> o.setCode(null)));
       // 测试 name 不匹配
       scenarioMapper.insert(cloneIgnoreId(dbScenario, o -> o.setName(null)));
       // 测试 plantId 不匹配
       scenarioMapper.insert(cloneIgnoreId(dbScenario, o -> o.setPlantId(null)));
       // 测试 workshopId 不匹配
       scenarioMapper.insert(cloneIgnoreId(dbScenario, o -> o.setWorkshopId(null)));
       // 测试 strategyId 不匹配
       scenarioMapper.insert(cloneIgnoreId(dbScenario, o -> o.setStrategyId(null)));
       // 测试 planStartTime 不匹配
       scenarioMapper.insert(cloneIgnoreId(dbScenario, o -> o.setPlanStartTime(null)));
       // 测试 planEndTime 不匹配
       scenarioMapper.insert(cloneIgnoreId(dbScenario, o -> o.setPlanEndTime(null)));
       // 测试 snapshotTime 不匹配
       scenarioMapper.insert(cloneIgnoreId(dbScenario, o -> o.setSnapshotTime(null)));
       // 测试 status 不匹配
       scenarioMapper.insert(cloneIgnoreId(dbScenario, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       scenarioMapper.insert(cloneIgnoreId(dbScenario, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       scenarioMapper.insert(cloneIgnoreId(dbScenario, o -> o.setCreateTime(null)));
       // 准备参数
       ScenarioPageReqVO reqVO = new ScenarioPageReqVO();
       reqVO.setCode(null);
       reqVO.setName(null);
       reqVO.setPlantId(null);
       reqVO.setWorkshopId(null);
       reqVO.setStrategyId(null);
       reqVO.setPlanStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setPlanEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSnapshotTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ScenarioRespVO> pageResult = scenarioService.getScenarioPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbScenario, pageResult.getList().get(0));
    }

}