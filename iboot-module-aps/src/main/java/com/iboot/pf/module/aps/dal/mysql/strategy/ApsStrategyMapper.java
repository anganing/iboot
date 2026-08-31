package com.iboot.pf.module.aps.dal.mysql.strategy;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategyPageReqVO;
import com.iboot.pf.module.aps.controller.admin.strategy.vo.StrategyRespVO;
import com.iboot.pf.module.aps.dal.dataobject.strategy.ApsStrategyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** APS-排程策略 Mapper。 */
@Mapper
public interface ApsStrategyMapper extends BaseMapperX<ApsStrategyDO> {

    default PageResult<StrategyRespVO> selectPage(StrategyPageReqVO reqVO) {
        IPage<StrategyRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    StrategyRespVO selectJoinById(@Param("id") Long id);

    IPage<StrategyRespVO> selectJoinPage(IPage<StrategyRespVO> page, @Param("req") StrategyPageReqVO reqVO);

    /** 唯一索引 uk_aps_strategy_code_ver_deleted 的应用层前置校验，命中说明编码与版本号重复。 */
    default ApsStrategyDO selectByCodeAndVersionNo(String code, String versionNo) {
        return selectOne(ApsStrategyDO::getCode, code, ApsStrategyDO::getVersionNo, versionNo);
    }

}
