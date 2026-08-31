package com.iboot.pf.module.aps.dal.mysql.scenario;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.ScenarioPageReqVO;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.ScenarioRespVO;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ApsScenarioDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * APS-排程场景 Mapper。
 *
 * <p>策略编码、名称在 XML 里 JOIN {@code aps_strategy} 取出；工厂、车间属于 mdm，
 * 由 Service 走门面批量补全，不在 SQL 里跨库 JOIN。
 */
@Mapper
public interface ApsScenarioMapper extends BaseMapperX<ApsScenarioDO> {

    default PageResult<ScenarioRespVO> selectPage(ScenarioPageReqVO reqVO) {
        IPage<ScenarioRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ScenarioRespVO selectJoinById(@Param("id") Long id);

    IPage<ScenarioRespVO> selectJoinPage(IPage<ScenarioRespVO> page, @Param("req") ScenarioPageReqVO reqVO);

    /** 唯一索引 uk_aps_scenario_code_deleted 的应用层前置校验，命中说明场景编码重复。 */
    default ApsScenarioDO selectByCode(String code) {
        return selectOne(ApsScenarioDO::getCode, code);
    }

    /** 策略是否被场景引用，用于策略删除前的占用校验。 */
    default Long selectCountByStrategyId(Long strategyId) {
        return selectCount(ApsScenarioDO::getStrategyId, strategyId);
    }

}
