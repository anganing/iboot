package com.iboot.pf.module.wms.service.strategy;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.strategy.vo.StrategySaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.strategy.WmsStrategyDO;
import com.iboot.pf.module.wms.dal.mysql.strategy.WmsStrategyMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.enums.WmsStrategyStatusEnum;
import com.iboot.pf.module.wms.enums.WmsStrategyTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_CODE_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 仓储策略 Service 测试：覆盖编码自动分配、业务唯一性、存在性校验与删除。
 */
@Import(WmsStrategyServiceImpl.class)
class WmsStrategyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private WmsStrategyService wmsStrategyService;
    @Resource
    private WmsStrategyMapper wmsStrategyMapper;

    @MockitoBean
    private WmsCodeService codeService;

    @BeforeEach
    void mockExternalDependencies() {
        // 编码规则由 infra 提供：调用方填了就沿用，没填才由规则分配
        when(codeService.generate(anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == null
                        ? "WST0001" : invocation.getArgument(1));
    }

    private StrategySaveReqVO buildReqVO(String code) {
        StrategySaveReqVO reqVO = new StrategySaveReqVO();
        reqVO.setCode(code);
        reqVO.setName("默认上架策略");
        reqVO.setStrategyType(WmsStrategyTypeEnum.PUTAWAY.name());
        reqVO.setWhId(1L);
        reqVO.setVersionNo("V1");
        reqVO.setConfigJson("{}");
        reqVO.setPriority(10);
        reqVO.setStatus(WmsStrategyStatusEnum.ENABLED.name());
        reqVO.setRemark("单测数据");
        return reqVO;
    }

    @Test
    void testCreateStrategy_blankCodeFilledByRule() {
        Long id = wmsStrategyService.createStrategy(buildReqVO(null));

        WmsStrategyDO entity = wmsStrategyMapper.selectById(id);
        assertNotNull(entity);
        assertEquals("WST0001", entity.getCode());
    }

    @Test
    void testCreateStrategy_duplicateRejected() {
        wmsStrategyService.createStrategy(buildReqVO("WST-A"));

        // 数据库唯一索引是最终保证，Service 前置校验负责给出可读提示
        assertServiceException(() -> wmsStrategyService.createStrategy(buildReqVO("WST-A")), STRATEGY_CODE_DUPLICATE);
    }

    @Test
    void testUpdateStrategy_keepOwnCode() {
        Long id = wmsStrategyService.createStrategy(buildReqVO("WST-A"));

        // 改自己时业务编码没变，不应该撞到自己
        StrategySaveReqVO updateReqVO = buildReqVO("WST-A");
        updateReqVO.setId(id);
        updateReqVO.setRemark("规则调整");
        wmsStrategyService.updateStrategy(updateReqVO);

        assertEquals("规则调整", wmsStrategyMapper.selectById(id).getRemark());
    }

    @Test
    void testUpdateStrategy_duplicateRejected() {
        wmsStrategyService.createStrategy(buildReqVO("WST-A"));
        Long secondId = wmsStrategyService.createStrategy(buildReqVO("WST-B"));

        StrategySaveReqVO updateReqVO = buildReqVO("WST-A");
        updateReqVO.setId(secondId);
        assertServiceException(() -> wmsStrategyService.updateStrategy(updateReqVO), STRATEGY_CODE_DUPLICATE);
    }

    @Test
    void testUpdateStrategy_notExists() {
        StrategySaveReqVO updateReqVO = buildReqVO("WST-A");
        updateReqVO.setId(randomLongId());

        assertServiceException(() -> wmsStrategyService.updateStrategy(updateReqVO), STRATEGY_NOT_EXISTS);
    }

    @Test
    void testDeleteStrategy_success() {
        Long id = wmsStrategyService.createStrategy(buildReqVO("WST-A"));

        wmsStrategyService.deleteStrategy(id);

        assertNull(wmsStrategyMapper.selectById(id));
    }

    @Test
    void testDeleteStrategy_notExists() {
        assertServiceException(() -> wmsStrategyService.deleteStrategy(randomLongId()), STRATEGY_NOT_EXISTS);
    }

}
