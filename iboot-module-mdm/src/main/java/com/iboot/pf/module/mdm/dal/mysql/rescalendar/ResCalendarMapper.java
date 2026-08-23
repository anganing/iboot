package com.iboot.pf.module.mdm.dal.mysql.rescalendar;

import java.util.*;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.module.mdm.dal.dataobject.rescalendar.ResCalendarDO;
import org.apache.ibatis.annotations.Mapper;
import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.*;

/**
 * 资源日历 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ResCalendarMapper extends BaseMapperX<ResCalendarDO> {

    default PageResult<ResCalendarDO> selectPage(ResCalendarPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ResCalendarDO>()
                .eqIfPresent(ResCalendarDO::getResId, reqVO.getResId())
                .betweenIfPresent(ResCalendarDO::getWorkDate, reqVO.getWorkDate())
                .eqIfPresent(ResCalendarDO::getShiftId, reqVO.getShiftId())
                .eqIfPresent(ResCalendarDO::getAvailableMinutes, reqVO.getAvailableMinutes())
                .eqIfPresent(ResCalendarDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(ResCalendarDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ResCalendarDO::getId));
    }

}