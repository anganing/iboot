package com.iboot.pf.module.mdm.dal.dataobject.calendar;

import lombok.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.iboot.pf.framework.mybatis.core.dataobject.BaseDO;

/**
 * 日历常规规则 DO：描述一周内每天各班次的常规可用分钟。
 *
 * @author iboot
 */
@TableName("mdm_calendar_rule")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarRuleDO extends BaseDO {

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
     * 星期几，1-7（周一至周日）
     */
    private Integer dayOfWeek;
    /**
     * 班次编号
     */
    private Long shiftId;
    /**
     * 可用分钟数
     */
    private Integer availableMinutes;

}
