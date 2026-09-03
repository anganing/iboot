package com.iboot.pf.module.aps.service.version;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.aps.api.version.dto.ChangeVersionStatusReqDTO;
import com.iboot.pf.module.aps.api.version.dto.GenerateVersionReqDTO;
import com.iboot.pf.module.aps.api.version.dto.ReplaceVersionReqDTO;
import com.iboot.pf.module.aps.controller.admin.version.vo.EffectiveVersionPageReqVO;
import com.iboot.pf.module.aps.controller.admin.version.vo.VersionRespVO;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import com.iboot.pf.module.aps.dal.mysql.scenario.ApsScenarioMapper;
import com.iboot.pf.module.aps.dal.mysql.version.ApsVersionMapper;
import com.iboot.pf.module.aps.enums.ApsScenarioStatusEnum;
import com.iboot.pf.module.aps.enums.ApsSourceModuleEnum;
import com.iboot.pf.module.aps.enums.ApsVersionStatusEnum;
import com.iboot.pf.module.aps.service.command.ApsCommandServiceImpl;
import com.iboot.pf.module.aps.service.release.ApsScheduleReleaseService;
import com.iboot.pf.module.aps.service.snapshot.ApsRelationFiller;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_CLOSED;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.SCENARIO_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.VERSION_NOT_EXISTS;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.VERSION_PUBLISHED_READ_ONLY;
import static com.iboot.pf.module.aps.enums.ErrorCodeConstants.VERSION_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 排程版本 Service 测试。
 *
 * <p>覆盖版本号在场景内连续分配、状态机只允许文档里的迁移、同场景唯一已发布版本、
 * 已发布版本的排程结果只读，以及"排程结果"页的生效版本查询白名单。
 */
@Import({ApsVersionServiceImpl.class, ApsCommandServiceImpl.class})
class ApsVersionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ApsVersionService versionService;
    @Resource
    private ApsVersionMapper versionMapper;
    @Resource
    private ApsScenarioMapper scenarioMapper;

    @MockitoBean
    private ApsRelationFiller relationFiller;
    /** 下达要打 MES 门面，单测里只验证被调用一次，真实回写由 mes 侧的单测覆盖。 */
    @MockitoBean
    private ApsScheduleReleaseService releaseService;

    private Long insertScenario(String code, String status) {
        ApsScenarioDO scenario = ApsScenarioDO.builder()
                .code(code).name("八月排程").plantId(1L).workshopId(0L).strategyId(2L)
                .planStartTime(LocalDateTime.of(2026, 8, 1, 8, 0))
                .planEndTime(LocalDateTime.of(2026, 8, 31, 20, 0))
                .status(status)
                .build();
        scenarioMapper.insert(scenario);
        return scenario.getId();
    }

    private Long insertVersion(Long scenarioId, int versionNo, ApsVersionStatusEnum status) {
        ApsVersionDO version = ApsVersionDO.builder()
                .scenarioId(scenarioId).versionNo(versionNo).status(status.name()).version(0)
                .solveStartTime(LocalDateTime.now())
                .build();
        versionMapper.insert(version);
        return version.getId();
    }

    private GenerateVersionReqDTO buildGenerateCommand(String idempotencyKey, Long scenarioId) {
        GenerateVersionReqDTO reqDTO = new GenerateVersionReqDTO();
        reqDTO.setIdempotencyKey(idempotencyKey);
        reqDTO.setSourceModule(ApsSourceModuleEnum.MES.name());
        reqDTO.setSourceBizNo("MO202608001");
        reqDTO.setScenarioId(scenarioId);
        reqDTO.setRemark("单测数据");
        return reqDTO;
    }

    private ChangeVersionStatusReqDTO buildStatusCommand(String idempotencyKey, Long versionId) {
        ChangeVersionStatusReqDTO reqDTO = new ChangeVersionStatusReqDTO();
        reqDTO.setIdempotencyKey(idempotencyKey);
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(versionId));
        reqDTO.setVersionId(versionId);
        reqDTO.setOperatorId(1L);
        return reqDTO;
    }

    @Test
    void testGenerateVersion_versionNoIncreasesInScenario() {
        Long scenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.READY.name());

        Long firstId = versionService.generateVersion(buildGenerateCommand("MES:GEN:1", scenarioId));
        Long secondId = versionService.generateVersion(buildGenerateCommand("MES:GEN:2", scenarioId));

        assertEquals(1, versionMapper.selectById(firstId).getVersionNo());
        assertEquals(2, versionMapper.selectById(secondId).getVersionNo());
        assertEquals(ApsVersionStatusEnum.SOLVING.name(), versionMapper.selectById(secondId).getStatus());
    }

    @Test
    void testGenerateVersion_duplicateKeyReturnsFirstResult() {
        Long scenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.READY.name());

        Long first = versionService.generateVersion(buildGenerateCommand("MES:GEN:1", scenarioId));
        Long second = versionService.generateVersion(buildGenerateCommand("MES:GEN:1", scenarioId));

        assertEquals(first, second);
        assertEquals(1L, versionMapper.selectCountByScenarioId(scenarioId));
    }

    @Test
    void testGenerateVersion_scenarioNotExists() {
        assertServiceException(
                () -> versionService.generateVersion(buildGenerateCommand("MES:GEN:1", randomLongId())),
                SCENARIO_NOT_EXISTS);
    }

    @Test
    void testGenerateVersion_closedScenarioRejected() {
        Long scenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.CLOSED.name());

        assertServiceException(
                () -> versionService.generateVersion(buildGenerateCommand("MES:GEN:1", scenarioId)),
                SCENARIO_CLOSED);
    }

    @Test
    void testConfirmVersion_success() {
        Long scenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.READY.name());
        Long versionId = insertVersion(scenarioId, 1, ApsVersionStatusEnum.SOLVED);

        versionService.confirmVersion(buildStatusCommand("UI:CONFIRM:1", versionId));

        assertEquals(ApsVersionStatusEnum.CONFIRMED.name(), versionMapper.selectById(versionId).getStatus());
    }

    @Test
    void testConfirmVersion_failedStatusRejected() {
        Long scenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.READY.name());
        Long versionId = insertVersion(scenarioId, 1, ApsVersionStatusEnum.FAILED);

        // 求解失败的版本没有可确认的结果
        assertServiceException(() -> versionService.confirmVersion(buildStatusCommand("UI:CONFIRM:1", versionId)),
                VERSION_STATUS_INVALID);
    }

    @Test
    void testPublishVersion_replaceOldPublished() {
        Long scenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.READY.name());
        Long oldId = insertVersion(scenarioId, 1, ApsVersionStatusEnum.PUBLISHED);
        Long newId = insertVersion(scenarioId, 2, ApsVersionStatusEnum.CONFIRMED);

        versionService.publishVersion(buildStatusCommand("UI:PUBLISH:2", newId));

        // 同场景只允许一个已发布版本，旧的自动置为已被替换
        assertEquals(ApsVersionStatusEnum.REPLACED.name(), versionMapper.selectById(oldId).getStatus());
        ApsVersionDO published = versionMapper.selectById(newId);
        assertEquals(ApsVersionStatusEnum.PUBLISHED.name(), published.getStatus());
        assertNotNull(published.getPublishedTime());
        assertEquals(1L, published.getPublishedBy());
    }

    @Test
    void testPublishVersion_idempotent() {
        Long scenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.READY.name());
        Long versionId = insertVersion(scenarioId, 1, ApsVersionStatusEnum.CONFIRMED);

        versionService.publishVersion(buildStatusCommand("UI:PUBLISH:1", versionId));
        versionService.publishVersion(buildStatusCommand("UI:PUBLISH:1", versionId));

        assertEquals(ApsVersionStatusEnum.PUBLISHED.name(), versionMapper.selectById(versionId).getStatus());
        // 幂等键拦住了第二次，计划不会被重复下达
        verify(releaseService, times(1)).release(versionId);
    }

    @Test
    void testPublishVersion_releasesScheduleToMes() {
        Long scenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.READY.name());
        Long versionId = insertVersion(scenarioId, 1, ApsVersionStatusEnum.CONFIRMED);

        versionService.publishVersion(buildStatusCommand("UI:PUBLISH:1", versionId));

        // 发布即下达：计划必须回写到 MES，否则排程结果只躺在 aps_task 里，车间拿不到
        verify(releaseService).release(versionId);
    }

    @Test
    void testPublishVersion_unconfirmedRejected() {
        Long scenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.READY.name());
        Long versionId = insertVersion(scenarioId, 1, ApsVersionStatusEnum.SOLVING);

        assertServiceException(() -> versionService.publishVersion(buildStatusCommand("UI:PUBLISH:1", versionId)),
                VERSION_STATUS_INVALID);
    }

    @Test
    void testReplaceVersion_crossScenarioRejected() {
        Long firstScenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.READY.name());
        Long secondScenarioId = insertScenario("APS-SC-B", ApsScenarioStatusEnum.READY.name());
        Long oldId = insertVersion(firstScenarioId, 1, ApsVersionStatusEnum.PUBLISHED);
        Long newId = insertVersion(secondScenarioId, 1, ApsVersionStatusEnum.CONFIRMED);

        ReplaceVersionReqDTO reqDTO = new ReplaceVersionReqDTO();
        reqDTO.setIdempotencyKey("UI:REPLACE:1");
        reqDTO.setSourceModule(ApsSourceModuleEnum.UI.name());
        reqDTO.setSourceBizNo(String.valueOf(newId));
        reqDTO.setOldVersionId(oldId);
        reqDTO.setNewVersionId(newId);

        // 跨场景替换会把两个场景的计划混在一起
        assertServiceException(() -> versionService.replaceVersion(reqDTO), VERSION_STATUS_INVALID);
    }

    @Test
    void testValidateMutable_publishedReadOnly() {
        Long scenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.READY.name());
        Long versionId = insertVersion(scenarioId, 1, ApsVersionStatusEnum.PUBLISHED);

        assertServiceException(() -> versionService.validateMutable(versionId), VERSION_PUBLISHED_READ_ONLY);
    }

    @Test
    void testValidateMutable_notExists() {
        assertServiceException(() -> versionService.validateMutable(randomLongId()), VERSION_NOT_EXISTS);
    }

    private EffectiveVersionPageReqVO buildEffectivePageReq(String scenarioCode, String status) {
        EffectiveVersionPageReqVO reqVO = new EffectiveVersionPageReqVO();
        reqVO.setScenarioCode(scenarioCode);
        reqVO.setStatus(status);
        return reqVO;
    }

    @Test
    void testGetEffectiveVersionPage_defaultOnlyPublished() {
        Long scenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.SOLVED.name());
        insertVersion(scenarioId, 1, ApsVersionStatusEnum.REPLACED);
        Long publishedId = insertVersion(scenarioId, 2, ApsVersionStatusEnum.PUBLISHED);
        insertVersion(scenarioId, 3, ApsVersionStatusEnum.SOLVING);

        PageResult<VersionRespVO> pageResult =
                versionService.getEffectiveVersionPage(buildEffectivePageReq(null, null));

        // 状态留空只看当前生效的计划，已被替换与求解中都不出现
        assertEquals(1L, pageResult.getTotal());
        assertEquals(publishedId, pageResult.getList().get(0).getId());
    }

    @Test
    void testGetEffectiveVersionPage_replacedOnly() {
        Long scenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.SOLVED.name());
        Long replacedId = insertVersion(scenarioId, 1, ApsVersionStatusEnum.REPLACED);
        insertVersion(scenarioId, 2, ApsVersionStatusEnum.PUBLISHED);

        PageResult<VersionRespVO> pageResult =
                versionService.getEffectiveVersionPage(buildEffectivePageReq(null, "REPLACED"));

        assertEquals(1L, pageResult.getTotal());
        assertEquals(replacedId, pageResult.getList().get(0).getId());
    }

    @Test
    void testGetEffectiveVersionPage_whitelistFallsBackToPublished() {
        Long scenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.SOLVED.name());
        Long publishedId = insertVersion(scenarioId, 1, ApsVersionStatusEnum.PUBLISHED);
        insertVersion(scenarioId, 2, ApsVersionStatusEnum.SOLVING);
        insertVersion(scenarioId, 3, ApsVersionStatusEnum.PENDING_CONFIRM);

        PageResult<VersionRespVO> pageResult =
                versionService.getEffectiveVersionPage(buildEffectivePageReq(null, "SOLVING"));

        // 白名单外的状态回落成只查已发布，构造请求也拿不到还在评审中的版本
        assertEquals(1L, pageResult.getTotal());
        assertEquals(publishedId, pageResult.getList().get(0).getId());
    }

    @Test
    void testGetEffectiveVersionPage_scenarioCodeLike() {
        Long matchedScenarioId = insertScenario("APS-SC-MATCH", ApsScenarioStatusEnum.SOLVED.name());
        Long otherScenarioId = insertScenario("APS-SC-OTHER", ApsScenarioStatusEnum.SOLVED.name());
        Long matchedId = insertVersion(matchedScenarioId, 1, ApsVersionStatusEnum.PUBLISHED);
        insertVersion(otherScenarioId, 1, ApsVersionStatusEnum.PUBLISHED);

        PageResult<VersionRespVO> pageResult =
                versionService.getEffectiveVersionPage(buildEffectivePageReq("MATCH", null));

        assertEquals(1L, pageResult.getTotal());
        assertEquals(matchedId, pageResult.getList().get(0).getId());
    }

    @Test
    void testGetEffectiveVersionPage_fillsScenarioColumns() {
        Long scenarioId = insertScenario("APS-SC-A", ApsScenarioStatusEnum.SOLVED.name());
        insertVersion(scenarioId, 1, ApsVersionStatusEnum.PUBLISHED);

        VersionRespVO respVO = versionService
                .getEffectiveVersionPage(buildEffectivePageReq(null, null)).getList().get(0);

        // 场景的可读字段与计划窗口由 Mapper JOIN 出来，前端不做二次拼接
        assertEquals("APS-SC-A", respVO.getScenarioCode());
        assertEquals("八月排程", respVO.getScenarioName());
        assertEquals(1L, respVO.getPlantId());
        assertEquals(0L, respVO.getWorkshopId());
        assertEquals(LocalDateTime.of(2026, 8, 1, 8, 0), respVO.getPlanStartTime());
        assertEquals(LocalDateTime.of(2026, 8, 31, 20, 0), respVO.getPlanEndTime());
    }

}
