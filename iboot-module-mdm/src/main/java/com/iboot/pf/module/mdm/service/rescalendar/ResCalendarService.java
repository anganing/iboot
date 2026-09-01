package com.iboot.pf.module.mdm.service.rescalendar;

import java.util.*;
import java.time.LocalDate;
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

    /**
     * 按资源编号与日期区间（闭区间）批量查询资源日历
     *
     * @param resIds 资源编号集合
     * @param startDate 起始日期（含）
     * @param endDate 结束日期（含）
     * @return 资源日历列表；空集合返回空列表
     */
    List<ResCalendarDO> getResCalendarList(Collection<Long> resIds, LocalDate startDate, LocalDate endDate);

    /**
     * 批量排班：为指定资源在日期区间内按星期过滤逐日写入资源日历，
     * 已存在（同资源同日期）的记录整体覆盖为新的班次与产能
     *
     * @param batchReqVO 批量排班信息
     * @return 新增与覆盖的条数
     */
    ResCalendarBatchSaveRespVO saveResCalendarBatch(@Valid ResCalendarBatchSaveReqVO batchReqVO);

}
