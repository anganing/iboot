package com.iboot.pf.module.aps.dal.mysql.strategy;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.dal.dataobject.strategy.ApsStrategyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.*;

/**
 * APS-排程策略 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ApsStrategyMapper extends BaseMapperX<ApsStrategyDO> {

    default PageResult<StrategyRespVO> selectPage(StrategyPageReqVO reqVO) {
        IPage<StrategyRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    StrategyRespVO selectJoinById(@Param("id") Long id);

    IPage<StrategyRespVO> selectJoinPage(IPage<StrategyRespVO> page, @Param("req") StrategyPageReqVO reqVO);

}