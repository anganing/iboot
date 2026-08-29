package com.iboot.pf.module.mdm.service.calendar;

import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.util.object.BeanUtils;
import com.iboot.pf.module.mdm.controller.admin.calendar.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.calendar.CalendarDO;
import com.iboot.pf.module.mdm.dal.dataobject.calendar.CalendarExceptionDO;
import com.iboot.pf.module.mdm.dal.dataobject.calendar.CalendarRuleDO;
import com.iboot.pf.module.mdm.dal.dataobject.rescalendar.ResCalendarDO;
import com.iboot.pf.module.mdm.dal.dataobject.shift.ShiftDO;
import com.iboot.pf.module.mdm.dal.mysql.calendar.CalendarExceptionMapper;
import com.iboot.pf.module.mdm.dal.mysql.calendar.CalendarMapper;
import com.iboot.pf.module.mdm.dal.mysql.calendar.CalendarRuleMapper;
import com.iboot.pf.module.mdm.dal.mysql.rescalendar.ResCalendarMapper;
import com.iboot.pf.module.mdm.dal.mysql.res.ResMapper;
import com.iboot.pf.module.mdm.dal.mysql.shift.ShiftMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertSet;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;
import static com.iboot.pf.module.mdm.enums.MdmCodeRuleConstants.CALENDAR;

/**
 * 日历模板 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class CalendarServiceImpl implements CalendarService {

    private static final String EXCEPTION_TYPE_HOLIDAY = "HOLIDAY";

    @Resource
    private CalendarMapper calendarMapper;
    @Resource
    private CalendarRuleMapper calendarRuleMapper;
    @Resource
    private CalendarExceptionMapper calendarExceptionMapper;
    @Resource
    private ResCalendarMapper resCalendarMapper;
    @Resource
    private ResMapper resMapper;
    @Resource
    private ShiftMapper shiftMapper;
    @Resource
    private MdmCodeService mdmCodeService;

    // ==================== 日历主档 ====================

    @Override
    public Long createCalendar(CalendarSaveReqVO createReqVO) {
        validateCodeUnique(null, createReqVO.getCode());
        CalendarDO calendar = BeanUtils.toBean(createReqVO, CalendarDO.class);
        calendar.setId(null);
        calendar.setCode(mdmCodeService.resolveCode(calendar.getCode(), CALENDAR));
        calendarMapper.insert(calendar);
        return calendar.getId();
    }

    @Override
    public void updateCalendar(CalendarSaveReqVO updateReqVO) {
        validateCalendarExists(updateReqVO.getId());
        validateCodeUnique(updateReqVO.getId(), updateReqVO.getCode());
        CalendarDO updateObj = BeanUtils.toBean(updateReqVO, CalendarDO.class);
        calendarMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCalendar(Long id) {
        validateCalendarExists(id);
        calendarRuleMapper.deleteByCalendarId(id);
        calendarExceptionMapper.deleteByCalendarId(id);
        calendarMapper.deleteById(id);
    }

    @Override
    public CalendarRespVO getCalendar(Long id) {
        return BeanUtils.toBean(validateCalendarExists(id), CalendarRespVO.class);
    }

    @Override
    public PageResult<CalendarRespVO> getCalendarPage(CalendarPageReqVO pageReqVO) {
        PageResult<CalendarDO> pageResult = calendarMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(pageResult, CalendarRespVO.class);
    }

    private CalendarDO validateCalendarExists(Long id) {
        CalendarDO calendar = calendarMapper.selectById(id);
        if (calendar == null) {
            throw exception(CALENDAR_NOT_EXISTS);
        }
        return calendar;
    }

    private void validateCodeUnique(Long id, String code) {
        if (code == null) {
            return;
        }
        CalendarDO existing = calendarMapper.selectByCode(code);
        if (existing != null && !existing.getId().equals(id)) {
            throw exception(CALENDAR_CODE_DUPLICATE);
        }
    }

    // ==================== 常规规则 ====================

    @Override
    public Long createCalendarRule(CalendarRuleSaveReqVO createReqVO) {
        validateCalendarExists(createReqVO.getCalendarId());
        validateShiftExists(createReqVO.getShiftId());
        validateRuleUnique(createReqVO.getCalendarId(), createReqVO.getDayOfWeek(), createReqVO.getShiftId(), null);
        CalendarRuleDO rule = BeanUtils.toBean(createReqVO, CalendarRuleDO.class);
        rule.setId(null);
        calendarRuleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    public void updateCalendarRule(CalendarRuleSaveReqVO updateReqVO) {
        validateRuleExists(updateReqVO.getId());
        validateCalendarExists(updateReqVO.getCalendarId());
        validateShiftExists(updateReqVO.getShiftId());
        validateRuleUnique(updateReqVO.getCalendarId(), updateReqVO.getDayOfWeek(), updateReqVO.getShiftId(),
                updateReqVO.getId());
        CalendarRuleDO updateObj = BeanUtils.toBean(updateReqVO, CalendarRuleDO.class);
        calendarRuleMapper.updateById(updateObj);
    }

    @Override
    public void deleteCalendarRule(Long id) {
        validateRuleExists(id);
        calendarRuleMapper.deleteById(id);
    }

    @Override
    public void deleteCalendarRuleListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteCalendarRule(id);
        }
    }

    @Override
    public CalendarRuleRespVO getCalendarRule(Long id) {
        return calendarRuleMapper.selectJoinById(id);
    }

    @Override
    public PageResult<CalendarRuleRespVO> getCalendarRulePage(PageParam pageParam, Long calendarId) {
        return calendarRuleMapper.selectJoinPage(pageParam, calendarId);
    }

    private CalendarRuleDO validateRuleExists(Long id) {
        CalendarRuleDO rule = calendarRuleMapper.selectById(id);
        if (rule == null) {
            throw exception(CALENDAR_RULE_NOT_EXISTS);
        }
        return rule;
    }

    private void validateRuleUnique(Long calendarId, Integer dayOfWeek, Long shiftId, Long excludeId) {
        CalendarRuleDO existing = calendarRuleMapper.selectByCalendarIdAndDayAndShift(calendarId, dayOfWeek, shiftId);
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw exception(CALENDAR_RULE_DUPLICATE);
        }
    }

    // ==================== 日期例外 ====================

    @Override
    public Long createCalendarException(CalendarExceptionSaveReqVO createReqVO) {
        validateCalendarExists(createReqVO.getCalendarId());
        validateExceptionFields(createReqVO);
        validateExceptionUnique(createReqVO.getCalendarId(), createReqVO.getExceptionDate(), createReqVO.getShiftId(), null);
        CalendarExceptionDO exceptionDO = BeanUtils.toBean(createReqVO, CalendarExceptionDO.class);
        exceptionDO.setId(null);
        calendarExceptionMapper.insert(exceptionDO);
        return exceptionDO.getId();
    }

    @Override
    public void updateCalendarException(CalendarExceptionSaveReqVO updateReqVO) {
        validateExceptionExists(updateReqVO.getId());
        validateCalendarExists(updateReqVO.getCalendarId());
        validateExceptionFields(updateReqVO);
        validateExceptionUnique(updateReqVO.getCalendarId(), updateReqVO.getExceptionDate(), updateReqVO.getShiftId(),
                updateReqVO.getId());
        CalendarExceptionDO updateObj = BeanUtils.toBean(updateReqVO, CalendarExceptionDO.class);
        calendarExceptionMapper.updateById(updateObj);
    }

    @Override
    public void deleteCalendarException(Long id) {
        validateExceptionExists(id);
        calendarExceptionMapper.deleteById(id);
    }

    @Override
    public void deleteCalendarExceptionListByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteCalendarException(id);
        }
    }

    @Override
    public CalendarExceptionRespVO getCalendarException(Long id) {
        return calendarExceptionMapper.selectJoinById(id);
    }

    @Override
    public PageResult<CalendarExceptionRespVO> getCalendarExceptionPage(PageParam pageParam, Long calendarId) {
        return calendarExceptionMapper.selectJoinPage(pageParam, calendarId);
    }

    private CalendarExceptionDO validateExceptionExists(Long id) {
        CalendarExceptionDO exceptionDO = calendarExceptionMapper.selectById(id);
        if (exceptionDO == null) {
            throw exception(CALENDAR_EXCEPTION_NOT_EXISTS);
        }
        return exceptionDO;
    }

    private void validateExceptionFields(CalendarExceptionSaveReqVO reqVO) {
        // 仅停工例外支持全天（班次为空）；加班与保养必须指定班次
        if (reqVO.getShiftId() == null) {
            if (!EXCEPTION_TYPE_HOLIDAY.equals(reqVO.getExceptionType())) {
                throw exception(CALENDAR_EXCEPTION_SHIFT_REQUIRED);
            }
            return;
        }
        validateShiftExists(reqVO.getShiftId());
        // 非停工例外必须填写可用分钟数
        if (!EXCEPTION_TYPE_HOLIDAY.equals(reqVO.getExceptionType()) && reqVO.getAvailableMinutes() == null) {
            throw exception(CALENDAR_EXCEPTION_MINUTES_REQUIRED);
        }
    }

    private void validateExceptionUnique(Long calendarId, LocalDate exceptionDate, Long shiftId, Long excludeId) {
        CalendarExceptionDO existing =
                calendarExceptionMapper.selectByCalendarIdAndDateAndShift(calendarId, exceptionDate, shiftId);
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw exception(CALENDAR_EXCEPTION_DUPLICATE);
        }
    }

    private void validateShiftExists(Long shiftId) {
        if (shiftMapper.selectById(shiftId) == null) {
            throw exception(SHIFT_NOT_EXISTS);
        }
    }

    // ==================== 资源日历生成 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ResCalendarDO> generateResCalendars(CalendarGenerateReqVO generateReqVO) {
        validateCalendarExists(generateReqVO.getCalendarId());
        if (generateReqVO.getDateTo().isBefore(generateReqVO.getDateFrom())) {
            throw exception(CALENDAR_GENERATE_DATE_INVALID);
        }
        List<Long> resIds = generateReqVO.getResIds();
        if (resMapper.selectCountByIds(resIds) != resIds.size()) {
            throw exception(RES_NOT_EXISTS);
        }

        List<CalendarRuleDO> rules = calendarRuleMapper.selectListByCalendarId(generateReqVO.getCalendarId());
        List<ResCalendarDO> generatedRows = new ArrayList<>();
        for (Long resId : resIds) {
            generatedRows.addAll(generateResCalendar(
                    resId, generateReqVO.getCalendarId(), generateReqVO.getDateFrom(), generateReqVO.getDateTo(), rules));
        }
        return generatedRows;
    }

    /**
     * 为单个资源按模板展开生成日历明细：先清理区间内模板生成的旧行，再按规则与例外重算。
     */
    private List<ResCalendarDO> generateResCalendar(Long resId, Long calendarId, LocalDate dateFrom,
                                                    LocalDate dateTo, List<CalendarRuleDO> allRules) {
        // 幂等第一步：仅清理模板生成行，人工调整行（generated_flag = false）保留
        resCalendarMapper.deleteGeneratedByResIdsAndDateRange(List.of(resId), dateFrom, dateTo);
        Set<String> manualKeys = new HashSet<>();
        for (ResCalendarDO row : resCalendarMapper.selectListByResIdsAndDateRange(List.of(resId), dateFrom, dateTo)) {
            manualKeys.add(row.getResId() + "-" + row.getWorkDate() + "-" + row.getShiftId());
        }

        List<ResCalendarDO> rows = new ArrayList<>();
        for (LocalDate date = dateFrom; !date.isAfter(dateTo); date = date.plusDays(1)) {
            int dayOfWeek = date.getDayOfWeek().getValue();
            List<CalendarExceptionDO> exceptions =
                    calendarExceptionMapper.selectListByCalendarIdAndDate(calendarId, date);
            // 全天停工例外：该日不生成任何班次行
            if (hasFullDayHoliday(exceptions)) {
                continue;
            }
            for (CalendarRuleDO rule : allRules) {
                if (rule.getDayOfWeek() != dayOfWeek) {
                    continue;
                }
                Integer minutes = resolveAvailableMinutes(rule, exceptions);
                if (minutes == null || minutes <= 0) {
                    continue;
                }
                String key = resId + "-" + date + "-" + rule.getShiftId();
                // 人工调整行优先：同键的人工行不覆盖
                if (manualKeys.contains(key)) {
                    continue;
                }
                rows.add(ResCalendarDO.builder()
                        .resId(resId)
                        .workDate(date)
                        .shiftId(rule.getShiftId())
                        .availableMinutes(minutes)
                        .sourceCalendarId(calendarId)
                        .generatedFlag(true)
                        .build());
            }
        }
        resCalendarMapper.insertBatch(rows);
        return rows;
    }

    private boolean hasFullDayHoliday(List<CalendarExceptionDO> exceptions) {
        return exceptions.stream()
                .anyMatch(item -> item.getShiftId() == null && EXCEPTION_TYPE_HOLIDAY.equals(item.getExceptionType()));
    }

    /**
     * 计算某班次的可用分钟：例外覆盖常规规则。
     */
    private Integer resolveAvailableMinutes(CalendarRuleDO rule, List<CalendarExceptionDO> exceptions) {
        CalendarExceptionDO matched = exceptions.stream()
                .filter(item -> rule.getShiftId().equals(item.getShiftId()))
                .findFirst().orElse(null);
        if (matched == null) {
            return rule.getAvailableMinutes();
        }
        // 停工例外：可用分钟为 0
        if (EXCEPTION_TYPE_HOLIDAY.equals(matched.getExceptionType())) {
            return 0;
        }
        return matched.getAvailableMinutes();
    }

}
