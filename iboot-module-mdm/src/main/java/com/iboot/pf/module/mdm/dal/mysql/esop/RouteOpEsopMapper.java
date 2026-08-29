package com.iboot.pf.module.mdm.dal.mysql.esop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.controller.admin.esop.vo.RouteOpEsopPageReqVO;
import com.iboot.pf.module.mdm.controller.admin.esop.vo.RouteOpEsopRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.esop.RouteOpEsopDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工序 ESOP 作业步骤 Mapper
 *
 * @author iboot
 */
@Mapper
public interface RouteOpEsopMapper extends BaseMapperX<RouteOpEsopDO> {

    /**
     * 按路线工序编号批量查询，供 MES 按 route_op_id 反查指导书。
     */
    default List<RouteOpEsopDO> selectListByRouteOpId(Long routeOpId) {
        return selectList(new LambdaQueryWrapperX<RouteOpEsopDO>()
                .eq(RouteOpEsopDO::getRouteOpId, routeOpId)
                .orderByAsc(RouteOpEsopDO::getSeq));
    }

    default List<RouteOpEsopDO> selectListByRouteOpIds(List<Long> routeOpIds) {
        return selectList(new LambdaQueryWrapperX<RouteOpEsopDO>()
                .in(RouteOpEsopDO::getRouteOpId, routeOpIds)
                .orderByAsc(RouteOpEsopDO::getSeq));
    }

    /**
     * 按工艺路线编号集合查询全部 ESOP（route 版本复制时使用）。
     */
    default List<RouteOpEsopDO> selectListByRouteId(List<Long> routeOpIds) {
        return selectListByRouteOpIds(routeOpIds);
    }

    /**
     * 校验同一工序下的作业步骤顺序是否重复。
     */
    default RouteOpEsopDO selectByRouteOpIdAndSeq(Long routeOpId, Integer seq) {
        return selectOne(new LambdaQueryWrapperX<RouteOpEsopDO>()
                .eq(RouteOpEsopDO::getRouteOpId, routeOpId)
                .eq(RouteOpEsopDO::getSeq, seq));
    }

    default int deleteByRouteOpIds(List<Long> routeOpIds) {
        return deleteBatch(RouteOpEsopDO::getRouteOpId, routeOpIds);
    }

    default PageResult<RouteOpEsopRespVO> selectJoinPage(PageParam pageParam, RouteOpEsopPageReqVO reqVO) {
        IPage<RouteOpEsopRespVO> mpPage = MyBatisUtils.buildPage(pageParam);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    IPage<RouteOpEsopRespVO> selectJoinPage(IPage<RouteOpEsopRespVO> page, @Param("req") RouteOpEsopPageReqVO reqVO);

    RouteOpEsopRespVO selectJoinById(@Param("id") Long id);

    /**
     * 按路线工序一次 JOIN 出作业步骤及其附件列表。
     */
    List<RouteOpEsopRespVO> selectJoinListByRouteOpId(@Param("routeOpId") Long routeOpId);

}
