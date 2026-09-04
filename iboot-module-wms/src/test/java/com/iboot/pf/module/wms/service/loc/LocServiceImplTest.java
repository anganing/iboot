package com.iboot.pf.module.wms.service.loc;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.loc.vo.LocSaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.warehouse.WmsLocDO;
import com.iboot.pf.module.wms.dal.mysql.loc.LocMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.enums.WmsDimensionCheckModeEnum;
import com.iboot.pf.module.wms.enums.WmsLocStatusEnum;
import com.iboot.pf.module.wms.enums.WmsLocTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LOC_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.LOC_CODE_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 库位 Service 测试：覆盖编码自动分配、业务唯一性、存在性校验与删除。
 */
@Import(LocServiceImpl.class)
class LocServiceImplTest extends BaseDbUnitTest {

    @Resource
    private LocService locService;
    @Resource
    private LocMapper locMapper;

    @MockitoBean
    private WmsCodeService codeService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "LOC0001" : invocation.getArgument(1));
    }

    private LocSaveReqVO buildReqVO(String code) {
        LocSaveReqVO reqVO = new LocSaveReqVO();
        reqVO.setWhId(1L);
        reqVO.setZoneId(1L);
        reqVO.setCode(code);
        reqVO.setLocType(WmsLocTypeEnum.ASRS_CELL.name());
        reqVO.setDimensionCheckMode(WmsDimensionCheckModeEnum.NONE.name());
        reqVO.setStatus(WmsLocStatusEnum.EMPTY.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateLoc_blankCodeFilledByRule() {
        Long id = locService.createLoc(buildReqVO(null));

        WmsLocDO entity = locMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("LOC0001", entity.getCode());
    }

    @Test
    void testCreateLoc_duplicateRejected() {
        locService.createLoc(buildReqVO("LOC-A"));

        // 数据库唯一索引是最终保证，Service 前置校验负责给出可读提示
        assertServiceException(() -> locService.createLoc(buildReqVO("LOC-A")), LOC_CODE_DUPLICATE);
    }

    @Test
    void testUpdateLoc_keepOwnCode() {
        Long id = locService.createLoc(buildReqVO("LOC-A"));

        // 改自己时业务编码没变，不应该撞到自己
        LocSaveReqVO updateReqVO = buildReqVO("LOC-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("规则调整");
        locService.updateLoc(updateReqVO);

        assertEquals("规则调整", locMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateLoc_duplicateRejected() {
        locService.createLoc(buildReqVO("LOC-A"));
        Long secondId = locService.createLoc(buildReqVO("LOC-B"));

        LocSaveReqVO updateReqVO = buildReqVO("LOC-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> locService.updateLoc(updateReqVO), LOC_CODE_DUPLICATE);
    }

    @Test
    void testUpdateLoc_notExists() {
        LocSaveReqVO updateReqVO = buildReqVO("LOC-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> locService.updateLoc(updateReqVO), LOC_NOT_EXISTS);
    }

    @Test
    void testDeleteLoc_success() {
        Long id = locService.createLoc(buildReqVO("LOC-A"));

        locService.deleteLoc(id);

        assertNull(locMapper.selectById(id));
    }

    @Test
    void testDeleteLoc_notExists() {
        assertServiceException(() -> locService.deleteLoc(randomLongId()), LOC_NOT_EXISTS);
    }

}
