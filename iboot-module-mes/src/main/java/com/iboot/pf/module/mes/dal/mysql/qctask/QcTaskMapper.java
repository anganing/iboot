package com.iboot.pf.module.mes.dal.mysql.qctask;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.qctask.QcTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.qctask.vo.*;

/**
 * MES-质检任务 Mapper
 *
 * @author iboot
 */
@Mapper
public interface QcTaskMapper extends BaseMapperX<QcTaskDO> {

    default PageResult<QcTaskRespVO> selectPage(QcTaskPageReqVO reqVO) {
        IPage<QcTaskRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    QcTaskRespVO selectJoinById(@Param("id") Long id);

    IPage<QcTaskRespVO> selectJoinPage(IPage<QcTaskRespVO> page, @Param("req") QcTaskPageReqVO reqVO);

}