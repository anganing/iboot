package com.iboot.pf.module.wms.service.strategy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.wms.controller.admin.strategy.vo.*;
import com.iboot.pf.module.wms.dal.dataobject.strategy.WmsStrategyDO;
import com.iboot.pf.module.wms.dal.mysql.strategy.WmsStrategyMapper;
import com.iboot.pf.module.wms.enums.WmsCodeRuleConstants;
import com.iboot.pf.module.wms.enums.WmsConstants;
import com.iboot.pf.module.wms.enums.WmsStrategyStatusEnum;
import com.iboot.pf.module.wms.enums.WmsStrategyTypeEnum;
import com.iboot.pf.module.wms.service.coderule.WmsCodeService;
import com.iboot.pf.module.wms.strategy.config.WmsStrategySortOrderEnum;
import com.iboot.pf.module.wms.strategy.field.WmsStrategyConditionField;
import com.iboot.pf.module.wms.strategy.field.WmsStrategyFieldCatalog;
import com.iboot.pf.module.wms.strategy.field.WmsStrategySortField;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_CODE_DUPLICATE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_CONFIG_INCOMPLETE;
import static com.iboot.pf.module.wms.enums.ErrorCodeConstants.STRATEGY_NOT_EXISTS;

/**
 * WMS-仓储策略 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class WmsStrategyServiceImpl implements WmsStrategyService {

    @Resource
    private WmsStrategyMapper strategyMapper;
    @Resource
    private WmsCodeService codeService;

    @Override
    public Long createStrategy(StrategySaveReqVO createReqVO) {
        // 插入
        WmsStrategyDO strategy = BeanUtils.toBean(createReqVO, WmsStrategyDO.class);
        strategy.setId(null);
        // 结构化配置在这里序列化成 config_json，顺带过一遍解析器做终检
        strategy.setConfigJson(WmsStrategyConfigConverter.toConfigJson(
                createReqVO.getStrategyType(), createReqVO.getConfig()));
        validateConfigCompleteOnEnabled(createReqVO);
        normalizeWhId(strategy);
        strategy.setCode(codeService.generate(WmsCodeRuleConstants.STRATEGY, strategy.getCode()));
        validateStrategyUnique(strategy, null);
        strategyMapper.insert(strategy);

        // 返回
        return strategy.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public StrategyImportRespVO importStrategyList(List<StrategyImportExcelVO> importList) {
        if (CollUtil.isEmpty(importList)) {
            return StrategyImportRespVO.builder()
                    .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        }
        // 遍历，逐个创建
        StrategyImportRespVO respVO = StrategyImportRespVO.builder()
                .successCount(0).failureCount(0).failureRows(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            try {
                StrategySaveReqVO saveReqVO = BeanUtils.toBean(importItem, StrategySaveReqVO.class);
                // 导入只批量建骨架，配置给该类型的默认值（不筛不排），进页面用结构化编辑器配
                saveReqVO.setConfig(WmsStrategyConfigConverter.defaultConfigVO(saveReqVO.getStrategyType()));
                createStrategy(saveReqVO);
                respVO.setSuccessCount(respVO.getSuccessCount() + 1);
            } catch (Exception ex) {
                respVO.getFailureRows().put(currentIndex, ex.getMessage());
            }
        });
        respVO.setFailureCount(respVO.getFailureRows().size());
        return respVO;
    }

    @Override
    public void updateStrategy(StrategySaveReqVO updateReqVO) {
        // 校验存在
        validateStrategyExists(updateReqVO.getId());
        // 更新
        WmsStrategyDO updateObj = BeanUtils.toBean(updateReqVO, WmsStrategyDO.class);
        updateObj.setConfigJson(WmsStrategyConfigConverter.toConfigJson(
                updateReqVO.getStrategyType(), updateReqVO.getConfig()));
        validateConfigCompleteOnEnabled(updateReqVO);
        normalizeWhId(updateObj);
        // 改自己不能撞自己，所以排除自身编号
        validateStrategyUnique(updateObj, updateReqVO.getId());
        strategyMapper.updateById(updateObj);
    }

    @Override
    public void deleteStrategy(Long id) {
        // 校验存在
        validateStrategyExists(id);
        // 删除
        strategyMapper.deleteById(id);
    }

    @Override
        public void deleteStrategyListByIds(List<Long> ids) {
        // 删除
        strategyMapper.deleteByIds(ids);
        }


    /**
     * 没选仓库就是全局策略，落成 0。
     *
     * <p>归一放在这里而不是让前端提交 0：页面上"不选仓库"表达的是意图，"0 表示全局"是落库约定，
     * 后者不该泄漏到 UI。{@code wh_id} 是 NOT NULL 列，不归一会直接插入失败。
     */
    private void normalizeWhId(WmsStrategyDO strategy) {
        if (strategy.getWhId() == null) {
            strategy.setWhId(WmsConstants.GLOBAL_WH_ID);
        }
    }

    /**
     * 启用时校验配置完整。
     *
     * <p>只在启用时校验，草稿和停用放过：策略是先建骨架再慢慢配的，一上来就要求配全，
     * 人连草稿都存不下来。真正不能含糊的时刻是启用——启用后策略就参与选库位和拣选了。
     *
     * <p>目前只有补货策略有必填三件套。上架与拣选即使一条条件都不配也是有意义的配置
     * （等于"所有候选都可以，按稳定顺序取第一个"），不该拦。
     */
    private void validateConfigCompleteOnEnabled(StrategySaveReqVO reqVO) {
        if (!WmsStrategyStatusEnum.isEnabled(reqVO.getStatus())
                || !WmsStrategyTypeEnum.REPLENISH.name().equals(reqVO.getStrategyType())) {
            return;
        }
        StrategyConfigVO config = reqVO.getConfig();
        if (CollUtil.isEmpty(config.getConditions())) {
            // 没有触发条件的补货策略，要么永远不补要么每次都补，两种都不是想要的结果
            throw exception(STRATEGY_CONFIG_INCOMPLETE, "补货策略至少要有一条触发条件");
        }
        if (StrUtil.isBlank(config.getFromWhCode())) {
            throw exception(STRATEGY_CONFIG_INCOMPLETE, "补货策略要指定从哪个仓库补货");
        }
        if (config.getReplenishQty() == null) {
            throw exception(STRATEGY_CONFIG_INCOMPLETE, "补货策略要填单次补货量");
        }
    }

    private void validateStrategyExists(Long id) {
        if (strategyMapper.selectById(id) == null) {
            throw exception(STRATEGY_NOT_EXISTS);
        }
    }

    /**
     * 唯一性校验：数据库唯一索引是最终保证，这里前置校验只为给出可读提示。
     */
    private void validateStrategyUnique(WmsStrategyDO strategy, Long excludeId) {
        WmsStrategyDO existed = strategyMapper.selectByTypeAndCodeAndVersion(
                strategy.getStrategyType(), strategy.getCode(), strategy.getVersionNo());
        if (existed == null || existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(STRATEGY_CODE_DUPLICATE);
    }

    @Override
    public StrategyRespVO getStrategy(Long id) {
        StrategyRespVO respVO = strategyMapper.selectJoinById(id);
        // 列表不解析配置：分页里没人看筛选条件，白解析几十条纯属浪费
        WmsStrategyConfigConverter.fillConfig(respVO);
        return respVO;
    }

    @Override
    public PageResult<StrategyRespVO> getStrategyPage(StrategyPageReqVO pageReqVO) {
        return strategyMapper.selectPage(pageReqVO);
    }

    /**
     * 策略字段元数据。
     *
     * <p>字段清单与默认配置都取自 {@code WmsStrategyFieldCatalog}——保存时校验用的也是它。
     * 前端不再自己维护一份，加字段时只改后端一处，不会出现"页面上能选、保存时被拒"。
     */
    @Override
    public StrategyFieldMetadataRespVO getFieldMetadata() {
        StrategyFieldMetadataRespVO respVO = new StrategyFieldMetadataRespVO();
        respVO.setTypes(Arrays.stream(WmsStrategyTypeEnum.values())
                .map(WmsStrategyServiceImpl::toTypeMetadata)
                .toList());
        respVO.setSortOrders(Arrays.stream(WmsStrategySortOrderEnum.values())
                .map(order -> toOption(order.name(), order.getName()))
                .toList());
        return respVO;
    }

    private static StrategyFieldMetadataRespVO.StrategyTypeMetadata toTypeMetadata(WmsStrategyTypeEnum type) {
        StrategyFieldMetadataRespVO.StrategyTypeMetadata metadata =
                new StrategyFieldMetadataRespVO.StrategyTypeMetadata();
        metadata.setStrategyType(type.name());
        metadata.setTypeName(type.getName());
        metadata.setCandidateTarget(WmsStrategyFieldCatalog.candidateTarget(type));
        metadata.setConditionFields(WmsStrategyFieldCatalog.conditionFields(type).stream()
                .map(WmsStrategyServiceImpl::toConditionFieldMetadata).toList());
        metadata.setSortFields(WmsStrategyFieldCatalog.sortFields(type).stream()
                .map(WmsStrategyServiceImpl::toSortFieldMetadata).toList());
        metadata.setDefaultConfig(WmsStrategyConfigConverter.defaultConfigVO(type));
        return metadata;
    }

    private static StrategyFieldMetadataRespVO.ConditionField toConditionFieldMetadata(
            WmsStrategyConditionField field) {
        StrategyFieldMetadataRespVO.ConditionField metadata = new StrategyFieldMetadataRespVO.ConditionField();
        metadata.setField(field.field());
        metadata.setDisplayName(field.displayName());
        metadata.setValueType(field.valueType().name());
        metadata.setDictType(field.dictType());
        metadata.setDescription(field.description());
        metadata.setOperators(field.operators().stream().map(op -> {
            StrategyFieldMetadataRespVO.Operator operator = new StrategyFieldMetadataRespVO.Operator();
            operator.setOp(op.name());
            operator.setOpName(op.getName());
            operator.setMultiValue(op.isMultiValue());
            return operator;
        }).toList());
        return metadata;
    }

    private static StrategyFieldMetadataRespVO.SortField toSortFieldMetadata(WmsStrategySortField field) {
        StrategyFieldMetadataRespVO.SortField metadata = new StrategyFieldMetadataRespVO.SortField();
        metadata.setField(field.field());
        metadata.setDisplayName(field.displayName());
        metadata.setDescription(field.description());
        return metadata;
    }

    private static StrategyFieldMetadataRespVO.Option toOption(String value, String label) {
        StrategyFieldMetadataRespVO.Option option = new StrategyFieldMetadataRespVO.Option();
        option.setValue(value);
        option.setLabel(label);
        return option;
    }

}
