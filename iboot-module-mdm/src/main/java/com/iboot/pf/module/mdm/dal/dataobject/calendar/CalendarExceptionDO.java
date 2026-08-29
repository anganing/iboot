package com.iboot.pf.module.mdm.dal.dataobject.calendar;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 日历例外 DO：例外日期覆盖常规规则。
 *
 * @author iboot
 */
@TableName("mdm_calendar_exception")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarExceptionDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 日历编号
     */
    private Long calendarId;
    /**
     * 例外日期
     */
    private LocalDate exceptionDate;
    /**
     * 例外类型：HOLIDAY-停工，OVERTIME-加班，MAINTENANCE-计划保养
     */
    private String exceptionType;
    /**
     * 班次编号；为空表示全天（仅停工例外支持全天）
     */
    private Long shiftId;
    /**
     * 可用分钟数
     */
    private Integer availableMinutes;

}
