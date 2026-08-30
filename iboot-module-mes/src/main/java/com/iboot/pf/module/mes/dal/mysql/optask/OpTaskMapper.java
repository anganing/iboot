package com.iboot.pf.module.mes.dal.mysql.optask;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.optask.OpTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.optask.vo.*;

/**
 * MES-工序任务 Mapper
 *
 * @author iboot
 */
@Mapper
public interface OpTaskMapper extends BaseMapperX<OpTaskDO> {

    default PageResult<OpTaskRespVO> selectPage(OpTaskPageReqVO reqVO) {
        IPage<OpTaskRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    OpTaskRespVO selectJoinById(@Param("id") Long id);

    IPage<OpTaskRespVO> selectJoinPage(IPage<OpTaskRespVO> page, @Param("req") OpTaskPageReqVO reqVO);

}