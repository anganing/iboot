package com.iboot.pf.module.mdm.dal.mysql.routeopmat;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.routeopmat.RouteOpMatDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.routeopmat.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * 工序用料 Mapper
 *
 * @author iboot
 */
@Mapper
public interface RouteOpMatMapper extends BaseMapperX<RouteOpMatDO> {

    default PageResult<RouteOpMatDO> selectPage(RouteOpMatPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RouteOpMatDO>()
                .eqIfPresent(RouteOpMatDO::getRouteOpId, reqVO.getRouteOpId())
                .eqIfPresent(RouteOpMatDO::getMatId, reqVO.getMatId())
                .eqIfPresent(RouteOpMatDO::getQty, reqVO.getQty())
                .eqIfPresent(RouteOpMatDO::getUomId, reqVO.getUomId())
                .eqIfPresent(RouteOpMatDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(RouteOpMatDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(RouteOpMatDO::getId));
    }

    default int deleteByRouteOpIds(List<Long> routeOpIds) {
        return deleteBatch(RouteOpMatDO::getRouteOpId, routeOpIds);
    }

    default List<RouteOpMatDO> selectListByRouteOpIds(List<Long> routeOpIds) {
        if (routeOpIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapper<RouteOpMatDO>()
                .in(RouteOpMatDO::getRouteOpId, routeOpIds)
                .orderByAsc(RouteOpMatDO::getRouteOpId)
                .orderByAsc(RouteOpMatDO::getId));
    }

}
