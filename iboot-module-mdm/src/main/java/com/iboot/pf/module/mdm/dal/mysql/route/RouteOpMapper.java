package com.iboot.pf.module.mdm.dal.mysql.route;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.controller.admin.route.vo.RouteOpRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.routeop.RouteOpDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 分页查询路线工序，JOIN 出工序与工作中心的可读字段。
     *
     * <p>列表与关联选择器只显示编码/名称，不显示裸 ID，所以在 Mapper 层一次 JOIN 出来，
     * 不在应用层二次拼接。
     */
    default PageResult<RouteOpRespVO> selectJoinPage(PageParam reqVO, Long routeId) {
        IPage<RouteOpRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, routeId);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    IPage<RouteOpRespVO> selectJoinPage(IPage<RouteOpRespVO> page, @Param("routeId") Long routeId);

    RouteOpRespVO selectJoinById(@Param("id") Long id);

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

    default long selectCountByOpId(Long opId) {
        return selectCount(RouteOpDO::getOpId, opId);
    }

	default int deleteByRouteIds(List<Long> routeIds) {
	    return deleteBatch(RouteOpDO::getRouteId, routeIds);
	}

}
