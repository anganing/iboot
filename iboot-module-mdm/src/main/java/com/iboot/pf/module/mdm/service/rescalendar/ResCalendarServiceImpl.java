package com.iboot.pf.module.mdm.service.rescalendar;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDate;
import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.rescalendar.ResCalendarDO;
import com.iboot.pf.module.mdm.dal.dataobject.res.ResDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mdm.dal.mysql.rescalendar.ResCalendarMapper;
import com.iboot.pf.module.mdm.dal.mysql.res.ResMapper;
import com.iboot.pf.module.mdm.dal.mysql.shift.ShiftMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertMap;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.diffList;
import static com.iboot.pf.module.mdm.enums.ErrorCodeConstants.*;

/**
 * 资源日历 Service 实现类
 *
 * @author iboot
 */
@Service
@Validated
public class ResCalendarServiceImpl implements ResCalendarService {

    @Resource
    private ResCalendarMapper resCalendarMapper;
    @Resource
    private ResMapper resMapper;
    @Resource
    private ShiftMapper shiftMapper;

    @Override
    public Long createResCalendar(ResCalendarSaveReqVO createReqVO) {
        validateReferences(createReqVO.getResId(), createReqVO.getShiftId());
        // 插入
        ResCalendarDO resCalendar = BeanUtils.toBean(createReqVO, ResCalendarDO.class);
        resCalendar.setId(null);
        resCalendarMapper.insert(resCalendar);

        // 返回
        return resCalendar.getId();
    }

    @Override
    public void updateResCalendar(ResCalendarSaveReqVO updateReqVO) {
        // 校验存在
        validateResCalendarExists(updateReqVO.getId());
        validateReferences(updateReqVO.getResId(), updateReqVO.getShiftId());
        // 更新
        ResCalendarDO updateObj = BeanUtils.toBean(updateReqVO, ResCalendarDO.class);
        resCalendarMapper.updateById(updateObj);
    }

    @Override
    public void deleteResCalendar(Long id) {
        // 校验存在
        validateResCalendarExists(id);
        // 删除
        resCalendarMapper.deleteById(id);
    }

    @Override
        public void deleteResCalendarListByIds(List<Long> ids) {
        // 删除
        resCalendarMapper.deleteByIds(ids);
        }


    private void validateResCalendarExists(Long id) {
        if (resCalendarMapper.selectById(id) == null) {
            throw exception(RES_CALENDAR_NOT_EXISTS);
        }
    }

    private void validateReferences(Long resId, Long shiftId) {
        if (resMapper.selectById(resId) == null) {
            throw exception(RES_NOT_EXISTS);
        }
        if (shiftMapper.selectById(shiftId) == null) {
            throw exception(SHIFT_NOT_EXISTS);
        }
    }

    @Override
    public ResCalendarRespVO getResCalendar(Long id) {
        return resCalendarMapper.selectJoinById(id);
    }

    @Override
    public PageResult<ResCalendarRespVO> getResCalendarPage(ResCalendarPageReqVO pageReqVO) {
        return resCalendarMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ResCalendarRespVO> getResCalendarList(ResCalendarListReqVO listReqVO) {
        return resCalendarMapper.selectJoinList(listReqVO);
    }

    @Override
    public List<ResCalendarDO> getResCalendarList(Collection<Long> resIds, LocalDate startDate, LocalDate endDate) {
        if (CollUtil.isEmpty(resIds)) {
            return List.of();
        }
        return resCalendarMapper.selectListByResIdsAndDateRange(new ArrayList<>(resIds), startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResCalendarBatchSaveRespVO saveResCalendarBatch(ResCalendarBatchSaveReqVO batchReqVO) {
        if (batchReqVO.getDateFrom().isAfter(batchReqVO.getDateTo())) {
            throw exception(RES_CALENDAR_DATE_RANGE_INVALID);
        }
        // 校验资源与班次都存在，避免批量写入脏数据
        List<Long> resIds = batchReqVO.getResIds().stream().distinct().toList();
        if (resMapper.selectByIds(resIds).size() != resIds.size()) {
            throw exception(RES_NOT_EXISTS);
        }
        if (shiftMapper.selectById(batchReqVO.getShiftId()) == null) {
            throw exception(SHIFT_NOT_EXISTS);
        }

        // 展开日期：按星期过滤，weekdays 为空表示区间内每天都排
        List<LocalDate> workDates = expandWorkDates(batchReqVO);
        if (workDates.isEmpty()) {
            throw exception(RES_CALENDAR_BATCH_EMPTY);
        }

        // 一次查出区间内已有记录，避免逐条 select 造成 N+1。
        // 唯一索引是 (res_id, work_date, shift_id)，同一资源同一天允许多个班次，所以按三元组对齐
        Map<String, ResCalendarDO> existsMap = convertMap(
                resCalendarMapper.selectListByResIdsAndDateRange(resIds, batchReqVO.getDateFrom(), batchReqVO.getDateTo()),
                item -> buildScheduleKey(item.getResId(), item.getWorkDate(), item.getShiftId()));

        List<ResCalendarDO> createList = new ArrayList<>();
        List<ResCalendarDO> updateList = new ArrayList<>();
        for (Long resId : resIds) {
            for (LocalDate workDate : workDates) {
                ResCalendarDO exists = existsMap.get(
                        buildScheduleKey(resId, workDate, batchReqVO.getShiftId()));
                if (exists == null) {
                    createList.add(ResCalendarDO.builder()
                            .resId(resId)
                            .workDate(workDate)
                            .shiftId(batchReqVO.getShiftId())
                            .availableMinutes(batchReqVO.getAvailableMinutes())
                            .remark(batchReqVO.getRemark())
                            .build());
                    continue;
                }
                // 已存在同资源、同日期、同班次的排班，覆盖产能与备注；同日期的其他班次不受影响
                ResCalendarDO updateObj = new ResCalendarDO();
                updateObj.setId(exists.getId());
                updateObj.setAvailableMinutes(batchReqVO.getAvailableMinutes());
                updateObj.setRemark(batchReqVO.getRemark());
                updateList.add(updateObj);
            }
        }

        if (CollUtil.isNotEmpty(createList)) {
            resCalendarMapper.insertBatch(createList);
        }
        updateList.forEach(resCalendarMapper::updateById);
        return new ResCalendarBatchSaveRespVO(createList.size(), updateList.size());
    }

    /**
     * 把日期区间按星期过滤展开成逐日列表。
     */
    private List<LocalDate> expandWorkDates(ResCalendarBatchSaveReqVO batchReqVO) {
        Set<Integer> weekdays = CollUtil.isEmpty(batchReqVO.getWeekdays())
                ? Set.of() : new HashSet<>(batchReqVO.getWeekdays());
        List<LocalDate> workDates = new ArrayList<>();
        for (LocalDate date = batchReqVO.getDateFrom(); !date.isAfter(batchReqVO.getDateTo()); date = date.plusDays(1)) {
            if (weekdays.isEmpty() || weekdays.contains(date.getDayOfWeek().getValue())) {
                workDates.add(date);
            }
        }
        return workDates;
    }

    private String buildScheduleKey(Long resId, LocalDate workDate, Long shiftId) {
        return resId + "_" + workDate + "_" + shiftId;
    }

}
