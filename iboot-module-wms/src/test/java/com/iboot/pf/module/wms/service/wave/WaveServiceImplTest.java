package com.iboot.pf.module.wms.service.wave;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.wave.vo.WaveSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.wave.WaveDO;
import com.iboot.pf.module.wms.dal.mysql.wave.WaveMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.enums.WmsWaveStatusEnum;
import com.iboot.pf.module.wms.enums.WmsWaveTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.WAVE_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.WAVE_NO_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 出库波次 Service 测试：覆盖单号自动分配、单号唯一、存在性校验。
 */
@Import(WaveServiceImpl.class)
class WaveServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WaveService waveService;
    @Resource
    private WaveMapper waveMapper;

    @MockitoBean
    private WmsCodeService codeService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "WV0001" : invocation.getArgument(1));
    }

    private WaveSaveReqVO buildReqVO(String waveNo) {
        WaveSaveReqVO reqVO = new WaveSaveReqVO();
        reqVO.setWaveNo(waveNo);
        reqVO.setWhId(1L);
        reqVO.setWaveType(WmsWaveTypeEnum.BY_ORDER.name());
        reqVO.setStatus(WmsWaveStatusEnum.DRAFT.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateWave_blankNoFilledByRule() {
        Long id = waveService.createWave(buildReqVO(null));

        WaveDO entity = waveMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("WV0001", entity.getWaveNo());
    }

    @Test
    void testCreateWave_duplicateNoRejected() {
        waveService.createWave(buildReqVO("WV0-A"));

        // 数据库唯一索引是最终保证，Service 前置校验负责给出可读提示
        assertServiceException(() -> waveService.createWave(buildReqVO("WV0-A")), WAVE_NO_DUPLICATE);
    }

    @Test
    void testUpdateWave_keepOwnNo() {
        Long id = waveService.createWave(buildReqVO("WV0-A"));

        WaveSaveReqVO updateReqVO = buildReqVO("WV0-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("单据备注调整");
        waveService.updateWave(updateReqVO);

        assertEquals("单据备注调整", waveMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateWave_duplicateNoRejected() {
        waveService.createWave(buildReqVO("WV0-A"));
        Long secondId = waveService.createWave(buildReqVO("WV0-B"));

        WaveSaveReqVO updateReqVO = buildReqVO("WV0-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> waveService.updateWave(updateReqVO), WAVE_NO_DUPLICATE);
    }

    @Test
    void testUpdateWave_notExists() {
        WaveSaveReqVO updateReqVO = buildReqVO("WV0-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> waveService.updateWave(updateReqVO), WAVE_NOT_EXISTS);
    }

    @Test
    void testDeleteWave_notExists() {
        assertServiceException(() -> waveService.deleteWave(randomLongId()), WAVE_NOT_EXISTS);
    }

    @Test
    void testDeleteWave_success() {
        Long id = waveService.createWave(buildReqVO("WV0-A"));

        waveService.deleteWave(id);

        assertNull(waveMapper.selectById(id));
    }

}
