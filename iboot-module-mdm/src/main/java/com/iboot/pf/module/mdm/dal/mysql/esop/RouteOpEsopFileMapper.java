package com.iboot.pf.module.mdm.dal.mysql.esop;

import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.mdm.dal.dataobject.esop.RouteOpEsopFileDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ESOP 作业步骤附件 Mapper
 *
 * @author iboot
 */
@Mapper
public interface RouteOpEsopFileMapper extends BaseMapperX<RouteOpEsopFileDO> {

    default List<RouteOpEsopFileDO> selectListByEsopId(Long esopId) {
        return selectList(new LambdaQueryWrapperX<RouteOpEsopFileDO>()
                .eq(RouteOpEsopFileDO::getEsopId, esopId)
                .orderByAsc(RouteOpEsopFileDO::getSeq));
    }

    default List<RouteOpEsopFileDO> selectListByEsopIds(List<Long> esopIds) {
        return selectList(new LambdaQueryWrapperX<RouteOpEsopFileDO>()
                .in(RouteOpEsopFileDO::getEsopId, esopIds)
                .orderByAsc(RouteOpEsopFileDO::getSeq));
    }

    default int deleteByEsopIds(List<Long> esopIds) {
        return deleteBatch(RouteOpEsopFileDO::getEsopId, esopIds);
    }

}
