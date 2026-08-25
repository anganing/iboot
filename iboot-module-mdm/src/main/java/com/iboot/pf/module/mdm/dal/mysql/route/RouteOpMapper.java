package com.iboot.pf.module.mdm.dal.mysql.route;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 工艺路线工序 Mapper
 *
 * @author iboot
 */
@Mapper
public interface RouteOpMapper extends BaseMapperX<RouteOpDO> {

    default PageResult<RouteOpDO> selectPage(PageParam reqVO, Long routeId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RouteOpDO>()
                .eq(RouteOpDO::getRouteId, routeId)
                .orderByAsc(RouteOpDO::getSeq));
    }

    default List<RouteOpDO> selectListByRouteId(Long routeId) {
        return selectList(new LambdaQueryWrapper<RouteOpDO>()
                .eq(RouteOpDO::getRouteId, routeId)
                .orderByAsc(RouteOpDO::getSeq));
    }

    default Integer selectMaxSeqByRouteId(Long routeId) {
        RouteOpDO routeOp = selectList(new LambdaQueryWrapper<RouteOpDO>()
                .eq(RouteOpDO::getRouteId, routeId)
                .orderByDesc(RouteOpDO::getSeq))
                .stream().findFirst().orElse(null);
        return routeOp == null ? null : routeOp.getSeq();
    }

    default int deleteByRouteId(Long routeId) {
        return delete(RouteOpDO::getRouteId, routeId);
    }

    default long selectCountByRouteId(Long routeId) {
        return selectCount(RouteOpDO::getRouteId, routeId);
    }

	default int deleteByRouteIds(List<Long> routeIds) {
	    return deleteBatch(RouteOpDO::getRouteId, routeIds);
	}

}
