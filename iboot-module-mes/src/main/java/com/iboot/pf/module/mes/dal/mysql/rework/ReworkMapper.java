package com.iboot.pf.module.mes.dal.mysql.rework;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.rework.ReworkDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.rework.vo.*;

/**
 * MES-返工任务 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ReworkMapper extends BaseMapperX<ReworkDO> {

    default PageResult<ReworkRespVO> selectPage(ReworkPageReqVO reqVO) {
        IPage<ReworkRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ReworkRespVO selectJoinById(@Param("id") Long id);

    IPage<ReworkRespVO> selectJoinPage(IPage<ReworkRespVO> page, @Param("req") ReworkPageReqVO reqVO);

}