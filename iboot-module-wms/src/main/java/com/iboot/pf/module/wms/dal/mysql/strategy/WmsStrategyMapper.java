package com.iboot.pf.module.wms.dal.mysql.strategy;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.wms.dal.dataobject.strategy.WmsStrategyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.wms.controller.admin.strategy.vo.*;

/**
 * WMS-仓储策略 Mapper
 *
 * @author iboot
 */
@Mapper
public interface WmsStrategyMapper extends BaseMapperX<WmsStrategyDO> {

    default PageResult<StrategyRespVO> selectPage(StrategyPageReqVO reqVO) {
        IPage<StrategyRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    StrategyRespVO selectJoinById(@Param("id") Long id);

    IPage<StrategyRespVO> selectJoinPage(IPage<StrategyRespVO> page, @Param("req") StrategyPageReqVO reqVO);


    /** 唯一索引 uk_wms_strategy_code_ver_deleted 的应用层前置校验，命中说明策略编码与版本重复。 */
    default WmsStrategyDO selectByTypeAndCodeAndVersion(String strategyType, String code, String versionNo) {
        return selectOne(WmsStrategyDO::getStrategyType, strategyType,
                WmsStrategyDO::getCode, code, WmsStrategyDO::getVersionNo, versionNo);
    }

}
