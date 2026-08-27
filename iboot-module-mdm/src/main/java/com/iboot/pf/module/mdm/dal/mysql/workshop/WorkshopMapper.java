package com.iboot.pf.module.mdm.dal.mysql.workshop;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.dal.dataobject.workshop.WorkshopDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mdm.controller.admin.workshop.vo.*;

/**
 * 车间 Mapper
 *
 * @author iboot
 */
@Mapper
public interface WorkshopMapper extends BaseMapperX<WorkshopDO> {

    default PageResult<WorkshopRespVO> selectPage(WorkshopPageReqVO reqVO) {
        IPage<WorkshopRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    WorkshopRespVO selectJoinById(@Param("id") Long id);

    IPage<WorkshopRespVO> selectJoinPage(IPage<WorkshopRespVO> page, @Param("req") WorkshopPageReqVO reqVO);

}
