package com.iboot.pf.module.mdm.service.calendar;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.controller.admin.calendar.vo.CalendarExceptionSaveReqVO;
import com.iboot.pf.module.mdm.controller.admin.calendar.vo.CalendarGenerateReqVO;
import com.iboot.pf.module.mdm.controller.admin.calendar.vo.CalendarSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.calendar.CalendarExceptionDO;
import com.iboot.pf.module.mdm.dal.dataobject.calendar.CalendarRuleDO;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import com.iboot.pf.module.mdm.dal.dataobject.rescalendar.ResCalendarDO;
import com.iboot.pf.module.mdm.dal.dataobject.shift.ShiftDO;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.dal.mysql.calendar.CalendarExceptionMapper;
import com.iboot.pf.module.mdm.dal.mysql.calendar.CalendarMapper;
import com.iboot.pf.module.mdm.dal.mysql.calendar.CalendarRuleMapper;
import com.iboot.pf.module.mdm.dal.mysql.res.ResMapper;
import com.iboot.pf.module.mdm.dal.mysql.rescalendar.ResCalendarMapper;
import com.iboot.pf.module.mdm.dal.mysql.shift.ShiftMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import com.iboot.pf.module.mdm.service.coderule.MdmCodeService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Import(CalendarServiceImpl.class)
class CalendarServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CalendarService calendarService;
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
    private WcMapper wcMapper;
    @Resource
    private ShiftMapper shiftMapper;

    @MockitoBean
    private MdmCodeService mdmCodeService;

    private Long calendarId;
    private Long shiftId;
    private Long resId;

    /**
     * 2026-08-31 是周一，用它覆盖“周一白班”规则。
     */
    private static final LocalDate MONDAY = LocalDate.of(2026, 8, 31);

    @BeforeEach
    void setUp() {
        when(mdmCodeService.resolveCode(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        shiftMapper.insert(ShiftDO.builder().id(1L).code("DAY").name("白班")
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(16, 0)).status(0).build());
        shiftId = 1L;

        wcMapper.insert(WcDO.builder().id(1L).workshopId(1L).code("WC-1").name("加工中心").status(0).build());
        resMapper.insert(ResDO.builder().id(1L).wcId(1L).resType("MACHINE").code("RES-1")
                .name("车床01").status(0).build());
        resId = 1L;

        CalendarSaveReqVO saveReqVO = new CalendarSaveReqVO();
        saveReqVO.setCode("CAL-001");
        saveReqVO.setName("标准双班");
        saveReqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        calendarId = calendarService.createCalendar(saveReqVO);
    }

    @Test
    void testGenerateResCalendars_isIdempotent() {
        createRule(1, 480);
        CalendarGenerateReqVO reqVO = buildGenerateReqVO(MONDAY, MONDAY);

        List<ResCalendarDO> first = calendarService.generateResCalendars(reqVO);
        assertEquals(1, first.size());
        assertEquals(480, first.get(0).getAvailableMinutes());
        assertTrue(first.get(0).getGeneratedFlag());
        assertEquals(calendarId, first.get(0).getSourceCalendarId());

        // 重复执行不产生重复行
        calendarService.generateResCalendars(reqVO);
        assertEquals(1, resCalendarMapper.selectListByResIdsAndDateRange(List.of(resId), MONDAY, MONDAY).size());
    }

    @Test
    void testGenerateResCalendars_exceptionOverridesRule() {
        createRule(1, 480);
        // 周一白班加班：可用分钟改为 720
        createException(MONDAY, "OVERTIME", shiftId, 720);

        calendarService.generateResCalendars(buildGenerateReqVO(MONDAY, MONDAY));

        assertEquals(720, selectSingleRow().getAvailableMinutes());
    }

    @Test
    void testGenerateResCalendars_holidaySkipsRow() {
        createRule(1, 480);
        createException(MONDAY, "HOLIDAY", shiftId, null);

        calendarService.generateResCalendars(buildGenerateReqVO(MONDAY, MONDAY));

        // 停工例外：该班次可用分钟为 0，不生成日历行
        assertTrue(resCalendarMapper.selectListByResIdsAndDateRange(List.of(resId), MONDAY, MONDAY).isEmpty());
    }

    @Test
    void testGenerateResCalendars_manualRowNotOverridden() {
        createRule(1, 480);
        // 人工调整行：generated_flag = false
        resCalendarMapper.insert(ResCalendarDO.builder()
                .resId(resId).workDate(MONDAY).shiftId(shiftId)
                .availableMinutes(240).sourceCalendarId(0L).generatedFlag(false).build());

        calendarService.generateResCalendars(buildGenerateReqVO(MONDAY, MONDAY));

        ResCalendarDO row = selectSingleRow();
        assertEquals(240, row.getAvailableMinutes());
        assertFalse(row.getGeneratedFlag());
    }

    private CalendarGenerateReqVO buildGenerateReqVO(LocalDate dateFrom, LocalDate dateTo) {
        CalendarGenerateReqVO reqVO = new CalendarGenerateReqVO();
        reqVO.setCalendarId(calendarId);
        reqVO.setResIds(List.of(resId));
        reqVO.setDateFrom(dateFrom);
        reqVO.setDateTo(dateTo);
        return reqVO;
    }

    private void createRule(Integer dayOfWeek, Integer availableMinutes) {
        calendarRuleMapper.insert(CalendarRuleDO.builder()
                .calendarId(calendarId).dayOfWeek(dayOfWeek).shiftId(shiftId)
                .availableMinutes(availableMinutes).build());
    }

    private void createException(LocalDate date, String type, Long exceptionShiftId, Integer minutes) {
        CalendarExceptionSaveReqVO reqVO = new CalendarExceptionSaveReqVO();
        reqVO.setCalendarId(calendarId);
        reqVO.setExceptionDate(date);
        reqVO.setExceptionType(type);
        reqVO.setShiftId(exceptionShiftId);
        reqVO.setAvailableMinutes(minutes);
        calendarService.createCalendarException(reqVO);
    }

    private ResCalendarDO selectSingleRow() {
        List<ResCalendarDO> rows = resCalendarMapper.selectListByResIdsAndDateRange(List.of(resId), MONDAY, MONDAY);
        assertEquals(1, rows.size());
        return rows.get(0);
    }

}
