package com.iboot.pf.module.mdm.dal.mysql.rescalendar;

import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.dal.dataobject.rescalendar.ResCalendarDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.*;

/**
 * 资源日历 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ResCalendarMapper extends BaseMapperX<ResCalendarDO> {

    default PageResult<ResCalendarRespVO> selectPage(ResCalendarPageReqVO reqVO) {
        IPage<ResCalendarRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    ResCalendarRespVO selectJoinById(@Param("id") Long id);

    IPage<ResCalendarRespVO> selectJoinPage(IPage<ResCalendarRespVO> page, @Param("req") ResCalendarPageReqVO reqVO);

}
