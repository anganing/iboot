package com.iboot.pf.module.mes.dal.mysql.workorder;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mes.dal.dataobject.workorder.WorkOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mes.controller.admin.workorder.vo.*;

/**
 * MES-作业工单 Mapper
 *
 * @author iboot
 */
@Mapper
public interface WorkOrderMapper extends BaseMapperX<WorkOrderDO> {

    default PageResult<WorkOrderRespVO> selectPage(WorkOrderPageReqVO reqVO) {
        IPage<WorkOrderRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    WorkOrderRespVO selectJoinById(@Param("id") Long id);

    IPage<WorkOrderRespVO> selectJoinPage(IPage<WorkOrderRespVO> page, @Param("req") WorkOrderPageReqVO reqVO);

}