package com.iboot.pf.module.mdm.dal.mysql.route;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.dal.dataobject.route.RouteDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mdm.controller.admin.route.vo.*;

/**
 * 工艺路线 Mapper
 *
 * @author iboot
 */
@Mapper
public interface RouteMapper extends BaseMapperX<RouteDO> {

    default PageResult<RouteRespVO> selectPage(RoutePageReqVO reqVO) {
        IPage<RouteRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    RouteRespVO selectJoinById(@Param("id") Long id);

    IPage<RouteRespVO> selectJoinPage(IPage<RouteRespVO> page, @Param("req") RoutePageReqVO reqVO);

    RouteDetailRespVO selectDetailById(@Param("id") Long id);

    /**
     * 查询物料下指定版本的路线，供版本复制时校验新版本号唯一。
     */
    default RouteDO selectByMatIdAndVersion(Long matId, String version) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RouteDO>()
                .eq(RouteDO::getMatId, matId)
                .eq(RouteDO::getVersion, version));
    }

}
