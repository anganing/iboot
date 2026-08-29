package com.iboot.pf.module.mdm.service.rescalendar;

import com.iboot.pf.framework.test.core.ut.BaseDbUnitTest;
import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.ResCalendarListReqVO;
import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.ResCalendarRespVO;
import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.ResCalendarSaveReqVO;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import com.iboot.pf.module.mdm.dal.dataobject.shift.ShiftDO;
import com.iboot.pf.module.mdm.dal.dataobject.wc.WcDO;
import com.iboot.pf.module.mdm.dal.mysql.res.ResMapper;
import com.iboot.pf.module.mdm.dal.mysql.shift.ShiftMapper;
import com.iboot.pf.module.mdm.dal.mysql.wc.WcMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(ResCalendarServiceImpl.class)
class ResCalendarServiceImplTest extends BaseDbUnitTest {

    private static final LocalDate DATE_IN = LocalDate.of(2026, 9, 10);
    private static final LocalDate DATE_OUT = LocalDate.of(2026, 10, 10);

    @Resource
    private ResCalendarService resCalendarService;
    @Resource
    private ResMapper resMapper;
    @Resource
    private WcMapper wcMapper;
    @Resource
    private ShiftMapper shiftMapper;

    private Long shiftId;
    private Long resId;
    private Long otherResId;

    @BeforeEach
    void setUp() {
        shiftMapper.insert(ShiftDO.builder().id(1L).code("DAY").name("白班")
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(16, 0)).status(0).build());
        shiftId = 1L;

        wcMapper.insert(WcDO.builder().id(1L).workshopId(1L).code("WC-1").name("加工中心").status(0).build());
        resMapper.insert(ResDO.builder().id(1L).wcId(1L).resType("MACHINE").code("RES-1")
                .name("车床01").status(0).build());
        resMapper.insert(ResDO.builder().id(2L).wcId(1L).resType("MACHINE").code("RES-2")
                .name("车床02").status(0).build());
        resId = 1L;
        otherResId = 2L;
    }

    @Test
    void testGetResCalendarList_filtersByDateRange() {
        createResCalendar(resId, DATE_IN, 480);
        createResCalendar(resId, DATE_OUT, 480);

        List<ResCalendarRespVO> list = resCalendarService.getResCalendarList(
                buildListReqVO(null, DATE_IN, DATE_IN));

        assertEquals(1, list.size());
        assertEquals(DATE_IN, list.get(0).getWorkDate());
    }

    @Test
    void testGetResCalendarList_filtersByResIds() {
        createResCalendar(resId, DATE_IN, 480);
        createResCalendar(otherResId, DATE_IN, 360);

        List<ResCalendarRespVO> filtered = resCalendarService.getResCalendarList(
                buildListReqVO(List.of(resId), DATE_IN, DATE_IN));
        assertEquals(1, filtered.size());
        assertEquals(resId, filtered.get(0).getResId());

        List<ResCalendarRespVO> all = resCalendarService.getResCalendarList(
                buildListReqVO(null, DATE_IN, DATE_IN));
        assertEquals(2, all.size());
    }

    @Test
    void testGetResCalendarList_joinsRelationFields() {
        createResCalendar(resId, DATE_IN, 480);

        ResCalendarRespVO respVO = resCalendarService.getResCalendarList(
                buildListReqVO(null, DATE_IN, DATE_IN)).get(0);

        assertEquals("RES-1", respVO.getResCode());
        assertEquals("车床01", respVO.getResName());
        assertEquals("DAY", respVO.getShiftCode());
        assertEquals("白班", respVO.getShiftName());
    }

    @Test
    void testCreateAndUpdateResCalendar() {
        Long id = createResCalendar(resId, DATE_IN, 480);

        ResCalendarRespVO created = resCalendarService.getResCalendar(id);
        assertEquals(resId, created.getResId());
        assertEquals(480, created.getAvailableMinutes());

        ResCalendarSaveReqVO updateReqVO = buildSaveReqVO(resId, DATE_IN, 360);
        updateReqVO.setId(id);
        resCalendarService.updateResCalendar(updateReqVO);

        assertEquals(360, resCalendarService.getResCalendar(id).getAvailableMinutes());
    }

    private Long createResCalendar(Long calendarResId, LocalDate workDate, Integer availableMinutes) {
        return resCalendarService.createResCalendar(buildSaveReqVO(calendarResId, workDate, availableMinutes));
    }

    private ResCalendarSaveReqVO buildSaveReqVO(Long calendarResId, LocalDate workDate, Integer availableMinutes) {
        ResCalendarSaveReqVO reqVO = new ResCalendarSaveReqVO();
        reqVO.setResId(calendarResId);
        reqVO.setWorkDate(workDate);
        reqVO.setShiftId(shiftId);
        reqVO.setAvailableMinutes(availableMinutes);
        return reqVO;
    }

    private ResCalendarListReqVO buildListReqVO(List<Long> resIds, LocalDate dateFrom, LocalDate dateTo) {
        ResCalendarListReqVO reqVO = new ResCalendarListReqVO();
        reqVO.setResIds(resIds);
        reqVO.setDateFrom(dateFrom);
        reqVO.setDateTo(dateTo);
        return reqVO;
    }

}
