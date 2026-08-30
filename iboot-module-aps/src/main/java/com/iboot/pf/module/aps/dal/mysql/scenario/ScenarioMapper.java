package com.iboot.pf.module.aps.dal.mysql.scenario;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.aps.dal.dataobject.scenario.ScenarioDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.aps.controller.admin.scenario.vo.*;

/**
 * APS-排程场景 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ScenarioMapper extends BaseMapperX<ScenarioDO> {

    default PageResult<ScenarioRespVO> selectPage(ScenarioPageReqVO reqVO) {
        IPage<ScenarioRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ScenarioRespVO selectJoinById(@Param("id") Long id);

    IPage<ScenarioRespVO> selectJoinPage(IPage<ScenarioRespVO> page, @Param("req") ScenarioPageReqVO reqVO);

}