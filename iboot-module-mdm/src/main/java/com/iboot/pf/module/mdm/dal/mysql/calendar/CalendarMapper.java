package com.iboot.pf.module.mdm.dal.mysql.calendar;

import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.module.mdm.controller.admin.calendar.vo.CalendarPageReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.calendar.CalendarDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日历主档 Mapper
 *
 * @author iboot
 */
@Mapper
public interface CalendarMapper extends BaseMapperX<CalendarDO> {

    default PageResult<CalendarDO> selectPage(CalendarPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CalendarDO>()
                .likeIfPresent(CalendarDO::getName, reqVO.getName())
                .eqIfPresent(CalendarDO::getCode, reqVO.getCode())
                .eqIfPresent(CalendarDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(CalendarDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CalendarDO::getId));
    }

    default CalendarDO selectByCode(String code) {
        return selectOne(CalendarDO::getCode, code);
    }

}
