package com.iboot.pf.module.wms.service.zone;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.zone.vo.ZoneSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsZoneDO;
import com.iboot.pf.module.wms.dal.mysql.zone.ZoneMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.module.wms.enums.WmsZoneTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ZONE_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.ZONE_CODE_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 库区 Service 测试：覆盖编码自动分配、业务唯一性、存在性校验与删除。
 */
@Import(ZoneServiceImpl.class)
class ZoneServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ZoneService zoneService;
    @Resource
    private ZoneMapper zoneMapper;

    @MockitoBean
    private WmsCodeService codeService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "ZN0001" : invocation.getArgument(1));
    }

    private ZoneSaveReqVO buildReqVO(String code) {
        ZoneSaveReqVO reqVO = new ZoneSaveReqVO();
        reqVO.setWhId(1L);
        reqVO.setCode(code);
        reqVO.setName("原料区");
        reqVO.setZoneType(WmsZoneTypeEnum.RAW.name());
        reqVO.setAllowMixedMat(true);
        reqVO.setAllowMixedLot(true);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateZone_blankCodeFilledByRule() {
        Long id = zoneService.createZone(buildReqVO(null));

        WmsZoneDO entity = zoneMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("ZN0001", entity.getCode());
    }

    @Test
    void testCreateZone_duplicateRejected() {
        zoneService.createZone(buildReqVO("ZN0-A"));

        // 数据库唯一索引是最终保证，Service 前置校验负责给出可读提示
        assertServiceException(() -> zoneService.createZone(buildReqVO("ZN0-A")), ZONE_CODE_DUPLICATE);
    }

    @Test
    void testUpdateZone_keepOwnCode() {
        Long id = zoneService.createZone(buildReqVO("ZN0-A"));

        // 改自己时业务编码没变，不应该撞到自己
        ZoneSaveReqVO updateReqVO = buildReqVO("ZN0-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("规则调整");
        zoneService.updateZone(updateReqVO);

        assertEquals("规则调整", zoneMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateZone_duplicateRejected() {
        zoneService.createZone(buildReqVO("ZN0-A"));
        Long secondId = zoneService.createZone(buildReqVO("ZN0-B"));

        ZoneSaveReqVO updateReqVO = buildReqVO("ZN0-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> zoneService.updateZone(updateReqVO), ZONE_CODE_DUPLICATE);
    }

    @Test
    void testUpdateZone_notExists() {
        ZoneSaveReqVO updateReqVO = buildReqVO("ZN0-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> zoneService.updateZone(updateReqVO), ZONE_NOT_EXISTS);
    }

    @Test
    void testDeleteZone_success() {
        Long id = zoneService.createZone(buildReqVO("ZN0-A"));

        zoneService.deleteZone(id);

        assertNull(zoneMapper.selectById(id));
    }

    @Test
    void testDeleteZone_notExists() {
        assertServiceException(() -> zoneService.deleteZone(randomLongId()), ZONE_NOT_EXISTS);
    }

}
