package com.iboot.pf.module.mdm.dal.mysql.rescalendar;

import java.time.LocalDate;
import java.util.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iboot.pf.framework.common.pojo.PageResult;
import com.iboot.pf.framework.mybatis.core.mapper.BaseMapperX;
import com.iboot.pf.framework.mybatis.core.util.MyBatisUtils;
import com.iboot.pf.module.mdm.dal.dataobject.rescalendar.ResCalendarDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.iboot.pf.module.mdm.controller.admin.rescalendar.vo.*;

/**
 * 资源日历 Mapper
 *
 * @author iboot
 */
@Mapper
public interface ResCalendarMapper extends BaseMapperX<ResCalendarDO> {

    default PageResult<ResCalendarRespVO> selectPage(ResCalendarPageReqVO reqVO) {
        IPage<ResCalendarRespVO> mpPage = MyBatisUtils.buildPage(reqVO);
        selectJoinPage(mpPage, reqVO);
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    /**
     * 查询资源在指定日期区间的日历明细。
     */
    default List<ResCalendarDO> selectListByResIdsAndDateRange(List<Long> resIds, LocalDate dateFrom, LocalDate dateTo) {
        return selectList(new LambdaQueryWrapper<ResCalendarDO>()
                .in(ResCalendarDO::getResId, resIds)
                .between(ResCalendarDO::getWorkDate, dateFrom, dateTo));
    }

    /**
     * 删除日期区间内的模板生成行；人工调整行（generated_flag = false）保留。
     */
    default int deleteGeneratedByResIdsAndDateRange(List<Long> resIds, LocalDate dateFrom, LocalDate dateTo) {
        return delete(new LambdaQueryWrapper<ResCalendarDO>()
                .in(ResCalendarDO::getResId, resIds)
                .between(ResCalendarDO::getWorkDate, dateFrom, dateTo)
                .eq(ResCalendarDO::getGeneratedFlag, true));
    }

    ResCalendarRespVO selectJoinById(@Param("id") Long id);

    IPage<ResCalendarRespVO> selectJoinPage(IPage<ResCalendarRespVO> page, @Param("req") ResCalendarPageReqVO reqVO);

}