package com.iboot.pf.module.wms.service.strategy;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.wms.controller.admin.strategy.vo.StrategyConfigVO;
import com.iboot.pf.module.wms.controller.admin.strategy.vo.StrategyFieldMetadataRespVO;
import com.iboot.pf.module.wms.controller.admin.strategy.vo.StrategyPageReqVO;
import com.iboot.pf.module.wms.controller.admin.strategy.vo.StrategyRespVO;
import com.iboot.pf.module.wms.controller.admin.strategy.vo.StrategySaveReqVO;
import com.iboot.pf.module.wms.dal.dataobject.strategy.WmsStrategyDO;
import com.iboot.pf.module.wms.dal.mysql.strategy.WmsStrategyMapper;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.enums.WmsConstants;
import com.iboot.pf.module.wms.enums.WmsDimensionCheckModeEnum;
import com.iboot.pf.module.wms.enums.WmsStrategyStatusEnum;
import com.iboot.pf.module.wms.enums.WmsStrategyTypeEnum;
import com.iboot.pf.module.wms.strategy.config.WmsStrategyConfig;
import com.iboot.pf.module.wms.strategy.config.WmsStrategyConfigParser;
import com.iboot.pf.module.wms.strategy.config.WmsStrategySortOrderEnum;
import com.iboot.pf.module.wms.strategy.field.WmsStrategyOperatorEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static com.iboot.pf.framework.test.core.util.AssertUtils.assertServiceException;
import static com.iboot.pf.framework.test.core.util.RandomUtils.randomLongId;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_NOT_EXISTS;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_CODE_DUPLICATE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_CONFIG_INCOMPLETE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 仓储策略 Service 测试：覆盖编码自动分配、业务唯一性、存在性校验、删除，
 * 以及结构化配置落库与启用时的配置完整性校验。
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
        reqVO.setConfig(WmsStrategyConfigConverter.defaultConfigVO(WmsStrategyTypeEnum.PUTAWAY));
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

    @Test
    void testCreateStrategy_configSerializedToJson() {
        StrategySaveReqVO reqVO = buildReqVO("WST-A");
        StrategyConfigVO.Condition condition = new StrategyConfigVO.Condition();
        condition.setField("zoneType");
        condition.setOp(WmsStrategyOperatorEnum.EQ);
        condition.setValue("RAW");
        reqVO.getConfig().setConditions(List.of(condition));
        StrategyConfigVO.Sort sort = new StrategyConfigVO.Sort();
        sort.setField("aisleNo");
        sort.setOrder(WmsStrategySortOrderEnum.ASC);
        reqVO.getConfig().setSorts(List.of(sort));
        reqVO.getConfig().setDimensionCheck(WmsDimensionCheckModeEnum.FULL_DIM.name());

        Long id = wmsStrategyService.createStrategy(reqVO);

        // 存进库的是结构化 JSON，而且能被解析器原样读回来——这是"存得进去就一定能执行"的依据
        WmsStrategyConfig stored = WmsStrategyConfigParser.parse(
                WmsStrategyTypeEnum.PUTAWAY, wmsStrategyMapper.selectById(id).getConfigJson());
        assertEquals("zoneType", stored.getConditions().get(0).getField());
        assertEquals(WmsStrategyOperatorEnum.EQ, stored.getConditions().get(0).getOp());
        assertEquals("aisleNo", stored.getSorts().get(0).getField());
        assertEquals(WmsDimensionCheckModeEnum.FULL_DIM.name(), stored.getDimensionCheck());
    }

    @Test
    void testCreateStrategy_crossTypeFieldsDropped() {
        StrategySaveReqVO reqVO = buildReqVO("WST-A");
        // 页面上从补货切回上架时，补货专属字段还留在表单值里，不能原样落库
        reqVO.getConfig().setFromWhCode("WH-FLAT01");
        reqVO.getConfig().setReplenishQty(BigDecimal.valueOf(200));

        Long id = wmsStrategyService.createStrategy(reqVO);

        WmsStrategyConfig stored = WmsStrategyConfigParser.parse(
                WmsStrategyTypeEnum.PUTAWAY, wmsStrategyMapper.selectById(id).getConfigJson());
        assertNull(stored.getFromWhCode());
        assertNull(stored.getReplenishQty());
    }

    @Test
    void testCreateStrategy_replenishDraftAllowsIncompleteConfig() {
        StrategySaveReqVO reqVO = buildReqVO("WST-R");
        reqVO.setStrategyType(WmsStrategyTypeEnum.REPLENISH.name());
        reqVO.setStatus(WmsStrategyStatusEnum.DRAFT.name());
        reqVO.setConfig(WmsStrategyConfigConverter.defaultConfigVO(WmsStrategyTypeEnum.REPLENISH));

        // 草稿要能存：策略是先建骨架再慢慢配的，一上来就要求配全，人连草稿都留不下来
        assertNotNull(wmsStrategyService.createStrategy(reqVO));
    }

    @Test
    void testCreateStrategy_replenishEnabledRequiresCompleteConfig() {
        StrategySaveReqVO reqVO = buildReqVO("WST-R");
        reqVO.setStrategyType(WmsStrategyTypeEnum.REPLENISH.name());
        reqVO.setStatus(WmsStrategyStatusEnum.ENABLED.name());
        reqVO.setConfig(WmsStrategyConfigConverter.defaultConfigVO(WmsStrategyTypeEnum.REPLENISH));

        // 启用才是配置必须完整的时刻：启用后策略立刻参与补货判断
        assertServiceException(() -> wmsStrategyService.createStrategy(reqVO), STRATEGY_CONFIG_INCOMPLETE,
                "补货策略至少要有一条触发条件");
    }

    @Test
    void testGetStrategy_invalidConfigFallsBackInsteadOfFailing() {
        Long id = wmsStrategyService.createStrategy(buildReqVO("WST-A"));
        // 模拟历史脏数据：库里存着当前版本认不出的配置
        WmsStrategyDO dirty = new WmsStrategyDO();
        dirty.setId(id);
        dirty.setConfigJson("{\"conditions\":[{\"field\":\"noSuchField\",\"op\":\"EQ\",\"value\":\"X\"}]}");
        wmsStrategyMapper.updateById(dirty);

        StrategyRespVO respVO = wmsStrategyService.getStrategy(id);

        // 打不开等于只能靠 DML 去修，所以读路径回落默认值并带上原因，保存一次就修正了
        assertEquals(Boolean.TRUE, respVO.getConfigInvalid());
        assertNotNull(respVO.getConfigInvalidReason());
        assertNotNull(respVO.getConfig());
        assertEquals(0, respVO.getConfig().getConditions().size());
    }

    @Test
    void testGetFieldMetadata_coversEveryStrategyType() {
        StrategyFieldMetadataRespVO metadata = wmsStrategyService.getFieldMetadata();

        assertEquals(WmsStrategyTypeEnum.values().length, metadata.getTypes().size());
        metadata.getTypes().forEach(type -> {
            assertNotNull(type.getDefaultConfig(), type.getStrategyType() + " 缺默认配置");
            // 每个类型都得有可筛字段，否则页面上打开就是一片空白，没法配
            assertFalse(type.getConditionFields().isEmpty(), type.getStrategyType() + " 没有可筛字段");
        });
    }

    @Test
    void testCreateStrategy_blankWhIdMeansGlobal() {
        StrategySaveReqVO reqVO = buildReqVO("WST-G");
        reqVO.setWhId(null);

        Long id = wmsStrategyService.createStrategy(reqVO);

        // 页面上不选仓库就是"对所有仓库生效"，而 wh_id 是 NOT NULL 列，落库前必须归一成 0
        assertEquals(WmsConstants.GLOBAL_WH_ID, wmsStrategyMapper.selectById(id).getWhId());
    }

    @Test
    void testGetStrategyPage_filterByWarehouse() {
        StrategySaveReqVO scoped = buildReqVO("WST-A");
        scoped.setWhId(1L);
        wmsStrategyService.createStrategy(scoped);
        StrategySaveReqVO global = buildReqVO("WST-B");
        global.setWhId(null);
        wmsStrategyService.createStrategy(global);

        StrategyPageReqVO pageReqVO = new StrategyPageReqVO();
        pageReqVO.setWhId(1L);
        PageResult<StrategyRespVO> page = wmsStrategyService.getStrategyPage(pageReqVO);

        // 选了仓库就只看这个仓库的，全局策略不跟着冒出来
        assertEquals(1, page.getTotal());
        assertEquals("WST-A", page.getList().get(0).getCode());
    }

}
