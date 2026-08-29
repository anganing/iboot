package com.iboot.pf.module.mdm.service.calendar;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.module.mdm.controller.admin.calendar.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.rescalendar.ResCalendarDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 日历模板 Service 接口
 *
 * @author iboot
 */
public interface CalendarService {

    // ==================== 日历主档 ====================

    /**
     * 创建日历
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCalendar(@Valid CalendarSaveReqVO createReqVO);

    /**
     * 更新日历
     *
     * @param updateReqVO 更新信息
     */
    void updateCalendar(@Valid CalendarSaveReqVO updateReqVO);

    /**
     * 删除日历（级联删除规则与例外）
     *
     * @param id 编号
     */
    void deleteCalendar(Long id);

    /**
     * 获得日历
     *
     * @param id 编号
     * @return 日历
     */
    CalendarRespVO getCalendar(Long id);

    /**
     * 获得日历分页
     *
     * @param pageReqVO 分页查询
     * @return 日历分页
     */
    PageResult<CalendarRespVO> getCalendarPage(CalendarPageReqVO pageReqVO);

    // ==================== 常规规则 ====================

    /**
     * 创建日历规则
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCalendarRule(@Valid CalendarRuleSaveReqVO createReqVO);

    /**
     * 更新日历规则
     *
     * @param updateReqVO 更新信息
     */
    void updateCalendarRule(@Valid CalendarRuleSaveReqVO updateReqVO);

    /**
     * 删除日历规则
     *
     * @param id 编号
     */
    void deleteCalendarRule(Long id);

    /**
     * 批量删除日历规则
     *
     * @param ids 编号列表
     */
    void deleteCalendarRuleListByIds(List<Long> ids);

    /**
     * 获得日历规则
     *
     * @param id 编号
     * @return 日历规则
     */
    CalendarRuleRespVO getCalendarRule(Long id);
    /**
     * 获得日历规则分页
     *
     * @param pageParam 分页参数
     * @param calendarId 日历编号
     * @return 日历规则分页
     */
    PageResult<CalendarRuleRespVO> getCalendarRulePage(PageParam pageParam, Long calendarId);

    // ==================== 日期例外 ====================

    /**
     * 创建日历例外
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCalendarException(@Valid CalendarExceptionSaveReqVO createReqVO);

    /**
     * 更新日历例外
     *
     * @param updateReqVO 更新信息
     */
    void updateCalendarException(@Valid CalendarExceptionSaveReqVO updateReqVO);

    /**
     * 删除日历例外
     *
     * @param id 编号
     */
    void deleteCalendarException(Long id);

    /**
     * 批量删除日历例外
     *
     * @param ids 编号列表
     */
    void deleteCalendarExceptionListByIds(List<Long> ids);

    /**
     * 获得日历例外
     *
     * @param id 编号
     * @return 日历例外
     */
    CalendarExceptionRespVO getCalendarException(Long id);
    /**
     * 获得日历例外分页
     *
     * @param pageParam 分页参数
     * @param calendarId 日历编号
     * @return 日历例外分页
     */
    PageResult<CalendarExceptionRespVO> getCalendarExceptionPage(PageParam pageParam, Long calendarId);

    // ==================== 资源日历生成 ====================

    /**
     * 按日历模板为资源展开生成资源日历，幂等：重复执行不产生重复行；
     * generated_flag = false 的人工调整行不被覆盖。
     *
     * @param generateReqVO 生成请求
     * @return 本次生成的日历明细行数
     */
    List<ResCalendarDO> generateResCalendars(@Valid CalendarGenerateReqVO generateReqVO);

}
