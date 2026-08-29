package com.iboot.pf.module.mdm.service.rescalendar;

import java.util.*;
import jakarta.validation.*;
import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.*;
import com.iboot.pf.module.mdm.dal.dataobject.rescalendar.ResCalendarDO;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.common.pojo.PageParam;

/**
 * 资源日历 Service 接口
 *
 * @author iboot
 */
public interface ResCalendarService {

    /**
     * 创建资源日历
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createResCalendar(@Valid ResCalendarSaveReqVO createReqVO);

    /**
     * 更新资源日历
     *
     * @param updateReqVO 更新信息
     */
    void updateResCalendar(@Valid ResCalendarSaveReqVO updateReqVO);

    /**
     * 删除资源日历
     *
     * @param id 编号
     */
    void deleteResCalendar(Long id);

    /**
    * 批量删除资源日历
    *
    * @param ids 编号
    */
    void deleteResCalendarListByIds(List<Long> ids);

    /**
     * 获得资源日历
     *
     * @param id 编号
     * @return 资源日历
     */
    ResCalendarRespVO getResCalendar(Long id);

    /**
     * 获得资源日历分页
     *
     * @param pageReqVO 分页查询
     * @return 资源日历分页
     */
    PageResult<ResCalendarRespVO> getResCalendarPage(ResCalendarPageReqVO pageReqVO);

    /**
     * 获得资源日历列表
     *
     * @param listReqVO 列表查询
     * @return 资源日历列表
     */
    List<ResCalendarRespVO> getResCalendarList(@Valid ResCalendarListReqVO listReqVO);

}