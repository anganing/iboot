package com.iboot.pf.module.mdm.dal.mysql.route;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.route.RouteDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.route.vo.*;

/**
 * 工艺路线 Mapper
 *
 * @author iboot
 */
@Mapper
public interface RouteMapper extends BaseMapperX<RouteDO> {

    default PageResult<RouteDO> selectPage(RoutePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RouteDO>()
                .eqIfPresent(RouteDO::getMatId, reqVO.getMatId())
                .eqIfPresent(RouteDO::getVersion, reqVO.getVersion())
                .eqIfPresent(RouteDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(RouteDO::getEffectiveDate, reqVO.getEffectiveDate())
                .betweenIfPresent(RouteDO::getExpiryDate, reqVO.getExpiryDate())
                .eqIfPresent(RouteDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(RouteDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(RouteDO::getId));
    }

}