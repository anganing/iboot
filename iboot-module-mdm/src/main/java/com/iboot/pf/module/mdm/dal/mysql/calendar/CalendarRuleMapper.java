package com.iboot.pf.module.mdm.dal.mysql.calendar;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.controller.admin.calendar.vo.CalendarRuleRespVO;
import com.iboot.pf.module.mdm.dal.dataobject.calendar.CalendarRuleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 日历常规规则 Mapper
 *
 * @author iboot
 */
@Mapper
public interface CalendarRuleMapper extends BaseMapperX<CalendarRuleDO> {

    default List<CalendarRuleDO> selectListByCalendarId(Long calendarId) {
        return selectList(new LambdaQueryWrapperX<CalendarRuleDO>()
                .eq(CalendarRuleDO::getCalendarId, calendarId)
                .orderByAsc(CalendarRuleDO::getDayOfWeek));
    }

    default CalendarRuleDO selectByCalendarIdAndDayAndShift(Long calendarId, Integer dayOfWeek, Long shiftId) {
        return selectOne(new LambdaQueryWrapperX<CalendarRuleDO>()
                .eq(CalendarRuleDO::getCalendarId, calendarId)
                .eq(CalendarRuleDO::getDayOfWeek, dayOfWeek)
                .eq(CalendarRuleDO::getShiftId, shiftId));
    }

    default int deleteByCalendarId(Long calendarId) {
        return delete(CalendarRuleDO::getCalendarId, calendarId);
    }

    default PageResult<CalendarRuleRespVO> selectJoinPage(PageParam pageParam, Long calendarId) {
        IPage<CalendarRuleRespVO> mpPage = MyBatisUtils.buildPage(pageParam);
        selectJoinPage(mpPage, calendarId);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    IPage<CalendarRuleRespVO> selectJoinPage(IPage<CalendarRuleRespVO> page, @Param("calendarId") Long calendarId);

    CalendarRuleRespVO selectJoinById(@Param("id") Long id);

}
