package com.iboot.pf.module.mdm.dal.mysql.tool;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.controller.admin.tool.vo.RouteOpToolRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.tool.RouteOpToolDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工序工装 Mapper
 *
 * @author iboot
 */
@Mapper
public interface RouteOpToolMapper extends BaseMapperX<RouteOpToolDO> {

    default List<RouteOpToolDO> selectListByRouteOpId(Long routeOpId) {
        return selectList(RouteOpToolDO::getRouteOpId, routeOpId);
    }

    default List<RouteOpToolDO> selectListByRouteOpIds(List<Long> routeOpIds) {
        if (routeOpIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<RouteOpToolDO>()
                .in(RouteOpToolDO::getRouteOpId, routeOpIds));
    }

    default RouteOpToolDO selectByRouteOpIdAndToolId(Long routeOpId, Long toolId) {
        return selectOne(new LambdaQueryWrapperX<RouteOpToolDO>()
                .eq(RouteOpToolDO::getRouteOpId, routeOpId)
                .eq(RouteOpToolDO::getToolId, toolId));
    }

    default int deleteByRouteOpIds(List<Long> routeOpIds) {
        return deleteBatch(RouteOpToolDO::getRouteOpId, routeOpIds);
    }

    default PageResult<RouteOpToolRespVO> selectJoinPage(PageParam pageParam, Long routeOpId) {
        IPage<RouteOpToolRespVO> mpPage = MyBatisUtils.buildPage(pageParam);
        selectJoinPage(mpPage, routeOpId);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    IPage<RouteOpToolRespVO> selectJoinPage(IPage<RouteOpToolRespVO> page, @Param("routeOpId") Long routeOpId);

    RouteOpToolRespVO selectJoinById(@Param("id") Long id);

}
