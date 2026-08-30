package com.iboot.pf.module.mes.dal.mysql.rgvtask;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.rgvtask.RgvTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.rgvtask.vo.*;

/**
 * MES-RGV工位流转任务 Mapper
 *
 * @author iboot
 */
@Mapper
public interface RgvTaskMapper extends BaseMapperX<RgvTaskDO> {

    default PageResult<RgvTaskRespVO> selectPage(RgvTaskPageReqVO reqVO) {
        IPage<RgvTaskRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    RgvTaskRespVO selectJoinById(@Param("id") Long id);

    IPage<RgvTaskRespVO> selectJoinPage(IPage<RgvTaskRespVO> page, @Param("req") RgvTaskPageReqVO reqVO);

}