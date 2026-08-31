package com.iboot.pf.module.aps.service.scenario;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.aps.api.scenario.dto.CloseScenarioReqDTO;
import com.iboot.pf.module.aps.api.scenario.dto.CopyScenarioReqDTO;
import com.iboot.pf.module.aps.api.scenario.dto.CreateScenarioReqDTO;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.ScenarioSaveReqVO;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import com.iboot.pf.module.aps.dal.mysql.scenario.ApsScenarioMapper;
import com.iboot.pf.module.aps.dal.mysql.version.ApsVersionMapper;
import com.iboot.pf.module.aps.enums.ApsScenarioStatusEnum;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.enums.ApsVersionStatusEnum;
import com.iboot.pf.module.aps.service.coderule.ApsCodeService;
import com.iboot.pf.module.aps.service.command.ApsCommandServiceImpl;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import com.iboot.pf.module.aps.service.snapshot.ApsSnapshotService;
import com.iboot.pf.module.aps.service.strategy.ApsStrategyService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_CLOSED;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_CODE_DUPLICATE;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_HAS_VERSION;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_TIME_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 排程场景 Service 测试。
 *
 * <p>覆盖编码自动分配、编码唯一、计划窗口校验、已生成版本时的删除拦截，
 * 以及跨模块命令的幂等（同一幂等键重复下发返回首次结果）。
 */
@Import({ApsScenarioServiceImpl.class, ApsCommandServiceImpl.class})
class ApsScenarioServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ApsScenarioService scenarioService;
    @Resource
    private ApsScenarioMapper scenarioMapper;
    @Resource
    private ApsVersionMapper versionMapper;

    @MockitoBean
    private ApsCodeService codeService;
    @MockitoBean
    private ApsStrategyService strategyService;
    @MockitoBean
    private ApsSnapshotService snapshotService;
    @MockitoBean
    private ApsRelationFiller relationFiller;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "APS-SC-0001" : invocation.getArgument(1));
    }

    private ScenarioSaveReqVO buildReqVO(String code) {
        ScenarioSaveReqVO reqVO = new ScenarioSaveReqVO();
        reqVO.setCode(code);
        reqVO.setName("八月排程");
        reqVO.setPlantId(1L);
        reqVO.setWorkshopId(0L);
        reqVO.setStrategyId(2L);
        reqVO.setPlanStartTime(LocalDateTime.of(2026, 8, 1, 8, 0));
        reqVO.setPlanEndTime(LocalDateTime.of(2026, 8, 31, 20, 0));
        reqVO.setStatus(ApsScenarioStatusEnum.DRAFT.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    private CreateScenarioReqDTO buildCommand(String idempotencyKey, String code) {
        CreateScenarioReqDTO reqDTO = new CreateScenarioReqDTO();
        reqDTO.setIdempotencyKey(idempotencyKey);
        reqDTO.setSourceModule(ApsSourceModuleEnum.MES.name());
        reqDTO.setSourceBizNo("MO202608001");
        reqDTO.setCode(code);
        reqDTO.setName("MES 下发排程");
        reqDTO.setPlantId(1L);
        reqDTO.setStrategyId(2L);
        reqDTO.setPlanStartTime(LocalDateTime.of(2026, 8, 1, 8, 0));
        reqDTO.setPlanEndTime(LocalDateTime.of(2026, 8, 31, 20, 0));
        return reqDTO;
    }

    @Test
    void testCreateScenario_blankCodeFilledByRule() {
        Long id = scenarioService.createScenario(buildReqVO(null));

        ApsScenarioDO entity = scenarioMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("APS-SC-0001", entity.getCode());
    }

    @Test
    void testCreateScenario_duplicateCodeRejected() {
        scenarioService.createScenario(buildReqVO("APS-SC-A"));

        assertServiceException(() -> scenarioService.createScenario(buildReqVO("APS-SC-A")),
                SCENARIO_CODE_DUPLICATE);
    }

    @Test
    void testCreateScenario_invalidPlanWindowRejected() {
        ScenarioSaveReqVO reqVO = buildReqVO("APS-SC-A");
        reqVO.setPlanEndTime(reqVO.getPlanStartTime());

        // 起止时间相等意味着零长度时间轴，求解器无处安放任务
        assertServiceException(() -> scenarioService.createScenario(reqVO), SCENARIO_TIME_INVALID);
    }

    @Test
    void testUpdateScenario_keepOwnCodeAndSnapshotTime() {
        Long id = scenarioService.createScenario(buildReqVO("APS-SC-A"));
        LocalDateTime snapshotTime = LocalDateTime.of(2026, 7, 31, 23, 0);
        scenarioMapper.updateById(ApsScenarioDO.builder().id(id).snapshotTime(snapshotTime).build());

        ScenarioSaveReqVO updateReqVO = buildReqVO("APS-SC-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("窗口顺延");
        scenarioService.updateScenario(updateReqVO);

        ApsScenarioDO entity = scenarioMapper.selectById(id);
        assertEquals("窗口顺延", entity.getRemark());
        // 编码与快照时间不由前端改
        assertEquals("APS-SC-A", entity.getCode());
        assertEquals(snapshotTime, entity.getSnapshotTime());
    }

    @Test
    void testUpdateScenario_closedRejected() {
        Long id = scenarioService.createScenario(buildReqVO("APS-SC-A"));
        scenarioMapper.updateById(ApsScenarioDO.builder()
                .id(id).status(ApsScenarioStatusEnum.CLOSED.name()).build());

        ScenarioSaveReqVO updateReqVO = buildReqVO("APS-SC-A");
        updateReqVO.setId(id);
        assertServiceException(() -> scenarioService.updateScenario(updateReqVO), SCENARIO_CLOSED);
    }

    @Test
    void testUpdateScenario_notExists() {
        ScenarioSaveReqVO updateReqVO = buildReqVO("APS-SC-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> scenarioService.updateScenario(updateReqVO), SCENARIO_NOT_EXISTS);
    }

    @Test
    void testDeleteScenario_success() {
        Long id = scenarioService.createScenario(buildReqVO("APS-SC-A"));

        scenarioService.deleteScenario(id);

        assertNull(scenarioMapper.selectById(id));
    }

    @Test
    void testDeleteScenario_hasVersionRejected() {
        Long id = scenarioService.createScenario(buildReqVO("APS-SC-A"));
        versionMapper.insert(ApsVersionDO.builder()
                .scenarioId(id).versionNo(1).status(ApsVersionStatusEnum.SOLVING.name()).version(0).build());

        // 场景删掉后版本会指向不存在的场景
        assertServiceException(() -> scenarioService.deleteScenario(id), SCENARIO_HAS_VERSION);
    }

    @Test
    void testCloseScenario_idempotent() {
        Long id = scenarioService.createScenario(buildReqVO("APS-SC-A"));

        CloseScenarioReqDTO reqDTO = new CloseScenarioReqDTO();
        reqDTO.setScenarioId(id);
        reqDTO.setIdempotencyKey("MES:CLOSE:1");
        reqDTO.setSourceModule(ApsSourceModuleEnum.MES.name());
        reqDTO.setSourceBizNo("MO202608001");
        scenarioService.closeScenario(reqDTO);
        // 重复下发同一条命令不应该报错
        scenarioService.closeScenario(reqDTO);

        assertEquals(ApsScenarioStatusEnum.CLOSED.name(), scenarioMapper.selectById(id).getStatus());
    }

    @Test
    void testCreateScenarioCommand_duplicateKeyReturnsFirstResult() {
        Long first = scenarioService.createScenario(buildCommand("MES:CREATE:1", "APS-SC-A"));
        // 幂等键相同：直接返回首次结果，不再新建场景
        Long second = scenarioService.createScenario(buildCommand("MES:CREATE:1", "APS-SC-B"));

        assertEquals(first, second);
        assertNull(scenarioMapper.selectByCode("APS-SC-B"));
    }

    @Test
    void testCreateScenarioCommand_defaultsWorkshopAndStatus() {
        Long id = scenarioService.createScenario(buildCommand("MES:CREATE:2", "APS-SC-C"));

        ApsScenarioDO entity = scenarioMapper.selectById(id);
        // 车间可选，落 0 表示全工厂；MES 下发的场景直接进入待求解
        assertEquals(0L, entity.getWorkshopId());
        assertEquals(ApsScenarioStatusEnum.READY.name(), entity.getStatus());
    }

    @Test
    void testCopyScenario_resetSnapshotAndStatus() {
        Long sourceId = scenarioService.createScenario(buildReqVO("APS-SC-A"));
        scenarioMapper.updateById(ApsScenarioDO.builder()
                .id(sourceId).snapshotTime(LocalDateTime.now())
                .status(ApsScenarioStatusEnum.SOLVED.name()).build());

        CopyScenarioReqDTO reqDTO = new CopyScenarioReqDTO();
        reqDTO.setIdempotencyKey("MES:COPY:1");
        reqDTO.setSourceModule(ApsSourceModuleEnum.MES.name());
        reqDTO.setSourceBizNo("MO202608001");
        reqDTO.setSourceScenarioId(sourceId);
        reqDTO.setCode("APS-SC-COPY");
        reqDTO.setName("八月排程副本");
        Long targetId = scenarioService.copyScenario(reqDTO);

        ApsScenarioDO target = scenarioMapper.selectById(targetId);
        assertEquals("APS-SC-COPY", target.getCode());
        // 副本是新场景：输入要重新冻结，状态回到待求解
        assertNull(target.getSnapshotTime());
        assertEquals(ApsScenarioStatusEnum.READY.name(), target.getStatus());
    }

}
