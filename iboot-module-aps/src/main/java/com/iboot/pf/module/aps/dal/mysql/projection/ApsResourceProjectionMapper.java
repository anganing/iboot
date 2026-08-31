package com.iboot.pf.module.aps.dal.mysql.projection;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.controller.admin.projectionresource.vo.ProjectionResourcePageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectionresource.vo.ProjectionResourceRespVO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsResourceProjectionDO;
import com.iboot.pf.module.aps.enums.ApsConstants;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** APS-MDM 资源与日历投影 Mapper。 */
@Mapper
public interface ApsResourceProjectionMapper extends BaseMapperX<ApsResourceProjectionDO> {

    default PageResult<ProjectionResourceRespVO> selectPage(ProjectionResourcePageReqVO reqVO) {
        IPage<ProjectionResourceRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProjectionResourceRespVO selectJoinById(@Param("id") Long id);

    IPage<ProjectionResourceRespVO> selectJoinPage(IPage<ProjectionResourceRespVO> page,
                                                   @Param("req") ProjectionResourcePageReqVO reqVO);

    /** 按场景列出日汇总产能（班次编号为 0 的记录），负荷校验取它作为可用分钟数。 */
    default List<ApsResourceProjectionDO> selectDailyListByScenarioId(Long scenarioId) {
        return selectList(new LambdaQueryWrapper<ApsResourceProjectionDO>()
                .eq(ApsResourceProjectionDO::getScenarioId, scenarioId)
                .eq(ApsResourceProjectionDO::getShiftId, ApsConstants.NONE_ID)
                .orderByAsc(ApsResourceProjectionDO::getSourceResId, ApsResourceProjectionDO::getWorkDate));
    }

    /** 物理删除某场景的资源投影，理由同物料投影。 */
    @Delete("DELETE FROM aps_projection_resource WHERE scenario_id = #{scenarioId}")
    int deletePhysicalByScenarioId(Long scenarioId);

}
