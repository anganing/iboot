package com.iboot.pf.module.aps.dal.mysql.projection;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.controller.admin.projectioninv.vo.ProjectionInvPageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectioninv.vo.ProjectionInvRespVO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsInventoryProjectionDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** APS-WMS 库存投影 Mapper。 */
@Mapper
public interface ApsInventoryProjectionMapper extends BaseMapperX<ApsInventoryProjectionDO> {

    default PageResult<ProjectionInvRespVO> selectPage(ProjectionInvPageReqVO reqVO) {
        IPage<ProjectionInvRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProjectionInvRespVO selectJoinById(@Param("id") Long id);

    IPage<ProjectionInvRespVO> selectJoinPage(IPage<ProjectionInvRespVO> page,
                                              @Param("req") ProjectionInvPageReqVO reqVO);

    /** 按场景与可选筛选条件列出库存投影。 */
    default List<ApsInventoryProjectionDO> selectListByScenario(Long scenarioId, Long matId, Long plantId,
                                                                Long projectId, String qualityStatus) {
        return selectList(new LambdaQueryWrapper<ApsInventoryProjectionDO>()
                .eq(ApsInventoryProjectionDO::getScenarioId, scenarioId)
                .eq(matId != null, ApsInventoryProjectionDO::getMatId, matId)
                .eq(plantId != null, ApsInventoryProjectionDO::getPlantId, plantId)
                .eq(projectId != null, ApsInventoryProjectionDO::getProjectId, projectId)
                .eq(qualityStatus != null, ApsInventoryProjectionDO::getQualityStatus, qualityStatus));
    }

    /** 按场景与质量状态列出库存投影，缺料汇总只认合格库存。 */
    default List<ApsInventoryProjectionDO> selectListByScenarioIdAndQualityStatus(Long scenarioId,
                                                                                  String qualityStatus) {
        return selectList(ApsInventoryProjectionDO::getScenarioId, scenarioId,
                ApsInventoryProjectionDO::getQualityStatus, qualityStatus);
    }

    /** 物理删除某场景的库存投影，理由同物料投影。 */
    @Delete("DELETE FROM aps_projection_inv WHERE scenario_id = #{scenarioId}")
    int deletePhysicalByScenarioId(Long scenarioId);

}
