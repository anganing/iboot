package com.iboot.pf.module.mdm.service.rescalendar;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.rescalendar.ResCalendarDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;
import com.iboot.pf.framework.common.util.object.BeanUtils;

import com.iboot.pf.module.mdm.dal.mysql.rescalendar.ResCalendarMapper;
import com.iboot.pf.module.mdm.dal.mysql.res.ResMapper;
import com.iboot.pf.module.mdm.dal.mysql.shift.ShiftMapper;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.framework.common.util.collection.CollectionUtils.convertList;
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
    public ResCalendarDO getResCalendar(Long id) {
        return resCalendarMapper.selectById(id);
    }

    @Override
    public PageResult<ResCalendarDO> getResCalendarPage(ResCalendarPageReqVO pageReqVO) {
        return resCalendarMapper.selectPage(pageReqVO);
    }

}
