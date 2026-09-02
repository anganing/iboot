package com.iboot.pf.module.aps.dal.mysql.version;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.controller.admin.version.vo.VersionPageReqVO;
import com.iboot.pf.module.aps.controller.admin.version.vo.VersionRespVO;
import com.iboot.pf.module.aps.dal.dataobject.version.ApsVersionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/** APS-排程版本 Mapper。 */
@Mapper
public interface ApsVersionMapper extends BaseMapperX<ApsVersionDO> {

    default PageResult<VersionRespVO> selectPage(VersionPageReqVO reqVO) {
        IPage<VersionRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    VersionRespVO selectJoinById(@Param("id") Long id);

    IPage<VersionRespVO> selectJoinPage(IPage<VersionRespVO> page, @Param("req") VersionPageReqVO reqVO);

    /** 唯一索引 uk_aps_version_scenario_no_deleted 的应用层前置校验。 */
    default ApsVersionDO selectByScenarioIdAndVersionNo(Long scenarioId, Integer versionNo) {
        return selectOne(ApsVersionDO::getScenarioId, scenarioId, ApsVersionDO::getVersionNo, versionNo);
    }

    /** 按场景倒序列出版本，最新版本在前。 */
    default List<ApsVersionDO> selectListByScenarioId(Long scenarioId) {
        return selectList(new LambdaQueryWrapper<ApsVersionDO>()
                .eq(ApsVersionDO::getScenarioId, scenarioId)
                .orderByDesc(ApsVersionDO::getVersionNo));
    }

    /** 按场景与状态列出版本，发布新版本时用来找出旧的已发布版本。 */
    default List<ApsVersionDO> selectListByScenarioIdAndStatus(Long scenarioId, String status) {
        return selectList(ApsVersionDO::getScenarioId, scenarioId, ApsVersionDO::getStatus, status);
    }

    /**
     * 场景下最新的已发布版本。
     *
     * <p>正常只会有一条：发布新版本时旧的已发布版本会被置为已被替换。这里仍按版本号倒序取第一条，
     * 避免历史脏数据导致查询直接抛错。
     */
    default ApsVersionDO selectPublishedByScenarioId(Long scenarioId, String publishedStatus) {
        return selectOne(new LambdaQueryWrapper<ApsVersionDO>()
                .eq(ApsVersionDO::getScenarioId, scenarioId)
                .eq(ApsVersionDO::getStatus, publishedStatus)
                .orderByDesc(ApsVersionDO::getVersionNo)
                .last("LIMIT 1"));
    }

    /**
     * 场景下曾经生效过的版本，最新在前。
     *
     * <p>只取已发布与已被替换：车间侧回看历史计划时，求解中、求解失败、待确认的版本从来没有下过车间，
     * 混进列表只会让人误以为那也是一份计划。
     */
    default List<ApsVersionDO> selectEffectiveListByScenarioId(Long scenarioId, Collection<String> statuses) {
        return selectList(new LambdaQueryWrapper<ApsVersionDO>()
                .eq(ApsVersionDO::getScenarioId, scenarioId)
                .in(ApsVersionDO::getStatus, statuses)
                .orderByDesc(ApsVersionDO::getVersionNo));
    }

    /** 场景下的版本数量，用于场景删除前的占用校验与版本号分配。 */
    default Long selectCountByScenarioId(Long scenarioId) {
        return selectCount(ApsVersionDO::getScenarioId, scenarioId);
    }

}
