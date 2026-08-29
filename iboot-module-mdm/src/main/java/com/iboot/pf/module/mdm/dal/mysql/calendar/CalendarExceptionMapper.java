package com.iboot.pf.module.mdm.dal.mysql.calendar;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.controller.admin.calendar.vo.CalendarExceptionRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.calendar.CalendarExceptionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 日历例外 Mapper
 *
 * @author iboot
 */
@Mapper
public interface CalendarExceptionMapper extends BaseMapperX<CalendarExceptionDO> {

    default List<CalendarExceptionDO> selectListByCalendarId(Long calendarId) {
        return selectList(new LambdaQueryWrapperX<CalendarExceptionDO>()
                .eq(CalendarExceptionDO::getCalendarId, calendarId)
                .orderByAsc(CalendarExceptionDO::getExceptionDate));
    }

    default List<CalendarExceptionDO> selectListByCalendarIdAndDate(Long calendarId, LocalDate exceptionDate) {
        return selectList(new LambdaQueryWrapperX<CalendarExceptionDO>()
                .eq(CalendarExceptionDO::getCalendarId, calendarId)
                .eq(CalendarExceptionDO::getExceptionDate, exceptionDate));
    }

    default CalendarExceptionDO selectByCalendarIdAndDateAndShift(Long calendarId, LocalDate exceptionDate, Long shiftId) {
        return selectOne(new LambdaQueryWrapperX<CalendarExceptionDO>()
                .eq(CalendarExceptionDO::getCalendarId, calendarId)
                .eq(CalendarExceptionDO::getExceptionDate, exceptionDate)
                .eq(CalendarExceptionDO::getShiftId, shiftId));
    }

    default int deleteByCalendarId(Long calendarId) {
        return delete(CalendarExceptionDO::getCalendarId, calendarId);
    }

    default PageResult<CalendarExceptionRespVO> selectJoinPage(PageParam pageParam, Long calendarId) {
        IPage<CalendarExceptionRespVO> mpPage = MyBatisUtils.buildPage(pageParam);
        selectJoinPage(mpPage, calendarId);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    IPage<CalendarExceptionRespVO> selectJoinPage(IPage<CalendarExceptionRespVO> page, @Param("calendarId") Long calendarId);

    CalendarExceptionRespVO selectJoinById(@Param("id") Long id);

}
