package com.iboot.pf.module.aps.dal.mysql.projection;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.controller.admin.projectionmat.vo.ProjectionMatPageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectionmat.vo.ProjectionMatRespVO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsMaterialProjectionDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** APS-MDM 物料投影 Mapper。 */
@Mapper
public interface ApsMaterialProjectionMapper extends BaseMapperX<ApsMaterialProjectionDO> {

    default PageResult<ProjectionMatRespVO> selectPage(ProjectionMatPageReqVO reqVO) {
        IPage<ProjectionMatRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProjectionMatRespVO selectJoinById(@Param("id") Long id);

    IPage<ProjectionMatRespVO> selectJoinPage(IPage<ProjectionMatRespVO> page,
                                              @Param("req") ProjectionMatPageReqVO reqVO);

    /** 按场景与可选筛选条件列出物料投影，供求解输入使用。 */
    default List<ApsMaterialProjectionDO> selectListByScenario(Long scenarioId, Long sourceMatId, String matType) {
        return selectList(new LambdaQueryWrapper<ApsMaterialProjectionDO>()
                .eq(ApsMaterialProjectionDO::getScenarioId, scenarioId)
                .eq(sourceMatId != null, ApsMaterialProjectionDO::getSourceMatId, sourceMatId)
                .eq(matType != null, ApsMaterialProjectionDO::getMatType, matType));
    }

    /**
     * 物理删除某场景的物料投影。
     *
     * <p>投影每次刷新整体重建，逻辑删除会让含 deleted 的唯一索引在下一次刷新时撞车。
     */
    @Delete("DELETE FROM aps_projection_mat WHERE scenario_id = #{scenarioId}")
    int deletePhysicalByScenarioId(Long scenarioId);

}
