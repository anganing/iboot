package com.iboot.pf.module.aps.dal.mysql.projection;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.controller.admin.projectionorder.vo.ProjectionOrderPageReqVO;
import com.iboot.pf.module.aps.controller.admin.projectionorder.vo.ProjectionOrderRespVO;
import com.iboot.pf.module.aps.dal.dataobject.projection.ApsOrderProjectionDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** APS-MES 需求工单任务投影 Mapper。 */
@Mapper
public interface ApsOrderProjectionMapper extends BaseMapperX<ApsOrderProjectionDO> {

    default PageResult<ProjectionOrderRespVO> selectPage(ProjectionOrderPageReqVO reqVO) {
        IPage<ProjectionOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ProjectionOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<ProjectionOrderRespVO> selectJoinPage(IPage<ProjectionOrderRespVO> page,
                                                @Param("req") ProjectionOrderPageReqVO reqVO);

    /** 按场景与可选筛选条件列出工单投影，按交期与优先级排序，交期紧、优先级高的先排。 */
    default List<ApsOrderProjectionDO> selectListByScenario(Long scenarioId, String sourceType, Long prodOrderId,
                                                            Long matId, String currentStatus) {
        return selectList(new LambdaQueryWrapper<ApsOrderProjectionDO>()
                .eq(ApsOrderProjectionDO::getScenarioId, scenarioId)
                .eq(sourceType != null, ApsOrderProjectionDO::getSourceType, sourceType)
                .eq(prodOrderId != null, ApsOrderProjectionDO::getProdOrderId, prodOrderId)
                .eq(matId != null, ApsOrderProjectionDO::getMatId, matId)
                .eq(currentStatus != null, ApsOrderProjectionDO::getCurrentStatus, currentStatus)
                .orderByAsc(ApsOrderProjectionDO::getDueDate)
                .orderByDesc(ApsOrderProjectionDO::getPriority));
    }

    /** 物理删除某场景的工单投影，理由同物料投影。 */
    @Delete("DELETE FROM aps_projection_order WHERE scenario_id = #{scenarioId}")
    int deletePhysicalByScenarioId(Long scenarioId);

}
